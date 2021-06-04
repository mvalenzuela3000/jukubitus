package bo.firmadigital.token;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;

/**
 * Clase que reprensetan Token.
 * 
 */
public interface Token {
    /**
     * Inicia session
     * @param pin Clave de acceso para iniciar sessión
     * @throws KeyStoreException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws IOException 
     */
    public void iniciar(String pin) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException;

    /**
     * Cierra la session.
     */
    public void salir();
    
    /**
     * Recupera el nombre del proveedor
     * @return Nombre del proveedor
     */
    public String getProviderName();

    /**
     * Genera para de claves
     * @param clavesId Label para identificar el par de claves
     * @param pin Clave de acceso
     * @param slotNumber Slot al cual se encuentra conectado el token
     * @return Clave publica
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     * @throws UnsupportedEncodingException
     * @throws KeyStoreException 
     */
    public PublicKey generarClaves(String clavesId, String pin, int slotNumber) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, UnsupportedEncodingException, KeyStoreException;

    /**
     * Genera un CSR 
     * @param alias Label para identificar el par de claves
     * @param subject Datos del sujeto
     * @return CSR en formato PEM
     */
    public String generarCSR(String alias, JSONArray subject);

    /**
     * Elimina par de claves
     * @param clavesId Label para identificar el par de claves
     * @throws KeyStoreException 
     */
    public void eliminarClaves(String clavesId) throws KeyStoreException;

    /**
     * Carga un par de claves generado externamente
     * @param priv Clave privada
     * @param pub Clave publica
     * @param clavesId Label para identificar el par de claves
     */
    public void cargarClaves(PrivateKey priv, PublicKey pub, String clavesId);

    /**
     * Carga el certificado correspondiente a la clave privada
     * @param certificado Certificado asociado a la clave privada
     * @param clavesId Label para identificar el par de claves
     * @throws CertificateNotYetValidException
     * @throws CertificateExpiredException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException 
     */
    public void cargarCertificado(X509Certificate certificado, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException;

    /**
     * Carga el certificado correspondiente a la clave privada en formato PEM
     * @param pem Certificado asociado a la clave privada en formato PEM
     * @param clavesId Label para identificar el par de claves
     * @throws CertificateNotYetValidException
     * @throws CertificateExpiredException
     * @throws UnrecoverableKeyException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException 
     */
    public void cargarCertificado(String pem, String clavesId) throws CertificateNotYetValidException, CertificateExpiredException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException;

    /**
     * Elimina el certificado
     * @param clavesId Label para identificar el par de claves
     * @throws KeyStoreException 
     */
    public void eliminarCertificado(String clavesId) throws KeyStoreException;

    /**
     * Lista los label de las claves almacenadas en el token
     * @return Lista de etiquetas
     * @throws KeyStoreException 
     */
    public List<String> listarIdentificadorClaves() throws KeyStoreException;

    /**
     * Lista los certiifcados almacenados en el token
     * @return Lista de certificados
     * @throws Exception 
     */
    public List<Certificate> listarCertificados() throws Exception;

    /**
     * Verifica la existencia de un certificado o clave con el label especificado
     * @param clavesId Label para identificar el par de claves
     * @return verdadero en caso de encontrar coincidencia
     * @throws KeyStoreException 
     */
    public boolean existeCertificadoClaves(String clavesId) throws KeyStoreException;
    
    

    /**
     * Recupera el certificado a partir del label asociado
     * @param clavesId Label para identificar el par de claves
     * @return Certificado
     * @throws KeyStoreException 
     */
    public X509Certificate obtenerCertificado(String clavesId) throws KeyStoreException;

    /**
     * Recupera la clave privada (O el acceso a la misma en el token)
     * @param clavesId Label para identificar el par de claves
     * @return Clave privada
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException 
     */
    public PrivateKey obtenerClavePrivada(String clavesId) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableEntryException;

    /**
     * Recupera la calve publica
     * @param clavesId Label para identificar el par de claves
     * @return Clave publica
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableEntryException
     * @throws KeyStoreException 
     */
    public PublicKey obtenerClavePublica(String clavesId) throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException;
}
