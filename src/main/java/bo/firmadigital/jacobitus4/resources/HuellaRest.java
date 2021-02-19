/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import bo.firmadigital.fingerprint.Capturar;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
@Path("/huella")
public class HuellaRest {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    @GET
    @Path("/capturar")
    @Produces(MediaType.APPLICATION_JSON)
    public void capturar(@Suspended final AsyncResponse response) {
        JSONObject json = new JSONObject();
        try {
            if (OS.contains("win")) {
                Capturar.capturar((byte[] image) -> {
                    try {
                        JSONObject datos = new JSONObject();
                        datos.put("image", Base64.getEncoder().encodeToString(image));
                        datos.put("wsq", Base64.getEncoder().encodeToString(Capturar.toWSQ(image)));
                        json.put("datos", datos);
                        json.put("finalizado", true);
                        json.put("mensaje", "Huella capturada");
                        response.resume(json.toString());
                    } catch (JSONException ex) {
                        Logger.getLogger(EstadoRest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            } else {
                Capturar.capturarLinux((byte[] image) -> {
                    try {
                        JSONObject datos = new JSONObject();
                        datos.put("image", Base64.getEncoder().encodeToString(image));
                        datos.put("wsq", Base64.getEncoder().encodeToString(Capturar.toWSQ(image)));
                        json.put("datos", datos);
                        json.put("finalizado", true);
                        json.put("mensaje", "Huella capturada");
                        response.resume(json.toString());
                    } catch (JSONException ex) {
                        Logger.getLogger(EstadoRest.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
            }
        } catch (RuntimeException e) {
            try {
                json.put("datos", JSONObject.NULL);
                json.put("finalizado", false);
                json.put("mensaje", e.getMessage());
            } catch (JSONException ex) {
                Logger.getLogger(EstadoRest.class.getName()).log(Level.SEVERE, null, ex);
            }
            response.resume(json.toString());
        }
    }
}
