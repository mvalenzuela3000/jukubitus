package bo.firmadigital.jacobitus.escritorio.extendidos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.revocacion.RevocacionHelper;
import bo.firmadigital.jacobitus.validador.base.ConfiguracionValidador;
import bo.firmadigital.jacobitus.validador.comun.CadenaConfianzaHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;

public class ValidadorExtendidoJws extends ValidadorExtendido {
    private static final Logger LOG = Logger.getLogger(ValidadorExtendidoJws.class.getName());

    protected ConfiguracionValidador configValidador = null;

    private Date fecFirmaPresunta = null;

    public ValidadorExtendidoJws(File archivo, Date fecFirmaPresunta, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        try {
            super.file = archivo;
            this.fecFirmaPresunta = fecFirmaPresunta;
            firmas = listarCertificados(new FileInputStream(archivo));
        } catch (Exception ignore) {
            LOG.severe(ignore.getMessage());
        }
    }

    public ValidadorExtendidoJws(InputStream is, Date fecFirmaPresunta, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        try {
            this.fecFirmaPresunta = fecFirmaPresunta;
            this.firmas = listarCertificados(is);
        } catch (Exception ignore) {
            LOG.severe(ignore.getMessage());
        }
    }

    @Override
    public String getAbsolutePath() {
        if (file.getName().endsWith(".jws")) {
            try {
                File f = new File(System.getProperty("java.io.tmpdir"), file.getName().replace(".jws", ".json"));
                try (InputStream is = new FileInputStream(file)) {
                    JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
                    byte[] payload = jwsObject.getPayload().toBytes();
                    try (FileOutputStream os = new FileOutputStream(f)) {
                        os.write(payload);
                    }
                }
                return f.getAbsolutePath();
            } catch (ParseException | IOException ex) {
                Logger.getLogger(ValidadorExtendidoJws.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return file.getAbsolutePath();
    }

    @Override
    public void export(File f) {
        try (InputStream is = new FileInputStream(file)) {
            JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
            byte[] payload = jwsObject.getPayload().toBytes();
            try (FileOutputStream os = new FileOutputStream(f)) {
                os.write(payload);
            }
        } catch (IOException | ParseException ex) {
            throw new JacobitusException(ex.getMessage());
        }
    }

    @Override
    public String exportB64(InputStream is) {
        try {
            JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
            return Base64.getEncoder().encodeToString(jwsObject.getPayload().toBytes());
        }   catch (IOException | ParseException ex) {
            throw new JacobitusException(ex.getMessage());
        }
    }

    public final List<Firma> listarCertificados(InputStream is) throws CertificateException, ParseException, IOException, JOSEException {
        List<Firma> firmas = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
        InputStream in = new ByteArrayInputStream(jwsObject.getHeader().getX509CertChain().get(0).decode());
        X509Certificate x509Certificate = (X509Certificate)certFactory.generateCertificate(in);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey)x509Certificate.getPublicKey());

        Firma firma = new Firma("Firma", x509Certificate, null, null, false);
        if (this.fecFirmaPresunta != null) {
            firma = new Firma("Firma", x509Certificate, this.fecFirmaPresunta, null, false);
        }
        firma.setIntegridad(jwsObject.verify(verifier));
        firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate(), this.configValidador.getProxy()));
        if (this.fecFirmaPresunta != null) {
            firma.setRevocacion(RevocacionHelper.verificar((X509Certificate) firma.getCertificate(), this.configValidador.getProxy(), firma.getFecFirma()));
        } else {
            firma.setRevocacion(null);
        }
        firmas.add(firma);
        return firmas;
    }
}
