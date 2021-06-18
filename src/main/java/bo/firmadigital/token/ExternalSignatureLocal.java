package bo.firmadigital.token;

import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.IExternalSignature;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;

public class ExternalSignatureLocal implements IExternalSignature {
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
    public String getHashAlgorithm() {
        return DigestAlgorithms.getDigest(DigestAlgorithms.getAllowedDigest("SHA256"));
    }

    @Override
    public synchronized String getEncryptionAlgorithm() {
        try {
            Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
            token.iniciar(pass);
            PrivateKey privateKey = token.obtenerClavePrivada(label);
            return privateKey.getAlgorithm();
        } catch (GeneralSecurityException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public synchronized Certificate[] getChain() {
        try {
            Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
            return token.getCertificateChain(label);
        } catch (KeyStoreException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public synchronized byte[] sign(byte[] sh) throws GeneralSecurityException {
        Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
        token.iniciar(pass);
        PrivateKey privateKey = token.obtenerClavePrivada(label);
        String signMode = getHashAlgorithm();
        signMode += "with" + privateKey.getAlgorithm();
        Signature signature = Signature.getInstance(signMode, token.getProviderName());
        signature.initSign(privateKey);
        signature.update(sh);
        return signature.sign();
    }
}
