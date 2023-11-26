/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources9000;

import bo.firmadigital.jacobitus4.App;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
@Path("/app")
public class AppRest {
    @POST
    @Path("/show")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String show(String body) {
        try {
            JSONObject json = new JSONObject(body);
            if (json.has("url")) {
                String url = json.getString("url"), token = null, urlpost = null;
                if (json.has("token")) {
                    token = json.getString("token");
                }
                if (json.has("urlpost")) {
                    urlpost = json.getString("urlpost");
                }
                App.show(url, token, urlpost);
            } else {
                if (json.has("file")) {
                    File file = new File(json.getString("file"));
                    App.show(file);
                } else if (json.has("error")) {
                    App.show(json.getString("error"));
                } else {
                    App.show();
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AppRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "{}";
    }
}
