/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.firmador;

import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Token;
import bo.firmadigital.jacobitus.validador.MagicBytes;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 *
 * @author ADSIB
 */
public class FirmadorPKCS7 implements IFirmador {
    private Opciones opciones = null;
    private static FirmadorPKCS7 firmarPkcs7;
    private final long slot;
    private final String label;
    private final String pass;

    private FirmadorPKCS7(long slot, String label, String pass, Opciones opciones) {
        this.opciones = opciones;
        this.slot = slot;
        this.label = label;
        this.pass = pass;
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static FirmadorPKCS7 getInstance(long slot, String label, String pass, Opciones opciones) {
        if (firmarPkcs7 == null) {
            firmarPkcs7 = new FirmadorPKCS7(slot, label, pass, opciones);
        } else {
            if (firmarPkcs7.slot != slot || !firmarPkcs7.label.equals(label) || !firmarPkcs7.pass.equals(pass)) {
                firmarPkcs7 = new FirmadorPKCS7(slot, label, pass, opciones);
            }
        }
        return firmarPkcs7;
    }

    @Override
    public synchronized void firmar(InputStream is, OutputStream os, boolean detached) throws IOException, GeneralSecurityException {
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
            List<Certificate> certlist = new ArrayList<>();
            certlist.add(x509Certificate);
            Store certstore = new JcaCertStore(certlist);
            ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").
                    build()).build(signer, (X509Certificate) x509Certificate));
            generator.addCertificates(certstore);
            
            CMSTypedData cmsdata;
            if (MagicBytes.P7S.is(is)) {
                if (detached) {
                    throw new RuntimeException("No puede realizar múltiples firmas con la opción detached.");
                }
                CMSSignedData signedData = new CMSSignedData(is);
                cmsdata = signedData.getSignedContent();
                Store current = signedData.getCertificates();
                generator.addCertificates(current);
                generator.addSigners(signedData.getSignerInfos());
            } else {
                byte[] data = is.readAllBytes();
                cmsdata = new CMSProcessableByteArray(data);
            }
            CMSSignedData signeddata = generator.generate(cmsdata, !detached);
            os.write(signeddata.getEncoded());
            token.salir();
        } catch (OperatorCreationException | CMSException ex) {
            Logger.getLogger(FirmadorPKCS7.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void firmar(InputStream is, OutputStream os) throws IOException, GeneralSecurityException {
        firmar(is, os, false);
    }
}
