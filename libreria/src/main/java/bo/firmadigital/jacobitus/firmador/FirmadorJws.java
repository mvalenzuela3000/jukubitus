/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.firmador;

import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Token;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ADSIB
 */
public class FirmadorJws implements IFirmador {
    private Opciones opciones = null;
    private static FirmadorJws firmarJws;
    private final long slot;
    private final String label;
    private final String pass;

    private FirmadorJws(long slot, String label, String pass, Opciones opciones) {
        this.opciones = opciones;
        this.slot = slot;
        this.label = label;
        this.pass = pass;
    }

    public static FirmadorJws getInstance(long slot, String label, String pass, Opciones opciones) {
        if (firmarJws == null) {
            firmarJws = new FirmadorJws(slot, label, pass, opciones);
        } else {
            if (firmarJws.slot != slot || !firmarJws.label.equals(label) || !firmarJws.pass.equals(pass)) {
                firmarJws = new FirmadorJws(slot, label, pass, opciones);
            }
        }
        return firmarJws;
    }

    @Override
    public synchronized void firmar(InputStream is, OutputStream os, boolean param) throws IOException, GeneralSecurityException {
        try {
            Token token = GestorSlot.getInstance().obtenerSlot(slot, this.opciones).getToken();
            token.iniciar(pass);
            PrivateKey privateKey = token.obtenerClavePrivada(label);
            if (privateKey == null) {
                token.salir();
                throw new RuntimeException("No se encontró la clave con alias: " + label);
            }
            X509Certificate x509Certificate = token.obtenerCertificado(label);
            x509Certificate.checkValidity();
            String pemCert = Base64.getEncoder().encodeToString(x509Certificate.getEncoded());
            JWSSigner jwsSigner = new RSASSASigner(privateKey);
            JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
            List<com.nimbusds.jose.util.Base64> x5c = new LinkedList<>();
            x5c.add(new com.nimbusds.jose.util.Base64(pemCert));
            builder.x509CertChain(x5c);
            JWSObject jwsObject = new JWSObject(builder.build(), new Payload(new String(is.readAllBytes())));
            jwsObject.sign(jwsSigner);
            os.write(jwsObject.serialize().getBytes());
            token.salir();
        } catch(JOSEException ex) {
            Logger.getLogger(FirmadorJws.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void firmar(InputStream is, OutputStream os) throws IOException, GeneralSecurityException {
        firmar(is, os, false);
    }

    public static synchronized void firmar(InputStream is, OutputStream os, boolean bloquear, Token token, String label) throws IOException, GeneralSecurityException {
        try {
            PrivateKey privateKey = token.obtenerClavePrivada(label);
            if (privateKey == null) {
                throw new RuntimeException("No se encontró la clave con alias: " + label);
            }
            X509Certificate x509Certificate = token.obtenerCertificado(label);
            x509Certificate.checkValidity();
            String pemCert = Base64.getEncoder().encodeToString(x509Certificate.getEncoded());
            JWSSigner jwsSigner = new RSASSASigner(privateKey);
            JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
            List<com.nimbusds.jose.util.Base64> x5c = new LinkedList<>();
            x5c.add(new com.nimbusds.jose.util.Base64(pemCert));
            builder.x509CertChain(x5c);
            JWSObject jwsObject = new JWSObject(builder.build(), new Payload(new String(is.readAllBytes())));
            jwsObject.sign(jwsSigner);
            os.write(jwsObject.serialize().getBytes());
        } catch(JOSEException ex) {
            Logger.getLogger(FirmadorJws.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
