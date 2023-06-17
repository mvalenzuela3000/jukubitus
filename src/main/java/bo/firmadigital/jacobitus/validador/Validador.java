/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
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
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.bouncycastle.x509.extension.X509ExtensionUtil;

/**
 *
 * @author ADSIB
 */
public abstract class Validador implements Iterable<CertDate> {
    protected List<CertDate> certificados;
    protected File file;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public boolean isRemoto() {
        return false;
    }

    public String getPost() {
        throw new UnsupportedOperationException("Not supported in " + this.getClass() + ".");
    }

    public String getToken() {
        throw new UnsupportedOperationException("Not supported in " + this.getClass() + ".");
    }

    public String getPath() {
        StringBuilder res = new StringBuilder(file.getPath());
        for (CertDate cert : certificados) {
            if (cert.isOk()) {
                res.append("\n\t✔ ");
            } else {
                if (cert.getOCSP().getState() == OCSPState.CONNECTION) {
                    res.append("\n\t✘? ");
                } else {
                    res.append("\n\t✘ ");
                }
            }
            res.append(cert.getDatos().getNombreComunSubject());
        }
        return res.toString();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getRevisionPath(String revision) {
        throw new RuntimeException("No implementado.");
    }

    public void export(File f) {
        throw new RuntimeException("No implementado.");
    }

    public String exportB64(InputStream is) {
        throw new RuntimeException("No implementado.");
    }

    public File getFile() {
        return file;
    }

    public static boolean verificarPKI(Certificate cert) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream is = Validador.class.getClassLoader().getResourceAsStream("firmadigital_bo.crt");
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

    public static OCSPData verificarOcsp(X509Certificate cert, Date signDate, Opciones opciones) {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            URL[] urls = getCrlURLs(cert);
            if (urls.length == 0) {
                return new OCSPData(OCSPState.UNKNOWN_SERVER, null);
            }
            HttpURLConnection connection;
            if (opciones != null && opciones.getProxyHablitado()) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(opciones.getServidorProxy(), opciones.getPuertoServidorProxy()));
                connection = (HttpURLConnection) urls[0].openConnection(proxy);
            } else {
                connection = (HttpURLConnection) urls[0].openConnection();
            }
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
                return new OCSPData(OCSPState.UNKNOWN, null);
            }
            X509CRLEntry entry = crl.getRevokedCertificate(cert.getSerialNumber());
            if (entry == null) {
                return new OCSPData(OCSPState.OK, null);
            }
            if (entry.getRevocationDate().compareTo(signDate) > 0) {
                return new OCSPData(OCSPState.ALERT, entry.getRevocationDate());
            } else {
                return new OCSPData(OCSPState.REVOKED, entry.getRevocationDate());
            }
        } catch (CertificateException | IOException | CRLException ex) {
            return new OCSPData(OCSPState.CONNECTION, null);
        }
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

    @Override
    public Iterator<CertDate> iterator() {
        return certificados.iterator();
    }

    public enum OCSPState {
        OK,
        REVOKED,
        UNKNOWN,
        ALERT,
        CONNECTION,
        UNKNOWN_SERVER
    }
}
