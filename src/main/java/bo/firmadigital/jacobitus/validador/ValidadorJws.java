/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ADSIB
 */
public class ValidadorJws extends Validador {
    protected Opciones opciones = null;

    private X509Certificate cert;

    public ValidadorJws(File file, Opciones opciones) {
        this.opciones = opciones;
        try {
            super.file = file;
            try (InputStream is = new FileInputStream(file)) {
                certificados = listarCertificados(is);
            }
        } catch (Exception ignore) {
        }
    }

    public ValidadorJws(InputStream is, Opciones opciones) {
        this.opciones = opciones;
        try {
            certificados = listarCertificados(is);
        } catch (Exception ignore) {
        }
    }

    @Override
    public String getAbsolutePath() {
        if (file.getName().endsWith(".jws")) {
            try {
                File f = new File(System.getProperty("java.io.tmpdir"), file.getName().replace(".jws", ".json"));
                InputStream is = new FileInputStream(file);
                JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
                byte[] payload = jwsObject.getPayload().toBytes();
                try (FileOutputStream os = new FileOutputStream(f)) {
                    os.write(payload);
                }
                return f.getAbsolutePath();
            } catch (ParseException | IOException ex) {
                Logger.getLogger(ValidadorJws.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file.getAbsolutePath();
    }

    @Override
    public void export(File f) {
        try {
            InputStream is = new FileInputStream(file);
            JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
            byte[] payload = jwsObject.getPayload().toBytes();
            try (FileOutputStream os = new FileOutputStream(f)) {
                os.write(payload);
            }
        } catch (IOException | ParseException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public String exportB64(InputStream is) {
        try {
            JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
            return Base64.getEncoder().encodeToString(jwsObject.getPayload().toBytes());
        }   catch (IOException | ParseException ex) {
            throw new RuntimeException(ex.getMessage());
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
        certDate.setOCSP(verificarOcsp((X509Certificate) certDate.getCertificate(), certDate.getSignDate(), this.opciones));
        certs.add(certDate);
        return certs;
    }
}
