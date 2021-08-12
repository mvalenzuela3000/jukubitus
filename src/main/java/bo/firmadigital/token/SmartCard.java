/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.token;

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

    public static List<JSONObject> cards() {
        LinkedList<JSONObject> res = new LinkedList();
        try {
            TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
            List<CardTerminal> terminals = factory.terminals().list();
            for (CardTerminal terminal : terminals) {
                Card card = terminal.connect("*");
                token = new JSONObject();
                token.put("name", obtenerNombreToken(terminal.getName()));
                token.put("id", hex(card.getATR().getBytes()));
                if (!res.contains(token)) {
                    res.add(token);
                }
            }
        } catch (NoSuchAlgorithmException | CardException | JSONException ex) {
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
            } else {
                Logger.getLogger(SmartCard.class.getName()).log(Level.SEVERE, null, ex);
            }
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
