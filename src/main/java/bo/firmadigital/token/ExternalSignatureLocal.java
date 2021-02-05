package bo.firmadigital.token;

import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.DigestAlgorithms;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

public class ExternalSignatureLocal implements ExternalSignatureContainer {
    private static ExternalSignatureLocal externalSignatureLocal;
    private final long slot;
    private final String label;
    private final String pass;

    private ExternalSignatureLocal(long slot, String label, String pass) {
        this.slot = slot;
        this.label = label;
        this.pass = pass;
    }

    public static ExternalSignatureLocal getInstance(long slot, String label, String pass) {
        if (externalSignatureLocal == null) {
            externalSignatureLocal = new ExternalSignatureLocal(slot, label, pass);
        } else {
            if (externalSignatureLocal.slot != slot || !externalSignatureLocal.label.equals(label) || !externalSignatureLocal.pass.equals(pass)) {
                externalSignatureLocal = new ExternalSignatureLocal(slot, label, pass);
            }
        }
        return externalSignatureLocal;
    }

    @Override
    public synchronized byte[] sign(InputStream is) throws GeneralSecurityException {
        try {
            Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
            token.iniciar(pass);
            PrivateKey privateKey = token.obtenerClavePrivada(label);
            String signMode = DigestAlgorithms.getDigest(DigestAlgorithms.getAllowedDigests("SHA256"));
            signMode += "with" + privateKey.getAlgorithm();
            Signature signature = Signature.getInstance(signMode, token.getProviderName());
            signature.initSign(privateKey);
            String hashAlgorithm = "SHA256";
            BouncyCastleDigest digest = new BouncyCastleDigest();
            PdfPKCS7 pdfPKCS7 = new PdfPKCS7(null, new Certificate[] { token.obtenerCertificado(label) }, hashAlgorithm, null, digest, false);
            byte hash[] = DigestAlgorithms.digest(is, digest.getMessageDigest(hashAlgorithm));
            byte[] sh = pdfPKCS7.getAuthenticatedAttributeBytes(hash, null, null, MakeSignature.CryptoStandard.CADES);
            signature.update(sh);
            byte[] extSignature = signature.sign();
            pdfPKCS7.setExternalDigest(extSignature, null, "RSA");
            TSAClient tsc = null;
            /*if (MyApplication.isTimeStamp()) {
                tsc = new TSAClientBouncyCastle(MyApplication.getUrlTS(), MyApplication.getUserTS(), MyApplication.getPassTS());
            }*/
            token.salir();
            return pdfPKCS7.getEncodedPKCS7(hash, tsc, null, null, MakeSignature.CryptoStandard.CADES);
        } catch (IOException | InvalidKeyException e) {
            throw new GeneralSecurityException(e.getMessage());
        }
    }

    @Override
    public void modifySigningDictionary(PdfDictionary signDic) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
