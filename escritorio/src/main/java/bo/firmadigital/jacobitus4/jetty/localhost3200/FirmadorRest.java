/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.jetty.localhost3200;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.FirmaPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost3200.servicios.FirmadorServicio;

/**
 *
 * @author ADSIB
 */
@Path("/")
public class FirmadorRest {
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        // return "Ok";
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.inicio();
    }

    @GET
    @Path("/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tokens() throws JsonGenerationException, JsonMappingException, IOException {
        // JSONObject json = new JSONObject();
        // try {
        //     try {
        //         JSONArray datos = new JSONArray();
        //         List<JSONObject> tokens = SmartCard.cards(this.getOpcionesFirmador());
        //         for (JSONObject token : tokens) {
        //             datos.put(token.get("name"));
        //         }
        //         json.put("datos", datos);
        //         json.put("finalizado", true);
        //         json.put("mensaje", "Tokens detectados");
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.tokens());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String start(@QueryParam("pin") String pin) {
        // JSONObject json = new JSONObject();
        // try {
        //     if (pin == null) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", "El pin es nulo. Utilice el metodo setParametrosConexion(ruta,pin)");
        //     } else {
        //         try {
        //             if (slots != null) {
        //                 if (slots.length == 1) {
        //                     slots[0].getToken().salir();
        //                 }
        //                 slots = null;
        //             }
        //             GestorSlot gestorSlot = GestorSlot.getInstance();
        //             gestorSlot.setOpciones(this.getOpcionesFirmador());
        //             slots = gestorSlot.listarSlots();
        //             if (slots.length == 1) {
        //                 slots[0].getToken().iniciar(pin);
        //                 json.put("finalizado", true);
        //                 json.put("mensaje", "Autenticacion correcta");
        //             } else {
        //                 json.put("finalizado", false);
        //                 if (slots.length > 1) {
        //                     json.put("mensaje", "Se encontró más de un token conectado.");
        //                 } else {
        //                     json.put("mensaje", "No se encontró ningún token conectado.");
        //                 }
        //                 slots = null;
        //             }
        //         } catch (RuntimeException | GeneralSecurityException ex) {
        //             json.put("finalizado", false);
        //             json.put("mensaje", ex.getMessage());
        //         }
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.start(pin));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/certs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String certs() {
        // JSONObject json = new JSONObject();
        // try {
        //     try {
        //         if (slots == null) {
        //             json.put("finalizado", false);
        //             json.put("mensaje", "Primero debe iniciar sesión.");
        //         } else {
        //             if (slots.length == 1) {
        //                 SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        //                 IToken token = slots[0].getToken();
        //                 List<String> labels = token.listarIdentificadorClaves();
        //                 JSONArray datos = new JSONArray();
        //                 for (String label : labels) {
        //                     DatosCertificado entry = new DatosCertificado(label, token.obtenerCertificado(label));
        //                     JSONObject cert = new JSONObject();
        //                     cert.put("esFirmaBolivia", Validador.verificarPKI(entry.getCert()));
        //                     cert.put("numeroSerie", entry.getCert().getSerialNumber());
        //                     cert.put("nombreComunIssuer", entry.getNombreComunIssuer());
        //                     cert.put("organizacionIssuer", entry.getOrganizacionIssuer());
        //                     cert.put("nombreComunSubject", entry.getNombreComunSubject());
        //                     cert.put("ci", entry.getNumeroDocumentoSubject());
        //                     cert.put("complemento", entry.getComplementoSubject());
        //                     cert.put("organizacionSubject", entry.getOrganizacionSubject());
        //                     cert.put("unidadOrganizacionalSubject", entry.getUnidadOrganizacionalSubject());
        //                     cert.put("inicioValidez", dateFormat.format(entry.getInicioValidez()));
        //                     cert.put("finValidez", dateFormat.format(entry.getFinValidez()));
        //                     cert.put("alias", label);
        //                     cert.put("esValido", entry.getInicioValidez().compareTo(new Date()) < 0 && entry.getFinValidez().compareTo(new Date()) > 0);
        //                     Validador.OCSPState state = Validador.verificarOcsp(entry.getCert(), new Date(), this.getOpcionesValidador()).getState();
        //                     if (state == Validador.OCSPState.OK) {
        //                         cert.put("OCSP", "no revocado");
        //                     } else {
        //                         cert.put("OCSP", state.toString());
        //                     }
        //                     datos.put(cert);
        //                 }
        //                 json.put("datos", datos);
        //                 json.put("finalizado", true);
        //                 json.put("mensaje", "Certificados obtenidos correctamente");
        //             } else {
        //                 json.put("finalizado", false);
        //                 if (slots.length > 1) {
        //                     json.put("mensaje", "Se encontró más de un token conectado.");
        //                 } else {
        //                     json.put("mensaje", "Por favor no desconecte el token.");
        //                 }
        //             }
        //         }
        //     } catch (RuntimeException | GeneralSecurityException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.certs());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/sign")
    @Produces(MediaType.APPLICATION_JSON)
    public String sign(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     try {
        //         if (slots == null) {
        //             json.put("finalizado", false);
        //             json.put("mensaje", "Primero debe iniciar sesión.");
        //         } else {
        //             if (slots.length == 1) {
        //                 JSONObject req = new JSONObject(body);
        //                 if (req.has("nombre_archivo") && req.has("alias") && req.has("pdf_base64")) {
        //                     SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        //                     IToken token = slots[0].getToken();
        //                     X509Certificate certificate = token.obtenerCertificado(req.getString("alias"));
        //                     if (certificate == null) {
        //                         json.put("finalizado", false);
        //                         json.put("mensaje", "No se encontró un certificado con el alias solicitado.");
        //                     } else {
        //                         DatosCertificado entry = new DatosCertificado(req.getString("alias"), certificate);
        //                         JSONObject cert = new JSONObject();
        //                         cert.put("esFirmaBolivia", Validador.verificarPKI(entry.getCert()));
        //                         cert.put("numeroSerie", entry.getCert().getSerialNumber());
        //                         cert.put("nombreComunIssuer", entry.getNombreComunIssuer());
        //                         cert.put("organizacionIssuer", entry.getOrganizacionIssuer());
        //                         cert.put("nombreComunSubject", entry.getNombreComunSubject());
        //                         cert.put("ci", entry.getNumeroDocumentoSubject());
        //                         cert.put("complemento", entry.getComplementoSubject());
        //                         cert.put("organizacionSubject", entry.getOrganizacionSubject());
        //                         cert.put("unidadOrganizacionalSubject", entry.getUnidadOrganizacionalSubject());
        //                         cert.put("inicioValidez", dateFormat.format(entry.getInicioValidez()));
        //                         cert.put("finValidez", dateFormat.format(entry.getFinValidez()));
        //                         cert.put("esValido", entry.getInicioValidez().compareTo(new Date()) < 0 && entry.getFinValidez().compareTo(new Date()) > 0);
        //                         Validador.OCSPState state = Validador.verificarOcsp(entry.getCert(), new Date(), this.getOpcionesValidador()).getState();
        //                         if (state == Validador.OCSPState.OK) {
        //                             cert.put("OCSP", "no revocado");
        //                         } else {
        //                             cert.put("OCSP", state.toString());
        //                         }
        //                         JSONObject datos = new JSONObject();
        //                         byte[] pdf = Base64.getDecoder().decode(req.getString("pdf_base64"));
        //                         ByteArrayOutputStream os = new ByteArrayOutputStream();
        //                         FirmadorPdf.firmar(new ByteArrayInputStream(pdf), os, false, token, req.getString("alias"));
        //                         datos.put("pdf_base64", Base64.getEncoder().encodeToString(os.toByteArray()));
        //                         datos.put("nombre_archivo", req.getString("nombre_archivo"));
        //                         datos.put("certificado", cert);
        //                         json.put("datos", datos);
        //                         json.put("finalizado", true);
        //                         json.put("mensaje", "Certificados obtenidos correctamente");
        //                     }
        //                 } else {
        //                     json.put("finalizado", false);
        //                     json.put("mensaje", "Parámetros obligatorios (nombre_archivo, alias, pdf_base64).");
        //                 }
        //             } else {
        //                 json.put("finalizado", false);
        //                 if (slots.length > 1) {
        //                     json.put("mensaje", "Se encontró más de un token conectado.");
        //                 } else {
        //                     json.put("mensaje", "Por favor no desconecte el token.");
        //                 }
        //             }
        //         }
        //     } catch (RuntimeException | GeneralSecurityException | IOException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaPdfDto objetoDto = om.readValue(body, FirmaPdfDto.class);
            return om.writeValueAsString(servicio.sign(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String finish() {
        // JSONObject json = new JSONObject();
        // try {
        //     if (slots == null) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", "No se encontró una sesión activa.");
        //     } else {
        //         try {
        //             if (slots.length == 1) {
        //                 slots[0].getToken().salir();
        //                 slots = null;
        //                 json.put("finalizado", true);
        //                 json.put("mensaje", "Cerrado sesión correctamente");
        //             } else {
        //                 json.put("finalizado", false);
        //                 if (slots.length > 1) {
        //                     json.put("mensaje", "Se encontró más de un token conectado.");
        //                 } else {
        //                     json.put("mensaje", "No se encontró ningún token conectado.");
        //                 }
        //             }
        //         } catch (RuntimeException ex) {
        //             json.put("finalizado", false);
        //             json.put("mensaje", ex.getMessage());
        //         }
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.finish());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/estadoToken")
    @Produces(MediaType.TEXT_HTML)
    public String estadoToken() {
        // JSONObject json = new JSONObject();
        // try {
        //     json.put("finalizado", true);
        //     json.put("mensaje", "False");
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.estadoToken());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/matarProceso")
    @Produces(MediaType.TEXT_HTML)
    public String matarProceso() {
        // return "Ok";
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.matarProceso();
    }

    @GET
    @Path("/reset")
    @Produces(MediaType.TEXT_HTML)
    public String reset() {
        // if (slots != null) {
        //     if (slots.length == 1) {
        //         slots[0].getToken().salir();
        //     }
        //     slots = null;
        // }
        // return "Ok";
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.reset();
    }
}
