package bo.firmadigital.jacobitus4.extendidos;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;

import bo.firmadigital.jacobitus.revocacion.CrlHelper;
import bo.firmadigital.jacobitus.validador.base.Opciones;
import bo.firmadigital.jacobitus.validador.comun.CadenaConfianzaHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;

/**
 *
 * @author ADSIB
 */
public class ValidadorExtendidoJws extends ValidadorExtendido {
    protected Opciones opciones = null;

    private Calendar fecFirmaPresunta = null;

    public ValidadorExtendidoJws(File file, Date fecFirmaPresunta, Opciones opciones) {
        this.opciones = opciones;
        try {
            super.file = file;
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(fecFirmaPresunta);
            this.fecFirmaPresunta = calendario;
            firmas = listarCertificados(new FileInputStream(file));
        } catch (Exception ignore) {
        }
    }

    public ValidadorExtendidoJws(InputStream is, Date fecFirmaPresunta, Opciones opciones) {
        this.opciones = opciones;
        try {
            Calendar calendario = Calendar.getInstance();
            calendario.setTime(fecFirmaPresunta);
            this.fecFirmaPresunta = calendario;
            this.firmas = listarCertificados(is);
        } catch (Exception ignore) {
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

    public final List<Firma> listarCertificados(InputStream is) throws Exception {
        X509Certificate cert;
        List<Firma> firmas = new ArrayList<>();
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        JWSObject jwsObject = JWSObject.parse(new String(is.readAllBytes()));
        InputStream in = new ByteArrayInputStream(jwsObject.getHeader().getX509CertChain().get(0).decode());
        cert = (X509Certificate)certFactory.generateCertificate(in);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey)cert.getPublicKey());

        Firma firma = new Firma("Firma", cert, null, null, false);
        if (this.fecFirmaPresunta != null) {
            firma = new Firma("Firma", cert, this.fecFirmaPresunta, null, false);
        }
        firma.setIntegridad(jwsObject.verify(verifier));
        firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate()));
        if (this.fecFirmaPresunta != null) {
            firma.setRevocacion(CrlHelper.verificar((X509Certificate) firma.getCertificate(), firma.getFecFirma(), this.opciones));
        } else {
            firma.setRevocacion(null);
        }
        firmas.add(firma);
        return firmas;
    }
}
