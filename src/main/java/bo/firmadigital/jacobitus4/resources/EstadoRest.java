/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
@Path("/status")
public class EstadoRest {
    /**
     * @api {get} /api/status Verifica el estado del servicio.
     * @apiGroup Estado
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "compilacion": 3100,
     *         "api_version": "1.0.2"
     *     },
     *     "finalizado": true,
     *     "mensaje": "Servicio ejecutandose correctamente"
     * }
     */
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
        JSONObject json = new JSONObject();
        String r = "{}";
        try {
            JSONObject datos = new JSONObject();
            datos.put("compilacion", 3100);
            datos.put("api_version", "1.0.2");
            json.put("datos", datos);
            json.put("finalizado", true);
            json.put("mensaje", "Servicio ejecutandose correctamente");
            r = json.toString();
        } catch (JSONException ex) {
            Logger.getLogger(EstadoRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return r;
    }
}
