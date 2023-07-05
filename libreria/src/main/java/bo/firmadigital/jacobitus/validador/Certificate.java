/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author ADSIB
 */
public class Certificate {
    public static String getPem(byte[] data) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(data));
            Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
            String pem = "-----BEGIN CERTIFICATE-----\n" + encoder.encodeToString(cert.getEncoded()) + "\n-----END CERTIFICATE-----";
            return pem;
        } catch (CertificateException ex) {
            return null;
        }
    }

    public static X509Certificate getCert(byte[] data) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(data));
            return cert;
        } catch (CertificateException ex) {
            return null;
        }
    }

    public static String getOCSP(X509Certificate cert) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = Certificate.class.getClassLoader().getResourceAsStream("firmadigital_bo.crt");
            List<X509Certificate> intermediates = (List<X509Certificate>) fact.generateCertificates(is);
            for (X509Certificate issuer : intermediates) {
                DatosCertificado datos = new DatosCertificado(issuer);
                if (datos.getNombreComunSubject().equals("Entidad Certificadora Publica ADSIB")) {
                    try {
                        cert.verify(issuer.getPublicKey());
                        return OCSP.check(cert, issuer);
                    } catch (GeneralSecurityException ignore) {
                        return "No lo emitió ADSIB";
                    }
                }
            }
            return "No lo emitión ADSIB";
        } catch (CertificateException ex) {
            return null;
        }
    }
}
