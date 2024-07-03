/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.jetty.localhost9000;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.TokenServicio;

/**
 *
 * @author ADSIB
 */
@Path("/token")
public class TokenRest {
    /**
     * @api {get} /api/token/status Verifica el estado de la consola SmartCard.
     * @apiGroup Token
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "connected": true,
     *         "tokens": [
     *             "FT ePass2003Auto"
     *         ]
     *     },
     *     "finalizado": true,
     *     "mensaje": "Lista de Tokens obtenida"
     * }
     */
    @GET
    @Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
        // JSONObject json = new JSONObject();
        // try {
        //     try {
        //         JSONObject datos = new JSONObject();
        //         JSONArray jsonArray = new JSONArray();
        //         List<JSONObject> tokens = SmartCard.cards(this.getOpciones());
        //         for (JSONObject token : tokens) {
        //             jsonArray.put(token.get("name"));
        //         }
        //         datos.put("connected", jsonArray.length() > 0);
        //         datos.put("tokens", jsonArray);
        //         json.put("datos", datos);
        //         json.put("finalizado", true);
        //         json.put("mensaje", "Lista de Tokens obtenida");
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.status());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    /**
     * @api {get} /api/token/connected Obtiene información de los tokens conectados.
     * @apiGroup Token
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "connected": true,
     *         "tokens": [
     *             {
     *                 "slot": 1,
     *                 "serial": "203531650003002A",
     *                 "name": "Feitian Technologies Co., Ltd",
     *                 "model": "ePass2003"
     *             }
     *         ]
     *     },
     *     "finalizado": true,
     *     "mensaje": "Lista de Tokens obtenida"
     * }
     */
    @GET
    @Path("/connected")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String connected() {
        // JSONObject json = new JSONObject();
        // try {
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     try {
        //         Slot[] slots = gestorSlot.listarSlots();
        //         json.put("datos", new JSONObject());
        //         ((JSONObject)json.get("datos")).put("connected", slots.length > 0);
        //         ((JSONObject)json.get("datos")).put("tokens", new JSONArray());
        //         for (Slot slot : slots) {
        //             CK_TOKEN_INFO info = slot.detalleToken();
        //             JSONObject token = new JSONObject();
        //             token.put("slot", slot.getSlotID());
        //             token.put("serial", new String(info.serialNumber).trim());
        //             token.put("name", new String(info.manufacturerID).trim());
        //             token.put("model", new String(info.model).trim());
        //             ((JSONArray)((JSONObject)json.get("datos")).get("tokens")).put(token);
        //         }
        //         json.put("finalizado", true);
        //         if (slots.length > 0) {
        //             json.put("mensaje", "Lista de Tokens obtenida");
        //         } else {
        //             json.put("mensaje", "No se detecto nigun token conectado");
        //         }
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.connected());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    /**
     * @api {post} /api/token/data Obtiene información de los certificados en el token conectado al slot.
     * @apiGroup Token
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     * 
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678"
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "data_token": {
     *             "certificates": 1,
     *             "data": [
     *                 {
     *                     "tipo": "PRIMARY_KEY",
     *                     "tipo_desc": "Clave Privada",
     *                     "alias": "355409121073",
     *                     "id": "355409121073",
     *                     "tiene_certificado": true
     *                 },
     *                 {
     *                     "tipo": "X509_CERTIFICATE",
     *                     "tipo_desc": "Certificado",
     *                     "adsib": false,
     *                     "serialNumber": "27cbd28f79876b40",
     *                     "alias": "355409121073",
     *                     "id": "355409121073",
     *                     "pem": "-----BEGIN CERTIFICATE-----\nMII...truncated...==\n-----END CERTIFICATE-----",
     *                     "validez": {
     *                         "desde": "2021-01-29 11:21:19",
     *                         "hasta": "2022-01-29 11:21:19"
     *                     },
     *                     "titular": {
     *                         "dnQualifier": "CI",
     *                         "CN": "Juan Perez",
     *                         "OU": "Gerencia",
     *                         "O": "Perez S.A.",
     *                         "uidNumber": "12345678"
     *                     },
     *                     "emisor": {
     *                         "CN": "ADSIB",
     *                         "O": "ADSIB"
     *                     }
     *                 }
     *             ],
     *             "private_keys": 1
     *         }
     *     },
     *     "finalizado": true,
     *     "mensaje": "Datos de token obtenidos correctamente"
     * }
     */
    @POST
    @Path("/data")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String data(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        //     CertificateFactory fact = CertificateFactory.getInstance("X.509");
        //     InputStream is = getClass().getClassLoader().getResourceAsStream("firmadigital_bo.crt");
        //     List<X509Certificate> intermediates = (List<X509Certificate>) fact.generateCertificates(is);
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot[] slots = gestorSlot.listarSlots();
        //     if (slots.length == 1 && !req.has("slot")) {
        //         req.put("slot", slots[0].getSlotID());
        //     }
        //     if (req.has("slot") && req.has("pin")) {
        //         Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
        //         IToken token = slot.getToken();
        //         json.put("datos", new JSONObject());
        //         try {
        //             token.iniciar((String)req.get("pin"));
        //             json.put("finalizado", true);
        //             json.put("mensaje", "Datos de token obtenidos correctamente");
        //             List<String> llaves = token.listarIdentificadorClaves();
        //             JSONObject data_token = new JSONObject();
        //             ((JSONObject)json.get("datos")).put("data_token", data_token);
        //             data_token.put("certificates", llaves.size());
        //             data_token.put("data", new JSONArray());
        //             for (int i = 0; i < llaves.size(); i++) {
        //                 JSONObject key = new JSONObject();
        //                 key.put("tipo", "PRIMARY_KEY");
        //                 key.put("tipo_desc", "Clave Privada");
        //                 key.put("alias", llaves.get(i));
        //                 key.put("id", llaves.get(i));
        //                 X509Certificate cert = token.obtenerCertificado(llaves.get(i));
        //                 DatosCertificado datos = new DatosCertificado(cert);
        //                 key.put("tiene_certificado", cert != null);
        //                 ((JSONArray)data_token.get("data")).put(key);
        //                 if (key.getBoolean("tiene_certificado")) {
        //                     JSONObject x509 = new JSONObject();
        //                     x509.put("tipo", "X509_CERTIFICATE");
        //                     x509.put("tipo_desc", "Certificado");
        //                     x509.put("adsib", false);
        //                     for (X509Certificate intermediate : intermediates) {
        //                         try {
        //                             cert.verify(intermediate.getPublicKey());
        //                             x509.put("adsib", true);
        //                             break;
        //                         } catch (GeneralSecurityException ex) {
        //                         }
        //                     }
        //                     x509.put("serialNumber", cert.getSerialNumber().toString(16));
        //                     x509.put("alias", llaves.get(i));
        //                     x509.put("id", llaves.get(i));
        //                     String pem = "-----BEGIN CERTIFICATE-----\n";
        //                     pem += Base64.getEncoder().encodeToString(cert.getEncoded());
        //                     pem += "\n-----END CERTIFICATE-----";
        //                     x509.put("pem", pem);
        //                     x509.put("validez", new JSONObject());
        //                     ((JSONObject)x509.get("validez")).put("desde", dateFormat.format(datos.getInicioValidez()));
        //                     ((JSONObject)x509.get("validez")).put("hasta", dateFormat.format(datos.getFinValidez()));
        //                     x509.put("titular", new JSONObject());
        //                     ((JSONObject)x509.get("titular")).put("dnQualifier", datos.getTipoDocumentoSubject());
        //                     ((JSONObject)x509.get("titular")).put("uidNumber", datos.getNumeroDocumentoSubject());
        //                     ((JSONObject)x509.get("titular")).put("UID", datos.getComplementoSubject());
        //                     ((JSONObject)x509.get("titular")).put("CN", datos.getNombreComunSubject());
        //                     ((JSONObject)x509.get("titular")).put("T", datos.getCargoSubject());
        //                     ((JSONObject)x509.get("titular")).put("O", datos.getOrganizacionSubject());
        //                     ((JSONObject)x509.get("titular")).put("OU", datos.getUnidadOrganizacionalSubject());
        //                     ((JSONObject)x509.get("titular")).put("EmailAddress", datos.getCorreoSubject());
        //                     ((JSONObject)x509.get("titular")).put("description", datos.getDescripcionSubject());
        //                     x509.put("common_name", datos.getNombreComunSubject());
        //                     x509.put("emisor", new JSONObject());
        //                     ((JSONObject)x509.get("emisor")).put("CN", datos.getNombreComunIssuer());
        //                     ((JSONObject)x509.get("emisor")).put("O", datos.getOrganizacionIssuer());
        //                     ((JSONArray)data_token.get("data")).put(x509);
        //                 }
        //             }
        //             data_token.put("private_keys", llaves.size());
        //         } catch (GeneralSecurityException ex) {
        //             json.put("finalizado", false);
        //             json.put("mensaje", ex.getMessage());
        //         }
        //         token.salir();
        //     } else {
        //         json.put("finalizado", false);
        //         json.put("mensaje", "Datos requeridos slot y pin.");
        //     }
        // } catch (JSONException | CertificateException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = om.readValue(body, TokenAutenticacionDto.class);
            return om.writeValueAsString(servicio.data(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
