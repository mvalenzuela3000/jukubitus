/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.firmar;

import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.token.ExternalSignatureLocal;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.TSAClient;
import bo.firmadigital.token.Token;
import bo.firmadigital.validar.DatosCertificado;
import com.itextpdf.forms.PdfSigFieldLock;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.ITSAClient;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 *
 * @author ADSIB
 */
public class FirmarPdf implements Firmar {
    private static FirmarPdf firmarPdf;
    private final long slot;
    private final String label;
    private final String pass;
    private ImageData imageData;
    private int x = 0;
    private int y = 0;
    private final Config config;

    private FirmarPdf(long slot, String label, String pass) {
        this.slot = slot;
        this.label = label;
        this.pass = pass;
        config = Config.getInstance();
    }

    private FirmarPdf(long slot, String label, String pass, String imageBase64, int x, int y) {
        this.slot = slot;
        this.label = label;
        this.pass = pass;
        config = Config.getInstance();
        if (imageBase64 != null) {
            byte[] bytes = Base64.getDecoder().decode(imageBase64);
            imageData = ImageDataFactory.create(bytes, false);
        }
        this.x = x;
        this.y = y;
    }

    public static FirmarPdf getInstance(long slot, String label, String pass) {
        if (firmarPdf == null) {
            firmarPdf = new FirmarPdf(slot, label, pass);
        } else {
            if (firmarPdf.slot != slot || !firmarPdf.label.equals(label) || !firmarPdf.pass.equals(pass) || firmarPdf.x != 0 || firmarPdf.y != 0) {
                firmarPdf = new FirmarPdf(slot, label, pass);
            }
        }
        return firmarPdf;
    }

    public static FirmarPdf getInstance(long slot, String label, String pass, String image, int x, int y) {
        if (firmarPdf == null) {
            firmarPdf = new FirmarPdf(slot, label, pass, image, x, y);
        } else {
            if (firmarPdf.slot != slot || !firmarPdf.label.equals(label) || !firmarPdf.pass.equals(pass) || firmarPdf.x != x || firmarPdf.y != y) {
                firmarPdf = new FirmarPdf(slot, label, pass, image, x, y);
            }
        }
        return firmarPdf;
    }

    @Override
    public synchronized void firmar(InputStream is, OutputStream os, boolean bloquear) throws IOException, GeneralSecurityException {
        PdfReader reader = new PdfReader(is);
        StampingProperties stamp = new StampingProperties();
        stamp.useAppendMode();
        PdfSigner signer = new PdfSigner(reader, os, stamp);
        if (reader.isEncrypted()) {
            throw new IOException("El documento se encuentra encriptado.");
        }
        if (bloquear) {
            PdfSigFieldLock fieldLock = new PdfSigFieldLock();
            fieldLock.setDocumentPermissions(PdfSigFieldLock.LockPermissions.NO_CHANGES_ALLOWED);
            fieldLock.setFieldLock(PdfSigFieldLock.LockAction.EXCLUDE, new String[]{});
            signer.setFieldLockDict(fieldLock);
        }
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
        token.iniciar(pass);
        if (x != 0) {
            if (imageData != null) {
                appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
                appearance.setSignatureGraphic(imageData);
            }
            DatosCertificado cert = new DatosCertificado(token.obtenerCertificado(label));
            appearance.setLayer2Text("Firmado por " + cert.getNombreComunSubject() + "\n" + cert.getCargoSubject());
            rect = new Rectangle(x, y, 200, 50);
        }
        appearance.setPageRect(rect);
        ITSAClient tsc = null;
        if (config.isTSEnabled()) {
            tsc = new TSAClient(config.getTS(), config.getTSJWT());
        }
        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new ExternalSignatureLocal(token.obtenerClavePrivada(label), token.getProviderName());
        signer.signDetached(digest, signature, token.getCertificateChain(label), null, null, tsc, 0, PdfSigner.CryptoStandard.CADES);
        token.salir();
        
    }

    @Override
    public synchronized void firmar(InputStream is, OutputStream os) throws IOException, GeneralSecurityException {
        firmar(is, os, false);
    }

    public static synchronized void firmar(InputStream is, OutputStream os, boolean bloquear, Token token, String label) throws IOException, GeneralSecurityException {
        PdfReader reader = new PdfReader(is);
        StampingProperties stamp = new StampingProperties();
        stamp.useAppendMode();
        PdfSigner signer = new PdfSigner(reader, os, stamp);
        if (reader.isEncrypted()) {
            throw new IOException("El documento se encuentra encriptado.");
        }
        if (bloquear) {
            PdfSigFieldLock fieldLock = new PdfSigFieldLock();
            fieldLock.setDocumentPermissions(PdfSigFieldLock.LockPermissions.NO_CHANGES_ALLOWED);
            fieldLock.setFieldLock(PdfSigFieldLock.LockAction.EXCLUDE, new String[]{});
            signer.setFieldLockDict(fieldLock);
        }
        Rectangle rect = new Rectangle(0, 0, 0, 0);
        PdfSignatureAppearance appearance = signer.getSignatureAppearance();
        appearance.setPageRect(rect);

        IExternalDigest digest = new BouncyCastleDigest();
        IExternalSignature signature = new ExternalSignatureLocal(token.obtenerClavePrivada(label), token.getProviderName());
        signer.signDetached(digest, signature, token.getCertificateChain(label), null, null, null, 0, PdfSigner.CryptoStandard.CADES);
    }
}
