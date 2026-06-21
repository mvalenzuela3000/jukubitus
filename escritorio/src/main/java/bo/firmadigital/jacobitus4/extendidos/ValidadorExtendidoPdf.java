package bo.firmadigital.jacobitus4.extendidos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemReader;

import com.itextpdf.kernel.exceptions.BadPasswordException;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.ReaderProperties;
import com.itextpdf.signatures.PdfPKCS7;
import com.itextpdf.signatures.SignatureUtil;

import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.revocacion.CrlHelper;
import bo.firmadigital.jacobitus.utilidades.PdfHelper;
import bo.firmadigital.jacobitus.validador.base.ConfiguracionValidador;
import bo.firmadigital.jacobitus.validador.comun.CadenaConfianzaHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;

public class ValidadorExtendidoPdf extends ValidadorExtendido {
    protected String urlRespuesta = null;
    protected String tokenAutorizacion = null;

    protected ConfiguracionValidador configValidador = null;
    private String contrasenia = null;

    public ValidadorExtendidoPdf(File archivo, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        try {
            super.file = archivo;
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            try (InputStream is = new FileInputStream(archivo)) {
                firmas = listarCertificados(is);
            }
        } catch (Exception ignore) {
        }
    }

    public ValidadorExtendidoPdf(File archivo, String contrasenia, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        this.contrasenia = contrasenia;
        try {
            super.file = archivo;
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            try (InputStream is = new FileInputStream(archivo)) {
                firmas = listarCertificados(is);
            }
        } catch (Exception ignore) {
            System.out.println(ignore.getMessage());
        }
    }

    public ValidadorExtendidoPdf(File archivo, String urlRespuesta, String tokenAutorizacion, ConfiguracionValidador configValidador) {
        this(archivo, configValidador);
        this.urlRespuesta = urlRespuesta;
        this.tokenAutorizacion = tokenAutorizacion;
    }
    
    public ValidadorExtendidoPdf(InputStream is, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            firmas = listarCertificados(is);
        } catch (BadPasswordException ex) {
            if (this.contrasenia == null) {
                throw new JacobitusException("Documento protegido, se esperaba una contraseña.");
            } else {
                throw new JacobitusException("Contraseña inválida.");
            }
        } catch (Exception ignore) {
        }
    }

    public ValidadorExtendidoPdf(InputStream is, String contrasenia, ConfiguracionValidador configValidador) {
        this.configValidador = configValidador;
        this.contrasenia = contrasenia;
        try {
            if (Security.getProvider("BC") == null) {
                Security.addProvider(new BouncyCastleProvider());
            }
            firmas = listarCertificados(is);
        } catch (BadPasswordException ex) {
            if (this.contrasenia == null) {
                throw new JacobitusException("Documento protegido, se esperaba una contraseña.");
            } else {
                throw new JacobitusException("Contraseña inválida.");
            }
        } catch (Exception ignore) {
        }
    }

    @Override
    public boolean isRemoto() {
        return urlRespuesta != null;
    }

    @Override
    public String getUrlRespuesta() {
        return urlRespuesta;
    }

    @Override
    public String getTokenAutorizacion() {
        return tokenAutorizacion;
    }

    @Override
    public String getRevisionPath(String revision) {
        try {
            File out = new File(System.getProperty("java.io.tmpdir"), "documento.pdf");
            int c = 1;
            while (out.exists()) {
                out = new File(System.getProperty("java.io.tmpdir"), "documento" + c + ".pdf");
                c++;
            }
            PdfReader pdf = new PdfReader(file);
            PdfDocument pdfDocument = new PdfDocument(pdf);
            SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
            try (InputStream is = signatureUtil.extractRevision(revision);OutputStream os = new FileOutputStream(out)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
            return out.getAbsolutePath();
        } catch (IOException ex) {
            throw new JacobitusException(ex.getMessage());
        }
    }

    private boolean bloqueaDocumento(PdfArray referenceArray) {
        if (referenceArray == null || referenceArray.size() == 0) {
            return false;
        }
        for (PdfObject referenceObject : referenceArray) {
            if (referenceObject.isIndirectReference())
                referenceObject = ((PdfIndirectReference)referenceObject).getRefersTo(true);
            if (referenceObject.isIndirectReference()) {
                continue;
            }
            if (!referenceObject.isDictionary()) {
                continue;
            }
            PdfDictionary reference = (PdfDictionary) referenceObject;

            PdfName method = reference.getAsName(PdfName.TransformMethod);
            if (method == null) {
                continue;
            }
            if (new PdfName("UR").equals(method)) {
                continue;
            }
            if (!PdfName.DocMDP.equals(method) && !PdfName.FieldMDP.equals(method)) {
                continue;
            }

            PdfDictionary transformParams = reference.getAsDictionary(PdfName.TransformParams);
            if (transformParams == null) {
                continue;
            }

            PdfNumber p = transformParams.getAsNumber(PdfName.P);
            if (p != null) {
                return p.intValue() == 1;
            }
        }
        return false;
    }

    public List<Firma> listarCertificados(InputStream is) throws Exception {
        Certificate certificateTSA;
        try (InputStreamReader isr = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("timestamp.crt"))) {
            PemReader reader = new PemReader(isr);
            byte[] cert = reader.readPemObject().getContent();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            certificateTSA = certificateFactory.generateCertificate(new ByteArrayInputStream(cert));
            reader.close();
        }

        List<Firma> firmas = new ArrayList<>();
        PdfHelper pdfHelper;
        if (this.contrasenia == null) {
            pdfHelper = new PdfHelper(is);
        } else {
            pdfHelper = new PdfHelper(is, new ReaderProperties().setPassword(this.contrasenia.getBytes()));
        }
        PdfDocument pdfDocument = new PdfDocument(pdfHelper);
        SignatureUtil signatureUtil = new SignatureUtil(pdfDocument);
        List<String> snFirmas = signatureUtil.getSignatureNames();

        for (String nombre : snFirmas) {
            PdfDictionary dict = signatureUtil.getSignatureDictionary(nombre);
            PdfArray referenceArray = dict.getAsArray(PdfName.Reference);
            PdfPKCS7 pkcs7 = signatureUtil.readSignatureData(nombre);

            boolean tieneSelloTiempo = pkcs7.getTimeStampToken() != null;
            boolean selloTiempoValido = false;
            try {
                selloTiempoValido = pkcs7.getTimeStampToken().isSignatureValid(new JcaSimpleSignerInfoVerifierBuilder().build(certificateTSA.getPublicKey()));
            } catch (Exception e) {
                selloTiempoValido = false;
            }

            Firma firma;
            if (tieneSelloTiempo && selloTiempoValido) {
                firma = new Firma(nombre, pkcs7.getSigningCertificate(), pkcs7.getSignDate(), pkcs7.getTimeStampDate(), bloqueaDocumento(referenceArray));
            } else {
                firma = new Firma(nombre, pkcs7.getSigningCertificate(), pkcs7.getSignDate(), null, bloqueaDocumento(referenceArray));
            }
            firma.setIntegridad(pkcs7.verifySignatureIntegrityAndAuthenticity());
            firma.setObjetoPdf(pdfHelper.checkElementAdded(dict));
            firma.setCadenaConfianza(CadenaConfianzaHelper.validar(firma.getCertificate()));
            firma.setRevocacion(CrlHelper.verificar((X509Certificate) firma.getCertificate(), firma.getFecFirma(), this.configValidador));
            firmas.add(firma);
        }
        return firmas;
    }

    public boolean getBloqueado() {
        boolean res = false;
        try {
            for (Firma firma : firmas) {
                if (firma.getBloqueado()) {
                    res = true;
                    break;
                }
            }
        } catch (Exception ignore) {
            //
        }
        return res;
    }
}
