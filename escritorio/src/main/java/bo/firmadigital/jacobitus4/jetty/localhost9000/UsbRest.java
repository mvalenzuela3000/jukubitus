/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.jetty.localhost9000;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.UsbServicio;

/**
 *
 * @author ADSIB
 */
@Path("/usbdisk")
public class UsbRest {
    @GET
    @Path("/serial")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String capturar() {
        // JSONObject json = new JSONObject();
        // try {
        //     if (OS.isUnix()) {
        //         Process p = Runtime.getRuntime().exec("lsblk --nodeps -o name,serial,type,tran");
        //         JSONArray jsonArrayUsb = new JSONArray();
        //         BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        //         String linea = null;
        //         while ((linea = in.readLine()) != null) {
        //             StringTokenizer stoken = new StringTokenizer(linea);
        //             if (stoken.countTokens() >= 4 && !linea.replace(" ", "").equals("NAMESERIALTYPETRAN")) {
        //                 JSONObject jsonUsb = new JSONObject();
        //                 int ind = 0;
        //                 while (stoken.hasMoreTokens()) {
        //                     String tk = stoken.nextToken();
        //                     switch (ind) {
        //                         case 0:
        //                             jsonUsb.put("name", tk);
        //                             break;
        //                         case 1:
        //                             jsonUsb.put("serial", tk);
        //                             break;
        //                         case 2:
        //                             jsonUsb.put("type", tk);
        //                             break;
        //                         case 3:
        //                             jsonUsb.put("tran", tk);
        //                             break;
        //                     }
        //                     ind++;
        //                 }
        //                 if (jsonUsb.get("type").equals("disk") && jsonUsb.get("tran").equals("usb")) {
        //                     jsonArrayUsb.put(jsonUsb);
        //                 }
        //             }
        //         }
        //         in.close();
        //         JSONObject datos = new JSONObject();
        //         datos.put("usbs", jsonArrayUsb);
        //         json.put("datos", datos);
        //     }
        //     json.put("finalizado", true);
        //     json.put("mensaje", "Operación finalizada correctamente...");
        //     response.resume(json.toString());
        // } catch (IOException | JSONException e) {
        //     try {
        //         json.put("datos", JSONObject.NULL);
        //         json.put("finalizado", false);
        //         json.put("mensaje", e.getMessage());
        //     } catch (JSONException ex) {
        //         Logger.getLogger(EstadoRest.class.getName()).log(Level.SEVERE, null, ex);
        //     }
        //     response.resume(json.toString());
        // }
        UsbServicio servicio = new UsbServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.serial());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
