/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.firmador;

import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Token;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.URIReference;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.jcp.xml.dsig.internal.dom.DOMSubTreeData;
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ADSIB
 */
public class FirmadorXml implements IFirmador {
    private Opciones opciones = null;
    private static FirmadorXml firmarXml;
    private final long slot;
    private final String label;
    private final String pass;
    private final String node;
    private String messageDigestAlgorithm = MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
    private String signatureMethodAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;

    static {
        Init.init();
    }

    private FirmadorXml(long slot, String label, String pass, String node, Opciones opciones) {
        this.opciones = opciones;
        this.slot = slot;
        this.label = label;
        this.pass = pass;
        this.node = node;
    }

    public static FirmadorXml getInstance(long slot, String label, String pass, Opciones opciones) {
        return getInstance(slot, label, pass, opciones);
    }

    public static FirmadorXml getInstance(long slot, String label, String pass, String node, Opciones opciones) {
        if (firmarXml == null) {
            firmarXml = new FirmadorXml(slot, label, pass, node, opciones);
        } else {
            if (firmarXml.slot != slot || !firmarXml.label.equals(label) || !firmarXml.pass.equals(pass)) {
                firmarXml = new FirmadorXml(slot, label, pass, node, opciones);
            } else {
                if (firmarXml.node == null) {
                    if (node != null) {
                        firmarXml = new FirmadorXml(slot, label, pass, node, opciones);
                    }
                } else {
                    if (!firmarXml.node.equals(node)) {
                        firmarXml = new FirmadorXml(slot, label, pass, node, opciones);
                    }
                }
            }
        }
        firmarXml.messageDigestAlgorithm = MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256;
        firmarXml.signatureMethodAlgorithm = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256;
        return firmarXml;
    }

    public void setMessageDigestAlgorithm(String messageDigestAlgorithm) {
        this.messageDigestAlgorithm = messageDigestAlgorithm;
    }

    public void setSignatureMethodAlgorithm(String signatureMethodAlgorithm) {
        this.signatureMethodAlgorithm = signatureMethodAlgorithm;
    }

    @Override
    public void firmar(InputStream is, OutputStream os, boolean param) throws IOException, GeneralSecurityException {
        if (param) {
            try {
                Token token = GestorSlot.getInstance().obtenerSlot(slot, this.opciones).getToken();
                token.iniciar(pass);
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xml = builder.parse(is);

                DOMSignContext signContext;
                XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");
                Reference ref;
                if (node == null) {
                    signContext = new DOMSignContext(token.obtenerClavePrivada(label), xml.getDocumentElement());
                    ref = sigFactory.newReference("", sigFactory.newDigestMethod(messageDigestAlgorithm, null), Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
                } else {
                    NodeList nodos = xml.getElementsByTagName(node);
                    if (nodos.getLength() != 1) {
                        throw new IOException("Error al identificar el nodo: " + node);
                    }
                    signContext = new DOMSignContext(token.obtenerClavePrivada(label), nodos.item(0).getParentNode());
                    signContext.setURIDereferencer((URIReference uriReference, XMLCryptoContext context) -> {
                        Node data = xml.getElementsByTagName(uriReference.getURI().replace("#", "")).item(0);
                        return new DOMSubTreeData(data, false);
                    });
                    ref = sigFactory.newReference("#" + node, sigFactory.newDigestMethod(messageDigestAlgorithm, null), Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)), null, null);
                }
                SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null), sigFactory.newSignatureMethod(signatureMethodAlgorithm, null), Collections.singletonList(ref));
                KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
                X509Data x509Data = keyInfoFactory.newX509Data(Collections.singletonList(token.obtenerCertificado(label)));
                KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
                javax.xml.crypto.dsig.XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);
                signature.sign(signContext);
                XMLUtils.outputDOMc14nWithComments(xml, os);
                token.salir();
            } catch (ParserConfigurationException | SAXException | MarshalException | XMLSignatureException ex) {
                throw new IOException(ex.getMessage());
            }
        } else {
            try {
                Token token = GestorSlot.getInstance().obtenerSlot(slot, this.opciones).getToken();
                token.iniciar(pass);
                ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "");
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document xml = builder.parse(is);
                Element parent;
                Transforms transforms = new Transforms(xml);
                transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
                if (node == null) {
                    parent = (Element) xml.getFirstChild();
                    xml.setXmlStandalone(false);
                    transforms.addTransform(Transforms.TRANSFORM_C14N_WITH_COMMENTS);
                } else {
                    NodeList nodos = xml.getElementsByTagName(node);
                    if (nodos.getLength() != 1) {
                        throw new IOException("Error al identificar el nodo: " + node);
                    }
                    parent = (Element)nodos.item(0).getParentNode();
                }
                XMLSignature signature = new XMLSignature(xml, null, signatureMethodAlgorithm);
                parent.appendChild(signature.getElement());
                if (node == null) {
                    signature.addDocument("", transforms, messageDigestAlgorithm);
                } else {
                    signature.addDocument("#" + node, transforms, messageDigestAlgorithm);
                }
                X509Certificate cert = token.obtenerCertificado(label);
                cert.checkValidity();
                signature.addKeyInfo(cert);
                if (node != null) {
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
                }
                signature.sign(token.obtenerClavePrivada(label));
                XMLUtils.outputDOMc14nWithComments(xml, os);
                token.salir();
            } catch (ParserConfigurationException | SAXException | XMLSecurityException ex) {
                throw new IOException(ex.getMessage());
            }
        }
    }

    @Override
    public void firmar(InputStream is, OutputStream os) throws IOException, GeneralSecurityException {
        firmar(is, os, false);
    }
}
