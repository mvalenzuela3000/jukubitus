/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import bo.firmadigital.jacobitus4.App;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.token.SmartCard;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Slot;
import bo.firmadigital.token.Token;
import bo.firmadigital.token.TokenPKCS12;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
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
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
        JSONObject json = new JSONObject();
        try {
            try {
                JSONObject datos = new JSONObject();
                JSONArray jsonArray = new JSONArray();
                List<JSONObject> tokens = SmartCard.cards();
                for (JSONObject token : tokens) {
                    jsonArray.put(token.get("name"));
                }
                datos.put("connected", jsonArray.length() > 0);
                datos.put("tokens", jsonArray);
                json.put("datos", datos);
                json.put("finalizado", true);
                json.put("mensaje", "Lista de Tokens obtenida");
            } catch (RuntimeException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    /**
     * @api {get} /api/token/connected Obtiene información de los tokens conectados.
     * @apiGroup Token
     * @apiVersion 1.0.0
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
        JSONObject json = new JSONObject();
        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            try {
                Slot[] slots = gestorSlot.listarSlots();
                json.put("datos", new JSONObject());
                ((JSONObject)json.get("datos")).put("connected", slots.length > 0);
                ((JSONObject)json.get("datos")).put("tokens", new JSONArray());
                for (Slot slot : slots) {
                    CK_TOKEN_INFO info = slot.detalleToken();
                    JSONObject token = new JSONObject();
                    token.put("slot", slot.getSlotID());
                    token.put("serial", new String(info.serialNumber).trim());
                    token.put("name", new String(info.manufacturerID).trim());
                    token.put("model", new String(info.model).trim());
                    ((JSONArray)((JSONObject)json.get("datos")).get("tokens")).put(token);
                }
                json.put("finalizado", true);
                if (slots.length > 0) {
                    json.put("mensaje", "Lista de Tokens obtenida");
                } else {
                    json.put("mensaje", "No se detecto nigun token conectado");
                }
            } catch (RuntimeException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/data Obtiene información de los certificados en el token conectado al slot.
     * @apiGroup Token
     * @apiVersion 1.0.0
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
        JSONObject json = new JSONObject();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = getClass().getClassLoader().getResourceAsStream("firmadigital_bo.crt");
            List<X509Certificate> intermediates = (List<X509Certificate>) fact.generateCertificates(is);
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot[] slots = gestorSlot.listarSlots();
            if (slots.length == 1 && !req.has("slot")) {
                req.put("slot", slots[0].getSlotID());
            }
            if (req.has("slot") && req.has("pin")) {
                Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
                Token token = slot.getToken();
                json.put("datos", new JSONObject());
                try {
                    token.iniciar((String)req.get("pin"));
                    json.put("finalizado", true);
                    json.put("mensaje", "Datos de token obtenidos correctamente");
                    List<String> llaves = token.listarIdentificadorClaves();
                    JSONObject data_token = new JSONObject();
                    ((JSONObject)json.get("datos")).put("data_token", data_token);
                    data_token.put("certificates", llaves.size());
                    data_token.put("data", new JSONArray());
                    for (int i = 0; i < llaves.size(); i++) {
                        JSONObject key = new JSONObject();
                        key.put("tipo", "PRIMARY_KEY");
                        key.put("tipo_desc", "Clave Privada");
                        key.put("alias", llaves.get(i));
                        key.put("id", llaves.get(i));
                        X509Certificate cert = token.obtenerCertificado(llaves.get(i));
                        key.put("tiene_certificado", cert != null);
                        ((JSONArray)data_token.get("data")).put(key);
                        if (key.getBoolean("tiene_certificado")) {
                            JSONObject x509 = new JSONObject();
                            x509.put("tipo", "X509_CERTIFICATE");
                            x509.put("tipo_desc", "Certificado");
                            x509.put("adsib", false);
                            for (X509Certificate intermediate : intermediates) {
                                try {
                                    cert.verify(intermediate.getPublicKey());
                                    x509.put("adsib", true);
                                    break;
                                } catch (GeneralSecurityException ex) {
                                }
                            }
                            x509.put("serialNumber", cert.getSerialNumber().toString(16));
                            x509.put("alias", llaves.get(i));
                            x509.put("id", llaves.get(i));
                            String pem = "-----BEGIN CERTIFICATE-----\n";
                            pem += Base64.getEncoder().encodeToString(cert.getEncoded());
                            pem += "\n-----END CERTIFICATE-----";
                            x509.put("pem", pem);
                            x509.put("validez", new JSONObject());
                            ((JSONObject)x509.get("validez")).put("desde", dateFormat.format(cert.getNotBefore()));
                            ((JSONObject)x509.get("validez")).put("hasta", dateFormat.format(cert.getNotAfter()));
                            X500Name x500Name = new JcaX509CertificateHolder(cert).getSubject();
                            x509.put("titular", new JSONObject());
                            for(RDN rdn : x500Name.getRDNs()) {
                                switch (rdn.getFirst().getType().getId()) {
                                    case "2.5.4.46":
                                        ((JSONObject)x509.get("titular")).put("dnQualifier", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                    case "1.3.6.1.1.1.1.0":
                                        ((JSONObject)x509.get("titular")).put("uidNumber", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                    case "2.5.4.3":
                                        ((JSONObject)x509.get("titular")).put("CN", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        x509.put("common_name", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                    case "2.5.4.10":
                                        ((JSONObject)x509.get("titular")).put("O", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                    case "2.5.4.11":
                                        ((JSONObject)x509.get("titular")).put("OU", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                }
                            }
                            X500Name x500IssuerName = new JcaX509CertificateHolder(cert).getSubject();
                            x509.put("emisor", new JSONObject());
                            for(RDN rdn : x500IssuerName.getRDNs()) {
                                switch (rdn.getFirst().getType().getId()) {
                                    case "2.5.4.3":
                                        ((JSONObject)x509.get("emisor")).put("CN", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                    case "2.5.4.10":
                                        ((JSONObject)x509.get("emisor")).put("O", IETFUtils.valueToString(rdn.getFirst().getValue()));
                                        break;
                                }
                            }
                            ((JSONArray)data_token.get("data")).put(x509);
                        }
                    }
                    data_token.put("private_keys", llaves.size());
                } catch (GeneralSecurityException ex) {
                    json.put("finalizado", false);
                    json.put("mensaje", ex.getMessage());
                }
                token.salir();
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot y pin.");
            }
        } catch (JSONException | CertificateException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String create(String body) {
        JSONObject json = new JSONObject();
        try {
            json.put("finalizado", false);
            JSONObject req = new JSONObject(body);
            if (req.getString("pin").length() < 8) {
                json.put("mensaje", "El pin es muy corto.");
            } else {
                int num = 0, may = 0, minu = 0;
                char[] password = req.getString("pin").toCharArray();
                for (int i = 0; i < req.getString("pin").length(); i++) {
                    if (password[i] >= '0' && password[i] <= '9') {
                        num++;
                    } else if (password[i] >= 'A' && password[i] <= 'Z') {
                        may++;
                    } else if (password[i] >= 'a' && password[i] <= 'z') {
                        minu++;
                    }
                }
                if (num < 1 || may < 1 || minu < 1) {
                    json.put("mensaje", "El pin debe contener al menos un número, una letra mayúscula y una letra minúscula.");
                } else {
                    Config config  = new Config();
                    Slot slot = new Slot(config.getTokenToCreate());
                    TokenPKCS12 token = new TokenPKCS12(slot);
                    try {
                        token.crear(req.getString("pin"));
                        json.put("finalizado", true);
                        json.put("mensaje", "Token generado correctamente.");
                    } catch (GeneralSecurityException ex) {
                        json.put("mensaje", ex.getMessage());
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @GET
    @Path("/generate_keypar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_keypar(@QueryParam("pin") String pin, @QueryParam("slot") Integer slotNumber) {
        JSONObject json = new JSONObject();
        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot slot = gestorSlot.obtenerSlot(slotNumber);
            Token token = slot.getToken();
            token.iniciar(pin);
            BigInteger max = new BigInteger("1000000000000");
            BigInteger id = new BigInteger(max.bitLength(), new SecureRandom()).mod(max);
            token.generarClaves(id.toString(), pin, slotNumber);
            JSONObject datos = new JSONObject();
            json.put("datos", datos);
            JSONObject data_token = new JSONObject();
            datos.put("data_token", data_token);
            data_token.put("certificates", 0);
            JSONArray data = new JSONArray();
            data_token.put("data", data);
            JSONObject pk = new JSONObject();
            data.put(pk);
            pk.put("tipo", "PRIMARY_KEY");
            pk.put("tipo_desc", "Clave Privada");
            pk.put("alias", id.toString());
            pk.put("tiene_certificado", false);
            data_token.put("private_keys", 1);
            json.put("finalizado", true);
            json.put("mensaje", "Se genero el par de claves correctamente.");
            token.salir();
        } catch (GeneralSecurityException | JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @POST
    @Path("/generate_csr")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_csr(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
            Token token = slot.getToken();
            JSONObject datos = new JSONObject();
            json.put("datos", datos);
            try {
                token.iniciar(req.getString("pin"));
                datos.put("csr", token.generarCSR(req.getString("alias_certificado"), req.getJSONArray("subject")));
                json.put("finalizado", true);
                json.put("mensaje", "Se genero el CSR correctamente");
            } catch (GeneralSecurityException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
            token.salir();
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @POST
    @Path("/cargar_pem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cargar_pem(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
            Token token = slot.getToken();
            json.put("datos", new JSONObject());
            try {
                token.iniciar(req.getString("pin"));
                token.cargarCertificado(new String(Base64.getDecoder().decode(req.getString("pem")), "UTF-8"), req.getString("id"));
                json.put("finalizado", true);
                json.put("mensaje", "El certificado fue adicionado correctamente");
            } catch (GeneralSecurityException | UnsupportedEncodingException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
            token.salir();
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }

    @POST
    @Path("/cambiar_pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cambiar_pin(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot slot = gestorSlot.obtenerSlot(req.getInt("slot"));
            Token token = slot.getToken();
            try {
                token.modificarPin(req.getString("old_pin"), req.getString("new_pin"));
                json.put("finalizado", true);
                json.put("mensaje", "El pin se cambió correctamente");
            } catch (RuntimeException ex) {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            }
        } catch (JSONException ex) {
            Logger.getLogger(TokenRest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json.toString();
    }
}
