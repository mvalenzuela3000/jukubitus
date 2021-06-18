/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.firmar;

import bo.firmadigital.token.ExternalSignatureLocal;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Token;
import com.itextpdf.forms.PdfSigFieldLock;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

/**
 *
 * @author ADSIB
 */
public class FirmarPdf implements Firmar {
    private static FirmarPdf firmarPdf;
    private final long slot;
    private final String label;
    private final String pass;

    private FirmarPdf(long slot, String label, String pass) {
        this.slot = slot;
        this.label = label;
        this.pass = pass;
    }

    public static FirmarPdf getInstance(long slot, String label, String pass) {
        if (firmarPdf == null) {
            firmarPdf = new FirmarPdf(slot, label, pass);
        } else {
            if (firmarPdf.slot != slot || !firmarPdf.label.equals(label) || !firmarPdf.pass.equals(pass)) {
                firmarPdf = new FirmarPdf(slot, label, pass);
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
        Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
        token.iniciar(pass);
        IExternalSignature signature = new ExternalSignatureLocal(token.obtenerClavePrivada(label), token.getProviderName());
        signer.signDetached(digest, signature, token.getCertificateChain(label), null, null, null, 0, PdfSigner.CryptoStandard.CADES);
        token.salir();
        
    }

    @Override
    public synchronized void firmar(InputStream is, OutputStream os) throws IOException, GeneralSecurityException {
        firmar(is, os, false);
    }
    
}
