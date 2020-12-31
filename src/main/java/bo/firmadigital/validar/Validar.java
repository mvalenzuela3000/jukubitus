package bo.firmadigital.validar;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Validar implements Iterable<CertDate> {
    private List<CertDate> certificados;
    private File file;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public Validar(File file) {
        try {
            this.file = file;
            certificados = listarCertificados(file);
        } catch (Exception ignore) {
        }
    }

    public String getPath() {
        StringBuilder res = new StringBuilder(file.getPath());
        for (CertDate cert : certificados) {
            if (cert.isOk()) {
                res.append("\n\t✔ ");
            } else {
                res.append("\n\t✘ ");
            }
            res.append(cert.getDatos().getNombreComunSubject());
        }
        return res.toString();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public boolean verificarPKI(Certificate cert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream is = getClass().getClassLoader().getResourceAsStream("firmadigital_bo.crt");
            PemReader pemReader = new PemReader(new InputStreamReader(is));
            List<X509Certificate> intermediates = new LinkedList<>();
            PemObject x509Data;
            while ((x509Data = pemReader.readPemObject()) != null) {
                intermediates.add((X509Certificate) cf.generateCertificate(new ByteArrayInputStream(x509Data.getContent())));
            }
            for (int i = 0; i < intermediates.size(); i++) {
                X500Name x500Name = new JcaX509CertificateHolder(intermediates.get(i)).getSubject();
                String cn = IETFUtils.valueToString(x500Name.getRDNs(new ASN1ObjectIdentifier("2.5.4.3"))[0].getFirst().getValue());
                if (cn.equals("Entidad Certificadora Publica ADSIB") || cn.equals("Entidad Certificadora Autorizada Digicert")) {
                    try {
                        cert.verify(intermediates.get(i).getPublicKey());
                        return true;
                    } catch (GeneralSecurityException ignore) {
                    }
                }
            }
            return false;
        } catch (GeneralSecurityException | IOException ex) {
            return false;
        }
    }

    public boolean verificarOcsp(X509Certificate cert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            URL[] urls = getCrlURLs(cert);
            if (urls.length == 0) {
                return false;
            }
            urls[0] = new URL(urls[0].toString().replace("http://", "https://"));
            HttpURLConnection connection = (HttpURLConnection) urls[0].openConnection();
            InputStream responseStream;
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                    connection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();
            }
            StringBuilder stringBuilder;
            try (BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream))) {
                String line;
                stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }
            responseStream.close();
            connection.disconnect();
            X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(stringBuilder.toString().getBytes()));
            if (crl == null) {
                return false;
            }
            X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
            return entry == null;
        } catch (CertificateException | IOException | CRLException ignore) {
        }
        return false;
    }

    public static URL[] getCrlURLs(X509Certificate cert) {
        List<URL> urls = new LinkedList<>();
        // Obtiene la extensión ASN1 2.5.29.31
        byte[] cdp = cert.getExtensionValue("2.5.29.31");
        if (cdp != null) {
            try {
                // Mapela los datos planos en una clase
                CRLDistPoint crldp = CRLDistPoint.getInstance(X509ExtensionUtil.fromExtensionValue(cdp));
                DistributionPoint[] distPoints = crldp.getDistributionPoints();

                for (DistributionPoint dp : distPoints) {
                    GeneralNames gns = (GeneralNames) dp.getDistributionPoint().getName();
                    DERIA5String uri;
                    for (GeneralName name : gns.getNames()) {
                        // Identifica si es una URL
                        if (name.getTagNo() == GeneralName.uniformResourceIdentifier) {
                            uri = (DERIA5String) name.getName();
                            urls.add(new URL(uri.getString()));
                        }
                    }
                }
            } catch (IOException ignore) {
            }
        }
        return (URL[]) urls.toArray(new URL[urls.size()]);
    }

    private boolean bloqueaDocumento(PdfArray referenceArray) {
        if (referenceArray == null || referenceArray.size() == 0) {
            return false;
        }
        for (PdfObject referenceObject : referenceArray) {
            if (referenceObject.isIndirect())
                referenceObject = ((PdfIndirectReference)referenceObject).getIndRef();
            if (referenceObject.isIndirect()) {
                continue;
            }
            if (!referenceObject.isDictionary()) {
                continue;
            }
            PdfDictionary reference = (PdfDictionary) referenceObject;

            PdfName method = reference.getAsName(PdfName.TRANSFORMMETHOD);
            if (method == null) {
                continue;
            }
            if (new PdfName("UR").equals(method)) {
                continue;
            }
            if (!PdfName.DOCMDP.equals(method) && !PdfName.FIELDMDP.equals(method)) {
                continue;
            }

            PdfDictionary transformParams = reference.getAsDict(PdfName.TRANSFORMPARAMS);
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

    public List<CertDate> listarCertificados(File file) throws Exception {
        Certificate certificateTSA;
        try (InputStreamReader isr = new InputStreamReader(getClass().getClassLoader().getResourceAsStream("timestamp.crt"))) {
            PemReader reader = new PemReader(isr);
            byte[] cert = reader.readPemObject().getContent();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X509");
            certificateTSA = certificateFactory.generateCertificate(new ByteArrayInputStream(cert));
            reader.close();
        }

        List<CertDate> certs = new ArrayList<>();
        InputStream is = new FileInputStream(file);
        PdfReader pdf = new PdfReader(is);
        AcroFields acroFields = pdf.getAcroFields();
        List<String> firmas = acroFields.getSignatureNames();

        for (String nombre : firmas) {
            PdfArray referenceArray = acroFields.getFieldItem(nombre).getWidget(0).getAsDict(PdfName.V).getAsArray(PdfName.REFERENCE);
            PdfPKCS7 pkcs7 = acroFields.verifySignature(nombre);

            CertDate certDate;
            if (pkcs7.getTimeStampToken() != null && pkcs7.getTimeStampToken().isSignatureValid(new JcaSimpleSignerInfoVerifierBuilder().build(certificateTSA.getPublicKey()))) {
                certDate = new CertDate(pkcs7.getSigningCertificate(), pkcs7.getSignDate(), pkcs7.getTimeStampDate(), bloqueaDocumento(referenceArray));
            } else {
                certDate = new CertDate(pkcs7.getSigningCertificate(), pkcs7.getSignDate(), null, bloqueaDocumento(referenceArray));
            }
            certDate.setValid(pkcs7.verify());
            certDate.setPKI(verificarPKI(certDate.getCertificate()));
            certDate.setOCSP(verificarOcsp((X509Certificate) certDate.getCertificate()));
            certs.add(certDate);
        }
        return certs;
    }

    public boolean isBloquea() {
        boolean res = false;
        try {
            for (CertDate cert : certificados) {
                if (cert.isBloquea()) {
                    res = true;
                    break;
                }
            }
        } catch (Exception ignore) {
        }
        return res;
    }

    @Override
    public Iterator<CertDate> iterator() {
        return certificados.iterator();
    }
}
