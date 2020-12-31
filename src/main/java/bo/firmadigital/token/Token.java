package bo.firmadigital.token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Clase que reprensetan Token (Dispositivo criptogr&aacute;fico).
 * 
 * Esta clase implementa las siguentes funcionalidades.
 * 1.- Iniciar sesi&oacute;n con un token.
 * 2.- Cerrar sesi&oacute;n con un token.
 * 3.- Modificar etiqueta de un token.
 * 4.- Modificar la clave (pin) de un token.
 * 5.- Modificar el identificador del par de claves.
 * 6.- Generar un par de claves.
 * 7.- Cargar un par de claves.
 * 8.- Cargar un certificado.
 * 9.- Eliminar claves.
 * 10.- Eliminar certificado.
 * 11.- Listar identificadores de todas las claves contenidas en un token.
 * 12.- Verificar si existen las claves y certificado para un identificador de claves
 * (identificador de claves).
 * 13.- Leer certificado.
 * 14.- Leer clave p&uacute;blica.
 * 15.- Leer clave privada.
 * 
 * Created by jcca on 11/28/16.
 * 
 */
public class Token {
    private static final String PKCS11_NOMBRE = "PKCS11";
    private String PIN;
    private KeyStore keystore;
    private Provider sunPKCS11;
    private final Slot slot;

    public Token(Slot slot) {
        this.slot = slot;
    }

    /**
     * Esta funci&oacute;n abre una sesi&oacute;n con el disposivito que
     * contenga las llaves y certificado x509.
     *
     * @param pin Clave del token.
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException
     */
    public void iniciar(String pin) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.PIN = pin;
        sunPKCS11 = Security.getProvider("SunPKCS11");
        sunPKCS11 = sunPKCS11.configure(slot.getConfiguracion());

        this.keystore = KeyStore.getInstance(PKCS11_NOMBRE, sunPKCS11);
        this.keystore.load(null, this.PIN.toCharArray());
        Security.addProvider(sunPKCS11);
    }

    /**
     * Esta funci&oacute;n cierra la conxi&oacute;n con el token.
     */
    public void salir() {
        if (this.sunPKCS11 != null) {
            this.slot.getP11().logout();
            Security.removeProvider(this.sunPKCS11.getName());

            this.PIN = null;
            this.sunPKCS11.clear();
            this.sunPKCS11 = null;
            System.gc();
        }
    }
    
    public String getProviderName() {
        return sunPKCS11.getName();
    }

    /**
     * Esta funci&oacute;n modifica la etiqueda de un token.
     *
     * @param etiqueta Nueva etiqueta del token.
     */
    public void modificarEtiqueda(String etiqueta) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Esta funci&oacute;n modifica la clave (PIN) del token.
     *
     * @param sopin Clave de seguridad del token.
     * @param pin Nueva clave del token.
     */
    public void modificarPin(String sopin, String pin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Esta funci&oacute;n modifica el identificador de un par de claves en el
     * token.
     *
     * @param clavesId Identificador del par de claves.
     * @param nuevoClavesId Nuevo identificador de par de claves.
     */
    public void modificarIdentificadorClaves(String clavesId, String nuevoClavesId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Esta funci&oacute;n generar un par de claves.
     *
     * @param clavesId Identificador para el nuevo par de claves.
     * @param pin Clave de seguridad del token
     * @param slotNumber Numero de slot
     * @return Retorna verdadero si el par de claves se ha generado falso en
     * caso contrario.
     */
    public PublicKey generarClaves(String clavesId, String pin, int slotNumber) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnsupportedEncodingException, KeyStoreException {
        if (existeCertificadoClaves(clavesId)) {
            return null;
        }

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", sunPKCS11);
        kpg.initialize(2048);
        KeyPair pair = kpg.generateKeyPair();

        // Generando nombres
        X500NameBuilder subjectBuilder = new X500NameBuilder();
        subjectBuilder.addRDN(BCStyle.CN, "Certificado Auto Firmado");
        
        // Generando certificado autofirmado
        X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(subjectBuilder.build(), new BigInteger("111111"), new Date(), new Date(), subjectBuilder.build(), SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded()));
        JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA256withRSA");
        ContentSigner signer;
        try {
            signer = builder.build(pair.getPrivate());
            byte[] certBytes = certBuilder.build(signer).getEncoded();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            keystore.setKeyEntry(clavesId, pair.getPrivate(), null,new Certificate[]{certificate});

            return pair.getPublic();
        } catch (OperatorCreationException | IOException | CertificateException ex) {
            throw new KeyStoreException(ex.getMessage());
        }
    }

    public String generarCSR(String alias, JSONArray subject) {
        try {
            PrivateKey privateKey = obtenerClavePrivada(alias);
            X509Certificate x509Certificate = obtenerCertificado(alias);
            X500NameBuilder nameBuilder = new X500NameBuilder();
            for (int i = 0; i < subject.length(); i++) {
                JSONObject o = subject.getJSONObject(i);
                
                ASN1ObjectIdentifier objectIdentifier = new ASN1ObjectIdentifier(o.getString("oid"));
                nameBuilder.addRDN(objectIdentifier, o.getString("value"));
            }
            
            PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(nameBuilder.build(), x509Certificate.getPublicKey());
            JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
            ContentSigner signer = csBuilder.build(privateKey);
            PKCS10CertificationRequest csr = p10Builder.build(signer);
            
            StringWriter w = new StringWriter();
            JcaPEMWriter p = new JcaPEMWriter(w);
            p.writeObject(csr);
            p.close();
            
            String csrResult = w.toString();
            csrResult = csrResult.replace("\r", "");
            return csrResult;
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | JSONException | OperatorCreationException | IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Esta funci&oacute;n eliminar el un par de claves.
     *
     * @param clavesId Identificador de las claves a eliminar.
     */
    public void eliminarClaves(String clavesId) throws KeyStoreException {
        keystore.deleteEntry(clavesId);
    }

    /**
     * Esta funci&oacute;n carga un par de claves a un token.
     *
     * @param priv Clave privada.
     * @param pub Clave p&uacute;blica.
     * @param clavesId Identificador para el nuevo par de claves.
     */
    public void cargarClaves(PrivateKey priv, PublicKey pub, String clavesId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Esta funci&oacute;n carga un certificado x509 a un token (Verificando
     * correspondencia de la clave p&uacute;blica).
     *
     * @param certificado Certificado x509 a cargar un token.
     * @param clavesId Identificador del par de claves a la cual corresponde el
     * certificado.
     * claves (ejemplo:
     * a3:f0:14:3d:77:29:2e:6b:cd:b1:4d:20:e4:a8:7a:2d:78:3b:95:b0).
     */
    public void cargarCertificado(X509Certificate certificado, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        certificado.checkValidity();

        Certificate[] chain = new Certificate[]{certificado};
        PrivateKey p = (PrivateKey)this.keystore.getKey(clavesId, null);
        this.keystore.setKeyEntry(clavesId, p, null, chain);
    }

    public void cargarCertificado(String pem, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
            cargarCertificado(cert, clavesId);
        } catch (CertificateException ex) {
            Logger.getLogger(Token.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Esta funci&oacute;n elimina un certificado de un token.
     *
     * @param clavesId Identificador del certificado a eliminar.
     */
    public void eliminarCertificado(String clavesId) throws KeyStoreException {
        this.keystore.deleteEntry(clavesId);
    }

    /**
     * Esta funci&oacute;n lista los identificadores de todas las llaves
     * almacenadas en el token.
     *
     * @return Retorna una lista de todos los identificadores de claves del Token.
     * @throws KeyStoreException
     */
    public List<String> listarIdentificadorClaves() throws KeyStoreException {
        List<String> claves = new ArrayList<>();
        Enumeration<String> aux = this.keystore.aliases();
        while (aux.hasMoreElements()) {
            String claveId = aux.nextElement();
            claves.add(claveId);
        }
        return claves;
    }

    public List<Certificate> listarCertificados() throws Exception {
        List<Certificate> certificados = new ArrayList<>();
        for(String id: listarIdentificadorClaves()) {
            certificados.add(obtenerCertificado(id));
        }        
        return certificados;
    }

    /**
     * Esta funci&oacute;n verifica si existe el par de claves y certificado
     * x509 para un identificador de clave determinado.
     *
     * @param clavesId Identificador de la clave y certificado a verificar.
     * @return Retorna verdadero si existe la clave y el certificado.
     * @throws KeyStoreException
     */
    public boolean existeCertificadoClaves(String clavesId) throws KeyStoreException {
        return this.keystore.containsAlias(clavesId) && this.keystore.isKeyEntry(clavesId);
    }
    
    

    /**
     * Esta funci&oacute;n retorna un certificado de un token.
     *
     * @param clavesId Identificador de certificado.
     * @return Retorna un certificado x509.
     * @throws KeyStoreException
     */
    public X509Certificate obtenerCertificado(String clavesId) throws KeyStoreException {
        return (X509Certificate) this.keystore.getCertificate(clavesId);
    }

    /**
     * Esta funci&oacute;n retorna la clave privada de un token.
     *
     * @param clavesId Identificador de la clave.
     * @return Retorna una clave privada.
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public PrivateKey obtenerClavePrivada(String clavesId) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException {
        PrivateKey privateKey = (PrivateKey) this.keystore.getKey(clavesId, null);
        return privateKey;
    }

    /**
     * Esta funci&oacute;n retorna la clave p&uacute;blica de un token.
     *
     * @param clavesId Identificador de la clave.
     * @return Retorna una clave p&uacute;blica.
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     * @throws KeyStoreException
     */
    public PublicKey obtenerClavePublica(String clavesId) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        PublicKey publicKey = ((PrivateKeyEntry) this.keystore.getEntry(clavesId, null)).getCertificate().getPublicKey();
        return publicKey;
    }

    public String nombreProveedor() {
        return sunPKCS11.getName();
    }
}
