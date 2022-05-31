/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.xml.security.Init;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author ADSIB
 */
public class ValidarXml extends Validar {
    static {
        Init.init();
    }

    public ValidarXml(File file) {
        try {
            super.file = file;
            certificados = listarCertificados(new FileInputStream(file));
        } catch (Exception ignore) {
        }
    }

    public ValidarXml(InputStream is) {
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
                        }
                    }
                }
                CertDate certDate = new CertDate(firma.toString(), cert, new GregorianCalendar(), null, false);
                certDate.setValid(integrity);
                certDate.setPKI(verificarPKI(certDate.getCertificate()));
                certDate.setOCSP(verificarOcsp((X509Certificate) certDate.getCertificate(), certDate.getSignDate()));
                certs.add(certDate);
                firma++;
            }
        } catch (Exception ignore) {
        }
        return certs;
    }
}
