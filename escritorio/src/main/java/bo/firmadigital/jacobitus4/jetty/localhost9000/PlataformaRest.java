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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenChangePinDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCreacionCsrDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCreacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenPemDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenSolicitudDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.PlataformaServicio;

/**
 *
 * @author ADSIB
 */
@Path("/token")
public class PlataformaRest {
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String create(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     json.put("finalizado", false);
        //     JSONObject req = new JSONObject(body);
        //     if (req.getString("pin").length() < 8) {
        //         json.put("mensaje", "El pin es muy corto.");
        //     } else {
        //         int num = 0, may = 0, minu = 0;
        //         char[] password = req.getString("pin").toCharArray();
        //         for (int i = 0; i < req.getString("pin").length(); i++) {
        //             if (password[i] >= '0' && password[i] <= '9') {
        //                 num++;
        //             } else if (password[i] >= 'A' && password[i] <= 'Z') {
        //                 may++;
        //             } else if (password[i] >= 'a' && password[i] <= 'z') {
        //                 minu++;
        //             }
        //         }
        //         if (num < 1 || may < 1 || minu < 1) {
        //             json.put("mensaje", "El pin debe contener al menos un número, una letra mayúscula y una letra minúscula.");
        //         } else {
        //             Config config = Config.getInstance();
        //             Slot slot = new Slot(config.getTokenToCreate(), this.getOpciones());
        //             TokenPKCS12 token = new TokenPKCS12(getOpciones(), slot);
        //             try {
        //                 token.crear(req.getString("pin"));
        //                 json.put("finalizado", true);
        //                 json.put("mensaje", "Token generado correctamente.");
        //             } catch (GeneralSecurityException ex) {
        //                 json.put("mensaje", ex.getMessage());
        //             }
        //         }
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenCreacionDto objetoDto = om.readValue(body, TokenCreacionDto.class);
            return om.writeValueAsString(servicio.create(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/generate_keypar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_keypar(@QueryParam("pin") String pin, @QueryParam("slot") Integer slotNumber) {
        // JSONObject json = new JSONObject();
        // try {
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(slotNumber);
        //     IToken token = slot.getToken();
        //     token.iniciar(pin);
        //     BigInteger max = new BigInteger("1000000000000");
        //     BigInteger id = new BigInteger(max.bitLength(), new SecureRandom()).mod(max);
        //     token.generarClaves(id.toString(), pin, slotNumber);
        //     JSONObject datos = new JSONObject();
        //     json.put("datos", datos);
        //     JSONObject data_token = new JSONObject();
        //     datos.put("data_token", data_token);
        //     data_token.put("certificates", 0);
        //     JSONArray data = new JSONArray();
        //     data_token.put("data", data);
        //     JSONObject pk = new JSONObject();
        //     data.put(pk);
        //     pk.put("tipo", "PRIMARY_KEY");
        //     pk.put("tipo_desc", "Clave Privada");
        //     pk.put("alias", id.toString());
        //     pk.put("tiene_certificado", false);
        //     data_token.put("private_keys", 1);
        //     json.put("finalizado", true);
        //     json.put("mensaje", "Se genero el par de claves correctamente.");
        //     token.salir();
        // } catch (GeneralSecurityException | JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = new TokenAutenticacionDto();
            objetoDto.setPin(pin);
            objetoDto.setSlot(Long.parseLong(slotNumber.toString()));
            return om.writeValueAsString(servicio.generateKeypar(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/generate_csr")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_csr(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
        //     IToken token = slot.getToken();
        //     JSONObject datos = new JSONObject();
        //     json.put("datos", datos);
        //     try {
        //         token.iniciar(req.getString("pin"));
        //         datos.put("csr", token.generarCSR(req.getString("alias_certificado"), req.getJSONArray("subject")));
        //         json.put("finalizado", true);
        //         json.put("mensaje", "Se genero el CSR correctamente");
        //     } catch (GeneralSecurityException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        //     token.salir();
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenCreacionCsrDto objetoDto = om.readValue(body, TokenCreacionCsrDto.class);
            return om.writeValueAsString(servicio.generateCsr(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cargar_pem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cargar_pem(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
        //     IToken token = slot.getToken();
        //     json.put("datos", new JSONObject());
        //     try {
        //         token.iniciar(req.getString("pin"));
        //         token.cargarCertificado(new String(Base64.getDecoder().decode(req.getString("pem")), "UTF-8"), req.getString("id"));
        //         json.put("finalizado", true);
        //         json.put("mensaje", "El certificado fue adicionado correctamente");
        //     } catch (GeneralSecurityException | UnsupportedEncodingException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        //     token.salir();
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenPemDto objetoDto = om.readValue(body, TokenPemDto.class);
            return om.writeValueAsString(servicio.cargarPem(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cambiar_pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cambiar_pin(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
        //     try {
        //         IToken token = slot.getToken();
        //         token.modificarPin(req.getString("old_pin"), req.getString("new_pin"));
        //         json.put("finalizado", true);
        //         json.put("mensaje", "El pin se cambió correctamente");
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenChangePinDto objetoDto = om.readValue(body, TokenChangePinDto.class);
            return om.writeValueAsString(servicio.cambiarPin(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cambiar_pin_so")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cambiar_pin_so(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
        //     try {
        //         IToken token = slot.getToken();
        //         token.modificarPinSo(req.getString("old_pin"), req.getString("new_pin"));
        //         json.put("finalizado", true);
        //         json.put("mensaje", "El pin se cambió correctamente");
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenChangePinDto objetoDto = om.readValue(body, TokenChangePinDto.class);
            return om.writeValueAsString(servicio.cambiarPinSO(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/test_pin_so")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String test_pin_so(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
        //     try {
        //         IToken token = slot.getToken();
        //         token.test(req.getString("pin"));
        //         json.put("finalizado", true);
        //         json.put("mensaje", "El pin se validó correctamente");
        //     } catch (RuntimeException ex) {
        //         json.put("finalizado", false);
        //         json.put("mensaje", ex.getMessage());
        //     }
        // } catch (JSONException ex) {
        //     Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = om.readValue(body, TokenAutenticacionDto.class);
            return om.writeValueAsString(servicio.testPinSO(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
    
    @POST
    @Path("/firmar_solicitudes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarSolicitudes(String body) {
        // JSONObject json = new JSONObject();
        // try {
        //     JSONObject req = new JSONObject(body);
        //     GestorSlot gestorSlot = GestorSlot.getInstance();
        //     gestorSlot.setOpciones(this.getOpciones());
        //     gestorSlot.listarSlots();
        //     Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
        //     IToken token = slot.getToken();
        //     token.iniciar(req.getString("pin"));
        //     JSONArray data = req.getJSONArray("data");
        //     JSONArray datos = new JSONArray();
        //     for (int i = 0; i < data.length(); i++) {
        //         JSONObject element = new JSONObject();
        //         element.put("id", data.getJSONObject(i).getString("id"));
        //         PrivateKey pk = token.obtenerClavePrivada(req.getString("alias"));
        //         if (pk == null) {
        //             token.salir();
        //             throw new KeyStoreException("No se encontró la clave con alias: " + req.getString("alias"));
        //         }
        //         JWSSigner jwsSigner = new RSASSASigner(pk);
        //         JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
        //         if (data.getJSONObject(i).isNull("url")) {
        //             builder.x509CertURL(new URI("https://agencia.firmadigital.bo/services_ar/certificado?serial_number=" + token.obtenerCertificado(req.getString("alias")).getSerialNumber()));
        //         } else {
        //             if (data.getJSONObject(i).getString("url").contains("?")) {
        //                 builder.x509CertURL(new URI(data.getJSONObject(i).getString("url")));
        //             } else {
        //                 builder.x509CertURL(new URI(data.getJSONObject(i).getString("url") + "?serial_number=" + token.obtenerCertificado(req.getString("alias")).getSerialNumber()));
        //             }
        //         }
        //         JWSObject jwsObject = new JWSObject(builder.build(),new Payload(data.getJSONObject(i).getString("payload")));
        //         jwsObject.sign(jwsSigner);
        //         element.put("jws", jwsObject.serialize());
        //         datos.put(element);
        //     }
        //     token.salir();
        //     json.put("datos", datos);
        //     json.put("finalizado", true);
        //     json.put("mensaje", "Se firmo las solicitudes correctamente!");
        // } catch (JSONException | GeneralSecurityException | URISyntaxException | JOSEException ex) {
        //     try {
        //         String mensaje = ex.getMessage();
        //         if (ex.getCause() instanceof IOException) {
        //             if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
        //                 mensaje = "Pin incorrecto, intente nuevamente.";
        //             }
        //         }
        //         if (ex instanceof java.security.cert.CertificateExpiredException) {
        //             mensaje = "El certificado se encuentra expirado.";
        //         }
        //         if (ex instanceof java.security.cert.CertificateNotYetValidException) {
        //             mensaje = "El certificado aún no está vigente.";
        //         }
        //         if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
        //             if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
        //                 mensaje = "Por favor verifique el pin.";
        //             }
        //         }
        //         if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
        //             if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
        //                 mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
        //             }
        //         }
        //         json.put("finalizado", false);
        //         json.put("mensaje", mensaje);
        //     } catch (JSONException e) {
        //         Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
        //     }
        // }
        // return json.toString();
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenSolicitudDto objetoDto = om.readValue(body, TokenSolicitudDto.class);
            return om.writeValueAsString(servicio.firmarSolicitudes(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
