package bo.firmadigital.token;

import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.pkcs11.PKCS11;
import java.io.IOException;

/**
 * Clase que representa el puerto cuando un token se conecta.
 * 
 * Esta clase implementa las siguentes funcionalidades.
 * 1.- Leer informaci&oacute;n del slot (identificador, etiqueta, modelo,
 * marca).
 * 
 * @author ADSIB
 */
public class Slot {

    private final PKCS11 p11;
    private final String configuracion;
    private final long slotID;
    private Token token;

    /**
     * Constructor de la clase.
     *
     * @param slotID Id de slot de que representa el puerto donde se encuentra
     * conectado el token.
     * @param p11 Instancia PKCS #11
     * @param configuracion configuraci&oacute;n del token.
     * @throws IOException
     */
    public Slot(long slotID, PKCS11 p11, String configuracion) throws IOException {
        this.slotID = slotID;
        this.p11 = p11;
        this.configuracion = configuracion;
    }

    /**
     * Esta funci&oacute;n retorna el id de Slot a la que representa esta clase.
     *
     * @return Retorna el ID de slot.
     */
    public long getSlotID() {
        return slotID;
    }

    public PKCS11 getP11() {
        return p11;
    }

    public String getConfiguracion() {
        return configuracion;
    }

    /**
     * Esta funci&oacute;n retorna un objeto Token.
     *
     * @return Retorna una instancia de un Token.
     */
    public Token getToken() {
        if (token == null) {
            token = new Token(this);
        }

        return token;
    }

    /**
     * Esta funci&oacute;n recupera informaci&oacute;n del token (identificador,
     * etiqueta, modelo, marca).
     *
     * @return Retorna Informaci&oacute;n del token conectado.
     */
    public CK_TOKEN_INFO detalleToken() {
        return this.p11.C_GetTokenInfo(this.slotID);
    }

}
