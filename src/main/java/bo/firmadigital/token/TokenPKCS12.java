package bo.firmadigital.token;

import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
public class TokenPKCS12 implements Token {
    private String PIN;
    private KeyStore keystore;
    private final Slot slot;

    public TokenPKCS12(Slot slot) {
        this.slot = slot;
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @Override
    public void iniciar(String pin) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        this.PIN = pin;
        
        try {
            this.keystore = KeyStore.getInstance("PKCS12", "BC");
            this.keystore.load(new FileInputStream(slot.getConfiguracion()), pin.toCharArray());
        } catch (NoSuchProviderException ex) {
            throw new IOException(ex);
        }
    }

    public void crear(String pin) throws IOException {
        this.PIN = pin;
        
        try {
            this.keystore = KeyStore.getInstance("PKCS12", "BC");
            keystore.load(null, pin.toCharArray());
            generarClaves("ADSIB", pin, 0);
            this.keystore.store(new FileOutputStream(slot.getConfiguracion()), pin.toCharArray());
        } catch (NoSuchProviderException | InvalidAlgorithmParameterException | UnsupportedEncodingException | KeyStoreException | NoSuchAlgorithmException | CertificateException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Esta funci&oacute;n cierra la conxi&oacute;n con el token.
     */
    @Override
    public void salir() { }
    
    @Override
    public String getProviderName() {
        return "PKCS12";
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
    @Override
    public PublicKey generarClaves(String clavesId, String pin, int slotNumber) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnsupportedEncodingException, KeyStoreException {
        if (existeCertificadoClaves(clavesId)) {
            return null;
        }

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
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

    @Override
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
    @Override
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
    @Override
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
    @Override
    public void cargarCertificado(X509Certificate certificado, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        certificado.checkValidity();

        if (!this.keystore.getCertificate(clavesId).getPublicKey().equals(certificado.getPublicKey())) {
            throw new UnrecoverableKeyException("El certificado no corresponde a la clave privada seleccionada.");
        }

        Certificate[] chain = new Certificate[]{certificado};
        PrivateKey p = (PrivateKey)this.keystore.getKey(clavesId, null);
        this.keystore.setKeyEntry(clavesId, p, null, chain);
        try {
            this.keystore.store(new FileOutputStream(slot.getConfiguracion()), PIN.toCharArray());
        } catch (IOException | CertificateException ex) {
            throw new KeyStoreException(ex.getMessage());
        }
    }

    @Override
    public void cargarCertificado(String pem, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
            cargarCertificado(cert, clavesId);
        } catch (CertificateException ex) {
            Logger.getLogger(TokenPKCS12.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Esta funci&oacute;n elimina un certificado de un token.
     *
     * @param clavesId Identificador del certificado a eliminar.
     */
    @Override
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
    @Override
    public List<String> listarIdentificadorClaves() throws KeyStoreException {
        List<String> claves = new ArrayList<>();
        Enumeration<String> aux = this.keystore.aliases();
        while (aux.hasMoreElements()) {
            String claveId = aux.nextElement();
            claves.add(claveId);
        }
        return claves;
    }

    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public PublicKey obtenerClavePublica(String clavesId) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException {
        PublicKey publicKey = ((PrivateKeyEntry) this.keystore.getEntry(clavesId, null)).getCertificate().getPublicKey();
        return publicKey;
    }

    public static CK_TOKEN_INFO getTokenInfo(String path) {
        return new CK_TOKEN_INFO(-1, new SoftInfo(path));
    }

    public static class SoftInfo {
        public char[] label;
        public char[] manufacturerID;
        public char[] model;
        public char[] serialNumber;
        public long flags;
        public long ulMaxSessionCount;
        public long ulSessionCount;
        public long ulMaxRwSessionCount;
        public long ulRwSessionCount;
        public long ulMaxPinLen;
        public long ulMinPinLen;
        public long ulTotalPublicMemory;
        public long ulFreePublicMemory;
        public long ulTotalPrivateMemory;
        public long ulFreePrivateMemory;
        public Version hardwareVersion; 
        public Version firmwareVersion;
        public char[] utcTime;

        public SoftInfo(String path) {
            label = new File(path).getName().toCharArray();
            manufacturerID = "ADSIB".toCharArray();
            model = "1.0".toCharArray();
            serialNumber = "ADSIB".toCharArray();
            hardwareVersion = new Version();
            firmwareVersion = new Version();
            utcTime = "".toCharArray();
        }

        public class Version {
            public byte major;
            public byte minor;
        }
    }
}
