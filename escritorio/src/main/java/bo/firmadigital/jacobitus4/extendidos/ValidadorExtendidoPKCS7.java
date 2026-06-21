package bo.firmadigital.jacobitus4.extendidos;

import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.pkcs_9_at_signingTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

import bo.firmadigital.jacobitus.revocacion.CrlHelper;
import bo.firmadigital.jacobitus.validador.base.Opciones;
import bo.firmadigital.jacobitus.validador.comun.CadenaConfianzaHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;

/**
 *
 * @author ADSIB
 */
public class ValidadorExtendidoPKCS7 extends ValidadorExtendido {
    protected String urlPost = null;
    protected String token = null;

    protected Opciones opciones = null;

    public ValidadorExtendidoPKCS7(File file, Opciones opciones) {
        this.opciones = opciones;
        try {
            super.file = file;
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            firmas = listarCertificados(new FileInputStream(file));
        } catch (Exception ignore) {
            //
        }
    }

    public ValidadorExtendidoPKCS7(File file, String urlPost, String token, Opciones opciones) {
        this(file, opciones);
        this.urlPost = urlPost;
        this.token = token;
    }

    public ValidadorExtendidoPKCS7(InputStream is, Opciones opciones) {
        this.opciones = opciones;
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            firmas = listarCertificados(is);
        } catch (Exception ignore) {
            //
        }
    }

    @Override
    public boolean isRemoto() {
        return urlPost != null;
    }

    @Override
    public String getPost() {
        return urlPost;
    }

    @Override
    public String getToken() {
        return token;
    }

    @Override
    public String getAbsolutePath() {
        if (file.getName().endsWith(".p7s")) {
            try {
                File f = new File(System.getProperty("java.io.tmpdir"), file.getName().replace(".p7s", ""));
                InputStream is = new FileInputStream(file);
                CMSSignedData signedData = new CMSSignedData(is);
                CMSProcessable sc = signedData.getSignedContent();
                try (FileOutputStream os = new FileOutputStream(f)) {
                    sc.write(os);
                }
                return f.getAbsolutePath();
            } catch (CMSException | IOException ex) {
                Logger.getLogger(ValidadorExtendidoPKCS7.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file.getAbsolutePath();
    }

    @Override
    public void export(File f) {
        try {
            InputStream is = new FileInputStream(file);
            CMSSignedData signedData = new CMSSignedData(is);
            CMSProcessable sc = signedData.getSignedContent();
            try (FileOutputStream os = new FileOutputStream(f)) {
                sc.write(os);
            }
        } catch (CMSException | IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public String exportB64(InputStream is) {
        try {
            CMSSignedData signedData = new CMSSignedData(is);
            CMSProcessable sc = signedData.getSignedContent();
            String b64;
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                sc.write(os);
                b64 = Base64.getEncoder().encodeToString(os.toByteArray());
            }
            return b64;
        } catch (CMSException | IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List<Firma> listarCertificados(InputStream is) throws Exception {
        List<Firma> certs = new ArrayList<>();
        try {
            CMSSignedData signedData = new CMSSignedData(is);

            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();

            Integer numFirma = 1;
            for (SignerInformation signerInfo : signers) {
                Attribute attribute = signerInfo.getSignedAttributes().get(pkcs_9_at_signingTime);
                Calendar fecha = Calendar.getInstance();
                fecha.setTime(((ASN1UTCTime)attribute.getAttrValues().getObjectAt(0)).getDate());
                // Integridad del documento
                X509Certificate x509Certificate = null;
                boolean integridad = false;
                Store store = signedData.getCertificates();
                Collection<X509CertificateHolder> allCerts = store.getMatches(null);
                for (X509CertificateHolder holder : allCerts) {
                    x509Certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
                    if (signerInfo.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider("BC").build(x509Certificate))) {
                        integridad = true;
                        break;
                    }
                }
                Firma firma = new Firma(numFirma.toString(), x509Certificate, fecha, null, false);
                firma.setIntegridad(integridad);
                firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate()));
                firma.setRevocacion(CrlHelper.verificar((X509Certificate) firma.getCertificate(), firma.getFecFirma(), this.opciones));
                certs.add(firma);
                numFirma++;
            }
        } catch (CMSException ignore) { }
        return certs;
    }
}
