/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.xml.security.Init;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author ADSIB
 */
public class ValidadorXml extends Validador {
    protected Opciones opciones = null;

    static {
        Init.init();
    }

    public ValidadorXml(File file, Opciones opciones) {
        this.opciones = opciones;
        try {
            super.file = file;
            certificados = listarCertificados(new FileInputStream(file));
        } catch (Exception ignore) {
        }
    }

    public ValidadorXml(InputStream is, Opciones opciones) {
        this.opciones = opciones;
        try {
            certificados = listarCertificados(is);
        } catch (Exception ignore) {
        }
    }

    public List<CertDate> listarCertificados(InputStream is) throws Exception {
        List<CertDate> certs = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(is);

            NodeList nl = xml.getElementsByTagName("Signature");

            Integer firma = 1;
            for (int i = 0; i < nl.getLength(); i++) {
                XMLSignature signature = new XMLSignature((Element)nl.item(i), null);
                signature.addResourceResolver(new ResourceResolverSpi() {
                    @Override
                    public XMLSignatureInput engineResolveURI(ResourceResolverContext rrc) throws ResourceResolverException {
                        String nodo = rrc.uriToResolve.replace("#", "");
                        return new XMLSignatureInput(xml.getElementsByTagName(nodo).item(0).getParentNode());
                    }

                    @Override
                    public boolean engineCanResolveURI(ResourceResolverContext rrc) {
                        String nodo = rrc.uriToResolve.replace("#", "");
                        return xml.getElementsByTagName(nodo).getLength() == 1;
                    }
                });
                KeyInfo kinfo = signature.getKeyInfo();
                X509Certificate cert = null;
                boolean integrity = false;
                if (kinfo != null) {
                    cert = kinfo.getX509Certificate();
                    if (cert != null) {
                        if (signature.checkSignatureValue(cert)) {
                            integrity = true;
                        } else {
                            integrity = Enveloped.enveloped(xml, nl.item(i));
                        }
                    }
                }
                CertDate certDate = new CertDate(firma.toString(), cert, new GregorianCalendar(), null, false);
                certDate.setValid(integrity);
                certDate.setPKI(verificarPKI(certDate.getCertificate()));
                certDate.setOCSP(verificarOcsp((X509Certificate) certDate.getCertificate(), certDate.getSignDate(), this.opciones));
                certs.add(certDate);
                firma++;
            }
        } catch (Exception ignore) {
        }
        return certs;
    }

    private static class Enveloped {
        public static boolean enveloped(Document xml, Node node) throws XMLSignatureException, MarshalException {
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
            DOMValidateContext validateContext = new DOMValidateContext(new X509KeySelector(), node);
            validateContext.setURIDereferencer((URIReference uriReference, XMLCryptoContext context) -> {
                Node data = xml.getElementsByTagName(uriReference.getURI().replace("#", "")).item(0);
                return new DOMSubTreeData(data, false);
            });
            javax.xml.crypto.dsig.XMLSignature signature = sigFactory.unmarshalXMLSignature(validateContext);
            javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo = signature.getKeyInfo();
            X509Certificate cert = null;
            boolean integrity = false;
            if (keyInfo != null) {
                X509Data x509Data = (X509Data) keyInfo.getContent().get(0);
                cert = (X509Certificate) x509Data.getContent().get(0);
                if (cert != null) {
                    if (signature.validate(validateContext)) {
                        integrity = true;
                    }
                }
            }
            return integrity;
        }

        private static class X509KeySelector extends KeySelector {
            public KeySelectorResult select(javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
                for (Object keyInfoItem : keyInfo.getContent()) {
                    if (keyInfoItem instanceof X509Data) {
                        X509Data x509Data = (X509Data) keyInfoItem;
                        for (Object x509CertItem : x509Data.getContent()) {
                            if (x509CertItem instanceof X509Certificate) {
                                PublicKey publicKey = ((X509Certificate) x509CertItem).getPublicKey();
                                return new SimpleKeySelectorResult(publicKey);
                            }
                        }
                    }
                }
                throw new KeySelectorException("No public key found for the XMLDSig signature");
            }
        }

        private static class SimpleKeySelectorResult implements KeySelectorResult {
            private final PublicKey publicKey;
            public SimpleKeySelectorResult(PublicKey publicKey) {
                this.publicKey = publicKey;
            }
            public Key getKey() { return publicKey; }
        }
    }
}
