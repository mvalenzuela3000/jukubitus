/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources3;

import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Slot;
import bo.firmadigital.jacobitus.comun.token.SmartCard;
import bo.firmadigital.jacobitus.comun.token.Token;
import bo.firmadigital.jacobitus.validador.DatosCertificado;
import bo.firmadigital.jacobitus.validador.Opciones;
import bo.firmadigital.jacobitus.validador.Validador;
import bo.firmadigital.jacobitus4.util.Config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
@Path("/")
public class TokenRest {
    private static Slot[] slots;

    private bo.firmadigital.jacobitus.firmador.Opciones getOpcionesFirmador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.firmador.Opciones opciones = new bo.firmadigital.jacobitus.firmador.Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        opciones.setApiSelloTiempo(config.getTS());
        opciones.setJwtSelloTiempo(config.getTSJWT());
        opciones.setHsmHabilitado(config.isHsmEnabled());
        opciones.setTipoHsm(config.getHsmType());
        opciones.setApiHsm(config.getHsmCloud());
        opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }
    
    private bo.firmadigital.jacobitus.validador.Opciones getOpcionesValidador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.validador.Opciones opciones = new bo.firmadigital.jacobitus.validador.Opciones();
        opciones.setProxyHabilitado(config.isProxyEnabled());
        opciones.setServidorProxy(config.getProxyIP());
        opciones.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
        return opciones;
    }

    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        return "Ok";
    }

    @GET
    @Path("/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tokens() {
        JSONObject json = new JSONObject();
        try {
            try {
                JSONArray datos = new JSONArray();
                List<JSONObject> tokens = SmartCard.cards(this.getOpcionesFirmador());
                for (JSONObject token : tokens) {
                    datos.put(token.get("name"));
                }
                json.put("datos", datos);
                json.put("finalizado", true);
                json.put("mensaje", "Tokens detectados");
            } catch (RuntimeException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String start(@QueryParam("pin") String pin) {
        JSONObject json = new JSONObject();
        try {
            if (pin == null) {
                json.put("finalizado", false);
                json.put("mensaje", "El pin es nulo. Utilice el metodo setParametrosConexion(ruta,pin)");
            } else {
                try {
                    if (slots != null) {
                        if (slots.length == 1) {
                            slots[0].getToken().salir();
                        }
                        slots = null;
                    }
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    slots = gestorSlot.listarSlots(this.getOpcionesFirmador());
                    if (slots.length == 1) {
                        slots[0].getToken().iniciar(pin);
                        json.put("finalizado", true);
                        json.put("mensaje", "Autenticacion correcta");
                    } else {
                        json.put("finalizado", false);
                        if (slots.length > 1) {
                            json.put("mensaje", "Se encontró más de un token conectado.");
                        } else {
                            json.put("mensaje", "No se encontró ningún token conectado.");
                        }
                        slots = null;
                    }
                } catch (RuntimeException | GeneralSecurityException ex) {
                    json.put("finalizado", false);
                    json.put("mensaje", ex.getMessage());
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/certs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String certs() {
        JSONObject json = new JSONObject();
        try {
            try {
                if (slots == null) {
                    json.put("finalizado", false);
                    json.put("mensaje", "Primero debe iniciar sesión.");
                } else {
                    if (slots.length == 1) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        Token token = slots[0].getToken();
                        List<String> labels = token.listarIdentificadorClaves();
                        JSONArray datos = new JSONArray();
                        for (String label : labels) {
                            DatosCertificado entry = new DatosCertificado(label, token.obtenerCertificado(label));
                            JSONObject cert = new JSONObject();
                            cert.put("esFirmaBolivia", Validador.verificarPKI(entry.getCert()));
                            cert.put("numeroSerie", entry.getCert().getSerialNumber());
                            cert.put("nombreComunIssuer", entry.getNombreComunIssuer());
                            cert.put("organizacionIssuer", entry.getOrganizacionIssuer());
                            cert.put("nombreComunSubject", entry.getNombreComunSubject());
                            cert.put("ci", entry.getNumeroDocumentoSubject());
                            cert.put("complemento", entry.getComplementoSubject());
                            cert.put("organizacionSubject", entry.getOrganizacionSubject());
                            cert.put("unidadOrganizacionalSubject", entry.getUnidadOrganizacionalSubject());
                            cert.put("inicioValidez", dateFormat.format(entry.getInicioValidez()));
                            cert.put("finValidez", dateFormat.format(entry.getFinValidez()));
                            cert.put("alias", label);
                            cert.put("esValido", entry.getInicioValidez().compareTo(new Date()) < 0 && entry.getFinValidez().compareTo(new Date()) > 0);
                            Validador.OCSPState state = Validador.verificarOcsp(entry.getCert(), new Date(), this.getOpcionesValidador()).getState();
                            if (state == Validador.OCSPState.OK) {
                                cert.put("OCSP", "no revocado");
                            } else {
                                cert.put("OCSP", state.toString());
                            }
                            datos.put(cert);
                        }
                        json.put("datos", datos);
                        json.put("finalizado", true);
                        json.put("mensaje", "Certificados obtenidos correctamente");
                    } else {
                        json.put("finalizado", false);
                        if (slots.length > 1) {
                            json.put("mensaje", "Se encontró más de un token conectado.");
                        } else {
                            json.put("mensaje", "Por favor no desconecte el token.");
                        }
                    }
                }
            } catch (RuntimeException | GeneralSecurityException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @POST
    @Path("/sign")
    @Produces(MediaType.APPLICATION_JSON)
    public String sign(String body) {
        JSONObject json = new JSONObject();
        try {
            try {
                if (slots == null) {
                    json.put("finalizado", false);
                    json.put("mensaje", "Primero debe iniciar sesión.");
                } else {
                    if (slots.length == 1) {
                        JSONObject req = new JSONObject(body);
                        if (req.has("nombre_archivo") && req.has("alias") && req.has("pdf_base64")) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            Token token = slots[0].getToken();
                            X509Certificate certificate = token.obtenerCertificado(req.getString("alias"));
                            if (certificate == null) {
                                json.put("finalizado", false);
                                json.put("mensaje", "No se encontró un certificado con el alias solicitado.");
                            } else {
                                DatosCertificado entry = new DatosCertificado(req.getString("alias"), certificate);
                                JSONObject cert = new JSONObject();
                                cert.put("esFirmaBolivia", Validador.verificarPKI(entry.getCert()));
                                cert.put("numeroSerie", entry.getCert().getSerialNumber());
                                cert.put("nombreComunIssuer", entry.getNombreComunIssuer());
                                cert.put("organizacionIssuer", entry.getOrganizacionIssuer());
                                cert.put("nombreComunSubject", entry.getNombreComunSubject());
                                cert.put("ci", entry.getNumeroDocumentoSubject());
                                cert.put("complemento", entry.getComplementoSubject());
                                cert.put("organizacionSubject", entry.getOrganizacionSubject());
                                cert.put("unidadOrganizacionalSubject", entry.getUnidadOrganizacionalSubject());
                                cert.put("inicioValidez", dateFormat.format(entry.getInicioValidez()));
                                cert.put("finValidez", dateFormat.format(entry.getFinValidez()));
                                cert.put("esValido", entry.getInicioValidez().compareTo(new Date()) < 0 && entry.getFinValidez().compareTo(new Date()) > 0);
                                Validador.OCSPState state = Validador.verificarOcsp(entry.getCert(), new Date(), this.getOpcionesValidador()).getState();
                                if (state == Validador.OCSPState.OK) {
                                    cert.put("OCSP", "no revocado");
                                } else {
                                    cert.put("OCSP", state.toString());
                                }
                                JSONObject datos = new JSONObject();
                                byte[] pdf = Base64.getDecoder().decode(req.getString("pdf_base64"));
                                ByteArrayOutputStream os = new ByteArrayOutputStream();
                                FirmadorPdf.firmar(new ByteArrayInputStream(pdf), os, false, token, req.getString("alias"));
                                datos.put("pdf_base64", Base64.getEncoder().encodeToString(os.toByteArray()));
                                datos.put("nombre_archivo", req.getString("nombre_archivo"));
                                datos.put("certificado", cert);
                                json.put("datos", datos);
                                json.put("finalizado", true);
                                json.put("mensaje", "Certificados obtenidos correctamente");
                            }
                        } else {
                            json.put("finalizado", false);
                            json.put("mensaje", "Parámetros obligatorios (nombre_archivo, alias, pdf_base64).");
                        }
                    } else {
                        json.put("finalizado", false);
                        if (slots.length > 1) {
                            json.put("mensaje", "Se encontró más de un token conectado.");
                        } else {
                            json.put("mensaje", "Por favor no desconecte el token.");
                        }
                    }
                }
            } catch (RuntimeException | GeneralSecurityException | IOException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String finish() {
        JSONObject json = new JSONObject();
        try {
            if (slots == null) {
                json.put("finalizado", false);
                json.put("mensaje", "No se encontró una sesión activa.");
            } else {
                try {
                    if (slots.length == 1) {
                        slots[0].getToken().salir();
                        slots = null;
                        json.put("finalizado", true);
                        json.put("mensaje", "Cerrado sesión correctamente");
                    } else {
                        json.put("finalizado", false);
                        if (slots.length > 1) {
                            json.put("mensaje", "Se encontró más de un token conectado.");
                        } else {
                            json.put("mensaje", "No se encontró ningún token conectado.");
                        }
                    }
                } catch (RuntimeException ex) {
                    json.put("finalizado", false);
                    json.put("mensaje", ex.getMessage());
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/estadoToken")
    @Produces(MediaType.TEXT_HTML)
    public String estadoToken() {
        JSONObject json = new JSONObject();
        try {
            json.put("finalizado", true);
            json.put("mensaje", "False");
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/matarProceso")
    @Produces(MediaType.TEXT_HTML)
    public String matarProceso() {
        return "Ok";
    }

    @GET
    @Path("/reset")
    @Produces(MediaType.TEXT_HTML)
    public String reset() {
        if (slots != null) {
            if (slots.length == 1) {
                slots[0].getToken().salir();
            }
            slots = null;
        }
        return "Ok";
    }
}
