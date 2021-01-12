package bo.firmadigital.token;

import bo.firmadigital.pkcs11.PKCS11;
import java.io.*;
import java.security.Provider;
import java.security.Security;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Clase que gestiona los Token conectados.
 * Esta clase implementa las siguientes funcionalidades.
 * 1.- Identificar el driver del Token.
 * 2.- Listar slots (Espacio de conexi&oacute;n de un token que es representado
 * por un n&uacute;mero entero Ejemplo: 0, 2, 6).
 * 
 * @author ADSIB
 */
public class GestorSlot {
    private static GestorSlot ourInstance;
    private Provider sunPKCS11;
    private String libreria;
    private final Map<Long, Slot> slots;
    private static final String OS = System.getProperty("os.name").toLowerCase();

    private GestorSlot() {
        slots = new HashMap<>();
        sunPKCS11 = Security.getProvider("SunPKCS11");
    }

    public static GestorSlot getInstance() {
        if (ourInstance == null) {
            ourInstance = new GestorSlot();
        }
        return ourInstance;
    }

    /**
     * Esta funci&oacute;n establece la ruta de la libreria a partir del identificador
     * 
     * @param id Identificador del controlador del token
     * @return Path de la libreria
     */
    public String getLib(String id) {
        BufferedReader br;
        if (OS.contains("win")) {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tokens.windows")));
        } else if (OS.contains("nux")) {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tokens.linux")));
        } else if (OS.contains("mac")) {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tokens.macos")));
        } else {
            throw new RuntimeException("Sistema operativo incompatible.");
        }
        String line = null;
        try {
            while ((line = br.readLine()) != null) {
                String[] datos = line.split(" ");
                if (id.equals(datos[0])) {
                    line = datos[1];
                    if (!new File(line).exists()) {
                        if (datos.length == 3) {
                            throw new RuntimeException(datos[2]);
                        } else {
                            if (System.getProperty("os.arch").equals("x86")) {
                                throw new RuntimeException(datos[2]);
                            } else {
                                throw new RuntimeException(datos[3]);
                            }
                        }
                    }
                    break;
                }
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(GestorSlot.class.getName()).log(Level.SEVERE, null, ex);
        }
        return line;
    }

    /**
     * Esta funci&oacute;n lista los Slots de los token disponibles.
     *
     * @return Retorna la lista de Slot de todos los token disponibles.
     */
    public Slot[] listarSlots() {
        slots.clear();
        try {
            List<JSONObject> tokens = SmartCard.cards();
            if (tokens.isEmpty() && libreria == null) {
                throw new RuntimeException("No se encontro ningun token conectado.");
            }
            if (tokens.size() > 1) {
                throw new RuntimeException("Tokens de diferentes marcas conectados.");
            }
            if (!tokens.isEmpty()) {
                libreria = getLib(tokens.get(0).getString("id"));
            }
            sunPKCS11 = sunPKCS11.configure(obtenerConfiguracion("token", null));
            Security.addProvider(sunPKCS11);
            PKCS11 p11 = new PKCS11(sunPKCS11);
            long[] lista = p11.C_GetSlotList(true);
            for (long id : lista) {
                slots.put(id, new Slot(id, p11, obtenerConfiguracion("token", id)));
            }
        } catch (JSONException | IOException ex) {
            Logger.getLogger(GestorSlot.class.getName()).log(Level.SEVERE, null, ex);
        }

        return slots.values().toArray(new Slot[0]);
    }

    /**
     * Esta funci&oacute;n retorna un Slot dado el id un token.
     *
     * @param slotID Id de slot de que representa el puerto donde se encuentra
     * conectado el token.
     * @return Retorna un objeto Slot.
     */
    public Slot obtenerSlot(long slotID) {
        return slots.get(slotID);
    }

    /**
     * Esta funci&oacute;n retorna una configuraci&oacute;n para conectar a un
     * dispositivo PKCS #11.
     *
     * @param nombre Nombre de la configuración.
     * @param slotID Id de slot de que representa el puerto donde se encuentra
     * conectado el token.
     * @return Retorna la configuraci&oacute;n de token.
     */
    private String obtenerConfiguracion(String nombre, Long slotID) {
        if (libreria == null) {
            throw new RuntimeException("No se encontro ningún token conectado.");
        }
        try {
            String configString = "name = " + nombre + slotID
                + "\nlibrary = " + libreria;
            if (slotID != null) {
                configString += "\nslot = " + slotID;
                configString += "\nattributes(*,*,*) = {\nCKA_TOKEN = true\n}";
                configString += "\ndisabledMechanisms = {\nCKM_SHA1_RSA_PKCS\n}";
            }
            File filePkcs11Config = File.createTempFile("fido_pkcs11_", ".cfg");
            try (FileOutputStream fos = new FileOutputStream(filePkcs11Config)) {
                fos.write(configString.getBytes());
                fos.flush();
            }
            return filePkcs11Config.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
