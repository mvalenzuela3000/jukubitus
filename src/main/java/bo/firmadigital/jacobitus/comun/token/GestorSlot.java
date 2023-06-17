package bo.firmadigital.jacobitus.comun.token;

import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus.comun.pkcs11.PKCS11;
import java.io.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
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
    private static final String ARCH = System.getProperty("os.arch");

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
        if (OS.isWindows()) {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tokens.windows")));
        } else if (OS.isUnix()) {
            br = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("tokens.linux")));
        } else if (OS.isMac()) {
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
                    if (new File(line).exists()) {
                        String hash;
                        if (ARCH.equals("x86")) {
                            hash = datos[3];
                        } else {
                            hash = datos[2];
                        }
                        if (!hash.equals("0")) {
                            try {
                                String hashFile = MD5Checksum.getMD5Checksum(line);
                                if (!hashFile.equalsIgnoreCase(hash)) {
                                    if (datos.length == 5) {
                                        throw new RuntimeException(datos[4]);
                                    } else {
                                        if (ARCH.equals("x86")) {
                                            throw new RuntimeException(datos[4]);
                                        } else {
                                            throw new RuntimeException(datos[5]);
                                        }
                                    }
                                }
                            } catch (NoSuchAlgorithmException ex) {
                                Logger.getLogger(GestorSlot.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } else {
                        if (datos.length == 5) {
                            throw new RuntimeException(datos[4]);
                        } else {
                            if (ARCH.equals("x86")) {
                                throw new RuntimeException(datos[4]);
                            } else {
                                throw new RuntimeException(datos[5]);
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
     * @param software Bandera para incluir (true) o excluir (false) los tokens por software.
     * @return Retorna la lista de Slot de todos los token disponibles.
     */
    public synchronized Slot[] listarSlots(boolean software) {
        slots.clear();
        try {
            Config config = Config.getInstance();
            if (config.getDriver() == null) {
                List<JSONObject> tokens = SmartCard.cards();
                if (tokens.isEmpty() && libreria == null && config.getToken() == null && !config.isHsmEnabled()) {
                    throw new RuntimeException("No se encontro ningun token conectado.");
                }
                if (tokens.size() > 1) {
                    Map<String, String> librerias = new TreeMap<>();
                    for (int i = 0; i < tokens.size(); i++) {
                        if (tokens.get(i).getString("id").equals(SmartCard.ADSIB)) {
                            librerias.put(SmartCard.ADSIB, getLib(SmartCard.ADSIB));
                        }
                        if (tokens.get(i).getString("id").equals(SmartCard.INDIA)) {
                            librerias.put(SmartCard.INDIA, getLib(SmartCard.INDIA));
                        }
                    }
                    if (librerias.size() == 1) {
                        libreria = librerias.get(librerias.keySet().iterator().next());
                    } else {
                        throw new RuntimeException("Tokens de diferentes marcas conectados.");
                    }
                }
                if (tokens.size() == 1) {
                    libreria = getLib(tokens.get(0).getString("id"));
                }
            } else {
                libreria = config.getDriver().getPath();
            }
            if (libreria != null) {
                sunPKCS11 = sunPKCS11.configure(obtenerConfiguracion("token", null, null));
                Security.addProvider(sunPKCS11);
                PKCS11 p11 = new PKCS11(sunPKCS11);
                long[] lista = p11.C_GetSlotList(true);
                for (long id : lista) {
                    slots.put(id, new Slot(id, p11, obtenerConfiguracion("token", id, null)));
                }
                p11.logout();
                Security.removeProvider(sunPKCS11.getName());
                sunPKCS11.clear();
            }
            if (software && config.getToken() != null) {
                slots.put(-1l, new Slot(config.getToken().getPath()));
            }
            if (config.isHsmEnabled() && config.getHsmJWT() != null) {
                slots.put(-1001l, new Slot(-1001));
            }
        } catch (JSONException | IOException ex) {
            Logger.getLogger(GestorSlot.class.getName()).log(Level.SEVERE, null, ex);
        }

        return slots.values().toArray(new Slot[0]);
    }

    /**
     * Esta funci&oacute;n lista los Slots de los token disponibles.
     *
     * @return Retorna la lista de Slot de todos los token disponibles.
     */
    public synchronized Slot[] listarSlots() {
        return listarSlots(true);
    }

    /**
     * Esta funci&oacute;n retorna un Slot dado el id un token.
     *
     * @param slotID Id de slot de que representa el puerto donde se encuentra
     * conectado el token.
     * @return Retorna un objeto Slot.
     */
    public synchronized Slot obtenerSlot(long slotID) {
        if (!slots.containsKey(slotID)) {
            listarSlots();
        }
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
    private String obtenerConfiguracion(String nombre, Long slotID, String label) {
        if (libreria == null) {
            throw new RuntimeException("No se encontro ningún token conectado.");
        }
        try {
            String configString = "name = " + nombre + slotID
                + "\nlibrary = " + libreria;
            if (slotID != null) {
                configString += "\nslot = " + slotID;
                if (label == null) {
                    configString += "\nattributes(*,*,*) = {\nCKA_TOKEN = true\n}";
                } else {
                    BigInteger etiqueta = new BigInteger(1, label.getBytes("UTF-8"));
                    configString += "\nattributes(*,*,*) = {\nCKA_TOKEN = true\nCKA_LABEL = " + String.format("0h%040x", etiqueta) + "\n}";
                }
                configString += "\ndisabledMechanisms = {\nCKM_SHA1_RSA_PKCS\n}";
            }
            File filePkcs11Config = File.createTempFile("fido_pkcs11_", ".cfg");
            filePkcs11Config.deleteOnExit();
            try (FileOutputStream fos = new FileOutputStream(filePkcs11Config)) {
                fos.write(configString.getBytes());
                fos.flush();
            }
            return filePkcs11Config.getAbsolutePath();
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Esta funci&oacute;n retorna una configuraci&oacute;n para conectar a un
     * dispositivo PKCS #11.
     * @param id Identificador del modelo de token.
     * @return Retorna el archivo de configuración del token.
     */
    public String obtenerConfiguracion(String id) {
        libreria = getLib(id);
        return obtenerConfiguracion("token", null, null);
    }

    /**
     * Esta funci&oacute;n retorna una configuraci&oacute;n para conectar a un
     * dispositivo PKCS #11.
     * @param id Identificador del modelo de token.
     * @param label Etiqueta con la cual se creara la clave privada.
     * @return Retorna el archivo de configuración del token.
     */
    public String obtenerConfiguracionPK(Long id, String label) {
        return obtenerConfiguracion("tokenpk", id, label);
    }
}

