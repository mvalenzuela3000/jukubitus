package bo.firmadigital.jacobitus4.extendidos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
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

import bo.firmadigital.jacobitus.revocacion.RevocacionHelper;
import bo.firmadigital.jacobitus.validador.base.ConfiguracionValidador;
import bo.firmadigital.jacobitus.validador.comun.CadenaConfianzaHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;

public class ValidadorExtendidoXml extends ValidadorExtendido {
    protected ConfiguracionValidador configValidador = null;

    private Calendar fecFirmaPresunta = null;

    static {
        Init.init();
    }

    public ValidadorExtendidoXml(File archivo, Date fecFirmaPresunta, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        try {
            super.file = archivo;
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(fecFirmaPresunta);
            this.fecFirmaPresunta = calendario;
            firmas = listarCertificados(new FileInputStream(archivo));
        } catch (Exception ignore) {
            //
        }
    }

    public ValidadorExtendidoXml(InputStream is, Date fecFirmaPresunta, ConfiguracionValidador opciones) {
        this.configValidador = opciones;
        try {
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(fecFirmaPresunta);
            this.fecFirmaPresunta = calendario;
            firmas = listarCertificados(is);
        } catch (Exception ignore) {
            //
        }
    }

    public List<Firma> listarCertificados(InputStream is) {
        List<Firma> firmas = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(true);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(is);

            NodeList nl = xml.getElementsByTagName("Signature");
            Integer numFirma = 1;
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
                X509Certificate x509Certificate = null;
                boolean integridad = false;
                if (kinfo != null) {
                    x509Certificate = kinfo.getX509Certificate();
                    if (x509Certificate != null) {
                        if (signature.checkSignatureValue(x509Certificate)) {
                            integridad = true;
                        } else {
                            integridad = Enveloped.enveloped(xml, nl.item(i));
                        }
                    }
                }
                Firma firma = new Firma(numFirma.toString(), x509Certificate, null, null, false);
                if (this.fecFirmaPresunta != null) {
                    firma = new Firma(numFirma.toString(), x509Certificate, this.fecFirmaPresunta, null, false);
                }
                firma.setIntegridad(integridad);
                firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate(), this.configValidador.getProxy()));
                if (this.fecFirmaPresunta != null) {
                    firma.setRevocacion(RevocacionHelper.verificar((X509Certificate) firma.getCertificate(), this.configValidador.getProxy(), firma.getFecFirma()));
                } else {
                    firma.setRevocacion(null);
                }
                firmas.add(firma);
                numFirma++;
            }
            
            nl = xml.getElementsByTagName("ds:Signature");
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
                X509Certificate x509Certificate = null;
                boolean integridad = false;
                if (kinfo != null) {
                    x509Certificate = kinfo.getX509Certificate();
                    if (x509Certificate != null) {
                        if (signature.checkSignatureValue(x509Certificate)) {
                            integridad = true;
                        } else {
                            integridad = Enveloped.enveloped(xml, nl.item(i));
                        }
                    }
                }
                Firma firma = new Firma(numFirma.toString(), x509Certificate, null, null, false);
                firma.setIntegridad(integridad);
                firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate(), this.configValidador.getProxy()));
                if (this.fecFirmaPresunta != null) {
                    firma.setRevocacion(RevocacionHelper.verificar((X509Certificate) firma.getCertificate(), this.configValidador.getProxy(), firma.getFecFirma()));
                } else {
                    firma.setRevocacion(null);
                }
                firmas.add(firma);
                numFirma++;
            }
        } catch (Exception ignore) {
            //
        }
        return firmas;
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
                if (cert != null && signature.validate(validateContext)) {
                    integrity = true;
                }
            }
            return integrity;
        }

        private static class X509KeySelector extends KeySelector {
            public KeySelectorResult select(javax.xml.crypto.dsig.keyinfo.KeyInfo keyInfo, KeySelector.Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
                for (XMLStructure keyInfoItem : keyInfo.getContent()) {
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
