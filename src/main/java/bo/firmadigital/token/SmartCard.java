/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.token;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
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
    public static List<JSONObject> cards() {
        LinkedList<JSONObject> res = new LinkedList();
        try {
            TerminalFactory factory = TerminalFactory.getDefault();
            List<CardTerminal> terminals = factory.terminals().list();
            for (CardTerminal terminal : terminals) {
                Card card = terminal.connect("*");
                JSONObject token = new JSONObject();
                token.put("name", obtenerNombreToken(terminal.getName()));
                token.put("id", hex(card.getATR().getBytes()));
                if (!res.contains(token)) {
                    res.add(token);
                }
            }
        } catch (CardException | JSONException ex) {
            if (!ex.getMessage().equals("list() failed") && !ex.getMessage().equals("connect() failed")) {
                throw new RuntimeException(ex.getMessage());
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
