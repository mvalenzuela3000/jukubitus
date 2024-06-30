/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.jetty.localhost4637;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost4637.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus4.jetty.localhost4637.servicios.FirmadorServicio;

/**
 *
 * @author ADSIB
 */
@Path("/")
public class FirmadorRest {
    /**
     * @api {post} /sign Firma documentos pdf en bloque.
     * @apiGroup Sign
     * @apiVersion 1.0.0
     * 
     * @apiParam {Array} archivo El array de documentos PDF agrupados por id y pdf
     * @apiParam {String} archivo.base64 El documento PDF en Base64
     * @apiParam {String} archivo.name El identificado único para la solicitud
     * @apiParam {String} ci El número de documento de identidad.
     * @apiParam {Boolean} [software] Bandera para incluir (true) o excluir (false) los tokens por software.
     * 
     * @apiParamExample {json} Request-Example: 
     * {
     *     "archivo":[{
     *         "base64":"data:application/pdf;base64,JVBERi0xLjQKMSAwIG9iago8PAovVGl0b....",
     *         "name":"22949.pdf"
     *     }],
     *     "format":"pades",
     *     "language":"es",
     *     "ci":"6817702"
     * }
     */
    @POST
    @Path("/sign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String sign(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     boolean software = false;
        //     if (req.has("software")) {
        //         software = req.getBoolean("software");
        //     }
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot[] slots = gestorSlot.listarSlots(software);
        //     if (slots.length != 1) {
        //         throw new RuntimeException("Por favor conecte solo un token.");
        //     }
        //     TokenSelected dt;
        //     if (req.has("ci")) {
        //         if (req.getString("format").equals("jws")) {
        //             dt = App.serviceJWS(slots[0], req.getString("ci"), req.getJSONArray("archivo"));
        //         } else {
        //             dt = App.service(slots[0], req.getString("ci"), req.getJSONArray("archivo"));
        //         }
        //     } else {
        //         if (req.getString("format").equals("jws")) {
        //             dt = App.serviceJWS(slots[0], null, req.getJSONArray("archivo"));
        //         } else {
        //             dt = App.service(slots[0], null, req.getJSONArray("archivo"));
        //         }
        //     }
        //     if (dt.getAlias() != null && dt.getPin() != null) {
        //         json.put("files", dt.getFiles());
        //         return Response.ok(json.toString()).build();
        //     } else {
        //         json.put("message", "Se canceló la firma del documento");
        //         return Response.status(400).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
        //     }
        // } catch (JSONException | RuntimeException ex) {
        //     try {
        //         json.put("message", ex.getMessage());
        //     } catch (JSONException e) {
        //         Logger.getLogger(SignRest.class.getName()).log(Level.SEVERE, null, e);
        //     }
        //     return Response.status(500).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
        // }
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaModoSeguroDto objetoDto = om.readValue(body, FirmaModoSeguroDto.class);
            return om.writeValueAsString(servicio.firmarModoSeguro(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
