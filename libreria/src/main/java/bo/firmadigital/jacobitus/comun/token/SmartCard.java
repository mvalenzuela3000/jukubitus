/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.comun.token;

import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus.comun.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.firmador.Opciones;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
public class SmartCard {
    private static JSONObject token = null;
    public static final String ADSIB = "3b9f958131fe9f006646530501001171df000000000013";
    public static final String INDIA = "3b9f958131fe9f006646530501001171df000006000015";

    static {
        if (OS.isUnix()) {
            if (new File("/usr/lib/x86_64-linux-gnu/libpcsclite.so.1").exists()) {
                System.setProperty("sun.security.smartcardio.library", "/usr/lib/x86_64-linux-gnu/libpcsclite.so.1");
            }
        } else if (OS.isMac()) {
            if (OS.version().equals("10.16")) {
                System.setProperty("sun.security.smartcardio.library", "/System/Library/Frameworks/PCSC.framework/Versions/Current/PCSC");
            }
        }
    }

    public static List<JSONObject> cards(Opciones opciones) {
        LinkedList<JSONObject> res = new LinkedList();
        try {
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
            List<CardTerminal> terminals = factory.terminals().list();
            for (CardTerminal terminal : terminals) {
                Card card = terminal.connect("*");
                JSONObject token = new JSONObject();
                token.put("name", obtenerNombreToken(terminal.getName()));
                token.put("id", hex(card.getATR().getBytes()));
                if (!res.contains(token)) {
                    res.add(token);
                    if (SmartCard.token == null && (token.getString("id").equals(ADSIB) || token.getString("id").equals(INDIA))) {
                        SmartCard.token = token;
                    }
                }
            }
        } catch (NoSuchAlgorithmException | CardException | JSONException ex) {
            if (opciones.getControlador() != null) {
                try {
                    GestorSlot gs = GestorSlot.getInstance();
                    Slot[] slots = gs.listarSlots(opciones);
                    for (Slot slot : slots) {
                        CK_TOKEN_INFO info = slot.detalleToken();
                        token = new JSONObject();
                        String name = new String(info.model).trim();
                        if (name.equals("ePass2003")) {
                            name = "FT ePass2003Auto";
                        }
                        token.put("name", name);
                        if (!res.contains(token)) {
                            res.add(token);
                        }
                    }
                } catch (JSONException ex2) {
                    Logger.getLogger(SmartCard.class.getName()).log(Level.SEVERE, null, ex2);
                }
            } else {
                if (ex.getMessage().equals("list() failed") || ex.getMessage().equals("connect() failed")) {
                    if (res.isEmpty() && token != null) {
                        try {
                            GestorSlot gs = GestorSlot.getInstance();
                            Provider sunPKCS11 = Security.getProvider("SunPKCS11");
                            sunPKCS11 = sunPKCS11.configure(gs.obtenerConfiguracion(token.getString("id")));
                            KeyStore.getInstance("PKCS11", sunPKCS11);
                            res.add(token);
                        } catch (JSONException ex2) {
                            throw new RuntimeException(ex2.getMessage());
                        } catch (KeyStoreException ignore) {}
                    }
                }
            }
            Logger.getLogger(SmartCard.class.getName()).log(Level.SEVERE, null, ex);
        }
        return res;
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }

    private static String obtenerNombreToken(String token) {
        StringBuilder builder = new StringBuilder();
        StringTokenizer tokenizer = new StringTokenizer(token, " ");
        if (tokenizer.hasMoreElements()) {
            builder.append(tokenizer.nextElement());
        }
        builder.append(" ");
        if (tokenizer.hasMoreElements()) {
            builder.append(tokenizer.nextElement());
        }
        return builder.toString();
    }
}
