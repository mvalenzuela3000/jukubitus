/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
public class Main {
    // public static Server jettyServer = new Server();

    public static void main(String[] args) {
        Request req = new Request();
        if (req.estado()) {
            if (args.length == 1) {
                req.show(args[0]);
            } else {
                req.show();
            }
        } else {
            try {
                WebServer.iniciar();
                if (args.length == 1) {
                    String[] parts = args[0].split("\\?");
                    if (parts.length == 2) {
                        JSONObject body = Request.splitQuery(parts[1]);
                        App.run(true, true, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                    } else {
                        App.run(true, true, false, args[0]);
                    }
                } else {
                    App.run(true, true, false);
                }
            } catch (Exception ex) {
                try {
                    WebServer.detener();
                } catch (Exception ex2) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
                App.run(false, false, false);
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}
