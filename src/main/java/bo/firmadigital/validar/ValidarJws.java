/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import static bo.firmadigital.validar.Validar.verificarOcsp;
import static bo.firmadigital.validar.Validar.verificarPKI;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author ADSIB
 */
public class ValidarJws extends Validar {
    private X509Certificate cert;

    public ValidarJws(File file) {
        try {
            super.file = file;
            try (InputStream is = new FileInputStream(file)) {
                certificados = listarCertificados(is);
            }
        } catch (Exception ignore) {
        }
    }

    public ValidarJws(InputStream is) {
        try {
            certificados = listarCertificados(is);
        } catch (Exception ignore) {
        }
    }

    public final List<CertDate> listarCertificados(InputStream is) throws Exception {
        List<CertDate> certs = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
        InputStream in = new ByteArrayInputStream(jwsObject.getHeader().getX509CertChain().get(0).decode());
        cert = (X509Certificate)certFactory.generateCertificate(in);
        CertDate certDate = new CertDate("Firma", cert, new GregorianCalendar(), null, false);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey)cert.getPublicKey());
        certDate.setValid(jwsObject.verify(verifier));
        certDate.setPKI(verificarPKI(certDate.getCertificate()));
        certDate.setOCSP(verificarOcsp((X509Certificate) certDate.getCertificate(), certDate.getSignDate()));
        certs.add(certDate);
        return certs;
    }
}
