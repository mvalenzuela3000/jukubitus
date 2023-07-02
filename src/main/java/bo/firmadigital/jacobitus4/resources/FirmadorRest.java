/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import bo.firmadigital.jacobitus.firmador.FirmadorPKCS7;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.FirmadorXml;
import bo.firmadigital.jacobitus.firmador.TokenSelected;
import bo.firmadigital.jacobitus4.App;
import bo.firmadigital.jacobitus4.pojo.CompleteSign;
import bo.firmadigital.jacobitus4.pojo.Signs;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus.utilidades.Base64StreamParser;
import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Slot;
import bo.firmadigital.jacobitus.comun.token.Token;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import bo.firmadigital.jacobitus.firmador.IFirmador;
import bo.firmadigital.jacobitus.firmador.Opciones;

/**
 *
 * @author ADSIB
 */
@Path("/token")
public class FirmadorRest {
    private Opciones getOpciones() {
        Config config = Config.getInstance();
        Opciones opciones = new Opciones();
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
    
    @POST
    @Path("/firmar_json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarJson(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            byte[] dataByte = Base64.getDecoder().decode(req.getString("data"));
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.listarSlots(this.getOpciones());
            Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"), this.getOpciones());
            Token token = slot.getToken();
            token.iniciar(req.getString("pin"));
            // Crea un firmador RSA256
            PrivateKey pk = token.obtenerClavePrivada(req.getString("alias"));
            if (pk == null) {
                token.salir();
                throw new KeyStoreException("No se encontró la clave con alias: " + req.getString("alias"));
            }
            JWSSigner signer = new RSASSASigner(pk);
            CompleteSign enviadoJson;
            boolean inicial = true;

            try {
                String enviado = new String(dataByte);
                ObjectMapper mapper = new ObjectMapper();
                enviadoJson = (CompleteSign) mapper.readValue(enviado, CompleteSign.class);
                inicial = false;
            } catch (IOException ex) {
                enviadoJson = new CompleteSign();
            }

            JWSObject jwsObject;
            if (inicial) {
                //Crea un objeto JWS para firmar
                jwsObject = new JWSObject(
                                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                                new Payload(dataByte));
                enviadoJson.setPayload(new String(Base64.getEncoder().encode(dataByte), StandardCharsets.UTF_8));
                enviadoJson.setSignatures(new ArrayList<>());
            } else {
                //Crea un objeto JWS para firmar con el payload existente
                jwsObject = new JWSObject(
                                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                                new Payload(enviadoJson.getPayload()));
            }

            try {
                jwsObject.sign(signer);
            } catch (JOSEException ex) {
                throw new RuntimeException("Error al firmar: " + ex.getMessage());
            }            
            // Conversion del certificado de X509 a PEM para su inclusion en el flat json
            X509Certificate cert = token.obtenerCertificado(req.getString("alias"));
            String pemCert = Base64.getEncoder().encodeToString(cert.getEncoded());
            /*String pemCert = "-----BEGIN CERTIFICATE-----\n\n";
            pemCert += java.util.Base64.getEncoder().encodeToString(cert.getEncoded());
            pemCert += "\n-----END CERTIFICATE-----";*/

            token.salir();

            // Crea un objeto de firma flat, una serializacion de JWT
            Signs sign = new Signs();
            Map<String, Object> mapa = new HashMap<>();
            mapa.put("gen", "MEFP-DGSGIF");
            mapa.put("x5c", pemCert.replaceAll("(\r\n|\n)", "").toCharArray());
            sign.setHeader(mapa);
            String serial = jwsObject.serialize();
            String[] partes = serial.split("\\.");
            sign.setProtect(partes[0]);
        
            sign.setSignature(jwsObject.getSignature().toString());
            enviadoJson.getSignatures().add(sign);

            ObjectMapper mapper = new ObjectMapper();
            byte[] bytes = mapper.writeValueAsBytes(enviadoJson);
            InputStream input = new ByteArrayInputStream(bytes);
            BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
            String resultado = buffer.lines().collect(Collectors.joining("\n"));

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String fechaFirma = dateFormat.format(new java.sql.Timestamp(calendar.getTime().getTime()));
            
            JSONObject jsonResult = new JSONObject();
            jsonResult.put("json_firmado", Base64.getEncoder().encodeToString(resultado.getBytes()));
            X500Name x500Name = new JcaX509CertificateHolder(token.obtenerCertificado(req.getString("alias"))).getSubject();
            jsonResult.put("cn", IETFUtils.valueToString(x500Name.getRDNs(new ASN1ObjectIdentifier("2.5.4.3"))[0].getFirst().getValue()));
            jsonResult.put("fecha_firma", fechaFirma);
            json.put("finalizado", true);
            json.put("mensaje", "Se firmo la solicitud correctamente!");
            json.put("datos", jsonResult);
        } catch (JSONException | GeneralSecurityException | IOException ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/firmar_pdf Firma un documento pdf.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {Boolean} [bloquear] Bandera que en caso de estar presente con valor true, bloqueará la posibilidad de añadir más firmas al documento.
     * @apiParam {String} pdf Archivo pdf en base64 que se desea firmar.
     * @apiParam {Object.} [point] Coordenadas para la referencia de firma.
     * @apiParam {Numeric} [point.x] Coordenada x para la referencia de firma.
     * @apiParam {Numeric} [point.y] Coordenada y para la referencia de firma.
     * @apiParam {String} [image] Imagen en base64 a utilizar en la firma.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678",
     *     "alias": "355409121073",
     *     "pdf": "MII...truncated...=="
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "pdf_firmado": "MII...truncated...=="
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se firmo el pdf correctamente."
     * }
     */
    @POST
    @Path("/firmar_pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarPdf(InputStream body) {
        JSONObject json = new JSONObject();
        try {
            String pin = null, alias = null, image = null;
            Long slot = null; Integer x = null, y = null;
            boolean bloquear = false;
            byte[] file = null;
            JsonFactory factory = new ObjectMapper().getJsonFactory();
            JsonParser jsonReader = factory.createJsonParser(body);
            try {
                jsonReader.nextToken();
                while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
                    String label = jsonReader.getText();
                    jsonReader.nextToken();
                    switch (label) {
                        case "slot":
                            slot = Long.parseLong(jsonReader.readValueAs(String.class));
                            break;
                        case "pin":
                            pin = jsonReader.readValueAs(String.class);
                            break;
                        case "alias":
                            alias = jsonReader.readValueAs(String.class);
                            break;
                        case "bloquear":
                            bloquear = Boolean.parseBoolean(jsonReader.readValueAs(String.class));
                            break;
                        case "pdf":
                            try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                    jsonReader.releaseBuffered(os);
                                    byte[] buff = os.toByteArray();
                                    Base64StreamParser parser = new Base64StreamParser(is, buff);
                                    file = parser.getFile();
                                    jsonReader.close();
                                    jsonReader = factory.createJsonParser(parser.getRemanent());
                                    jsonReader.nextToken();
                                }
                            }
                            break;
                        case "point":
                            Point point = jsonReader.readValueAs(Point.class);
                            x = (int)point.getX();
                            y = (int)point.getY();
                            break;
                        case "image":
                            image = jsonReader.readValueAs(String.class);
                            break;
                        default:
                            Logger.getLogger(FirmadorRest.class.getName()).log(Level.WARNING, null, label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                Slot[] slots = gestorSlot.listarSlots(this.getOpciones());
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }
            if (slot != null && pin != null && alias != null && file != null) {
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IFirmador firmar;
                if (x == null) {
                    firmar = FirmadorPdf.getInstance(slot, alias, pin, this.getOpciones());
                } else {
                    firmar = FirmadorPdf.getInstance(slot, alias, pin, image, x, y, this.getOpciones());
                }
                firmar.firmar(new ByteArrayInputStream(file), out, bloquear);

                datos.put("pdf_firmado", Base64.getEncoder().encodeToString(out.toByteArray()));
                json.put("finalizado", true);
                json.put("mensaje", "Se firmo el pdf correctamente!");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot, pin, alias y pdf.");
            }
        } catch (JSONException | IOException | GeneralSecurityException | OutOfMemoryError ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    @POST
    @Path("/firmar_solicitudes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarSolicitudes(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.listarSlots(this.getOpciones());
            Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"), this.getOpciones());
            Token token = slot.getToken();
            token.iniciar(req.getString("pin"));
            JSONArray data = req.getJSONArray("data");
            JSONArray datos = new JSONArray();
            for (int i = 0; i < data.length(); i++) {
                JSONObject element = new JSONObject();
                element.put("id", data.getJSONObject(i).getString("id"));
                PrivateKey pk = token.obtenerClavePrivada(req.getString("alias"));
                if (pk == null) {
                    token.salir();
                    throw new KeyStoreException("No se encontró la clave con alias: " + req.getString("alias"));
                }
                JWSSigner jwsSigner = new RSASSASigner(pk);
                JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
                if (data.getJSONObject(i).isNull("url")) {
                    builder.x509CertURL(new URI("https://agencia.firmadigital.bo/services_ar/certificado?serial_number=" + token.obtenerCertificado(req.getString("alias")).getSerialNumber()));
                } else {
                    if (data.getJSONObject(i).getString("url").contains("?")) {
                        builder.x509CertURL(new URI(data.getJSONObject(i).getString("url")));
                    } else {
                        builder.x509CertURL(new URI(data.getJSONObject(i).getString("url") + "?serial_number=" + token.obtenerCertificado(req.getString("alias")).getSerialNumber()));
                    }
                }
                JWSObject jwsObject = new JWSObject(builder.build(),new Payload(data.getJSONObject(i).getString("payload")));
                jwsObject.sign(jwsSigner);
                element.put("jws", jwsObject.serialize());
                datos.put(element);
            }
            token.salir();
            json.put("datos", datos);
            json.put("finalizado", true);
            json.put("mensaje", "Se firmo las solicitudes correctamente!");
        } catch (JSONException | GeneralSecurityException | URISyntaxException | JOSEException ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/firmar_hash Obtiene la firma encriptando el hash.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     * 
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {String} hash Suma de verificación calculada usando sha2 a 256bits.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678",
     *     "alias": "355409121073",
     *     "hash": "e633f4fc79badea1dc5db970cf397c8248bac47cc3acf9915ba60b5d76b0e88f"
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "firma": "BBTDS8+NDwA38cyFd/sd3gQ5PpA42ZkQpkQFXtx3vjKi0YGkuyl50yW87hxYcSsIS674nilBSMLRFnwSg87E6rUUhaG+RS0Rh55PwXqE7GRxbyS0yEJAYF+ifw+epD3UBywExrJyUPikmADcBa1jUqLrlYiWOr1Vdhg7/SPTapNyEGtG/Tlv00KWv0RVt6NNx7hQMeRG52pbdCRv6LyJZcck7QuLsVPHuqGBLwHG6j4q+WnWih1Fi7Mnlj0DXYn1uDH/K5+saXHKfAO5SQTMYZwVbWRiyPSwLOIAFcebprL2+RTIA5cq7pl9hSXOqcyGmBcHDVWG2iNDYsYXtaqdNA=="
     *     },
     *     "finalizado": true,
     *     "mensaje": "Firma realizada correctamente."
     * }
     */
    @POST
    @Path("/firmar_hash")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarHash(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot[] slots = gestorSlot.listarSlots(this.getOpciones());
            if (!req.has("slot") && slots.length == 1) {
                req.put("slot", slots[0].getSlotID());
            }
            if (req.has("slot") && req.has("pin") && req.has("alias") && req.has("hash")) {
                Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"), this.getOpciones());
                Token token = slot.getToken();
                token.iniciar(req.getString("pin"));
                JSONObject datos = new JSONObject();
                Signature signature = Signature.getInstance("SHA256withRSA");
                PrivateKey pk = token.obtenerClavePrivada(req.getString("alias"));
                if (pk == null) {
                    token.salir();
                    throw new KeyStoreException("No se encontró la clave con alias: " + req.getString("alias"));
                }
                signature.initSign(pk);
                signature.update(Base64.getDecoder().decode(req.getString("hash")));
                byte[] signed = signature.sign();
                token.salir();
                datos.put("firma", Base64.getEncoder().encodeToString(signed));
                json.put("datos", datos);
                json.put("finalizado", true);
                json.put("mensaje", "Firma realizada correctamente.");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot, pin, alias y hash.");
            }
        } catch (GeneralSecurityException | JSONException ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/firmar_pkcs7 Firma un archivo.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {Boolean} [detached] Bandera que en caso de estar presente con valor true, firma sin el contenido del documento.
     * @apiParam {String} file Archivo en base64 que se desea firmar.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678",
     *     "alias": "355409121073",
     *     "file": "MII...truncated...=="
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "pkcs7": "MII...truncated...=="
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se firmo el archivo correctamente."
     * }
     */
    @POST
    @Path("/firmar_pkcs7")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarPKCS7(InputStream body) {
        JSONObject json = new JSONObject();
        try {
            String pin = null, alias = null;
            Long slot = null;
            boolean detached = false;
            byte[] file = null;
            JsonFactory factory = new ObjectMapper().getJsonFactory();
            JsonParser jsonReader = factory.createJsonParser(body);
            try {
                jsonReader.nextToken();
                while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
                    String label = jsonReader.getText();
                    jsonReader.nextToken();
                    switch (label) {
                        case "slot":
                            slot = Long.parseLong(jsonReader.readValueAs(String.class));
                            break;
                        case "pin":
                            pin = jsonReader.readValueAs(String.class);
                            break;
                        case "alias":
                            alias = jsonReader.readValueAs(String.class);
                            break;
                        case "detached":
                            detached = Boolean.parseBoolean(jsonReader.readValueAs(String.class));
                            break;
                        case "file":
                            try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                    jsonReader.releaseBuffered(os);
                                    byte[] buff = os.toByteArray();
                                    Base64StreamParser parser = new Base64StreamParser(is, buff);
                                    file = parser.getFile();
                                    jsonReader.close();
                                    jsonReader = factory.createJsonParser(parser.getRemanent());
                                    jsonReader.nextToken();
                                }
                            }
                            break;
                        default:
                            Logger.getLogger(FirmadorRest.class.getName()).log(Level.WARNING, null, label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                Slot[] slots = gestorSlot.listarSlots(this.getOpciones());
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }
            if (slot != null && pin != null && alias != null && file != null) {
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    FirmadorPKCS7 firmar = FirmadorPKCS7.getInstance(slot, alias, pin, this.getOpciones());
                    try (InputStream is = new BufferedInputStream(new ByteArrayInputStream(file))) {
                        firmar.firmar(is, out, detached);
                    }
                    datos.put("pkcs7", Base64.getEncoder().encodeToString(out.toByteArray()));
                }
                json.put("finalizado", true);
                json.put("mensaje", "Se firmo el archivo correctamente!");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot, pin, alias y file.");
            }
        } catch (JSONException | IOException | GeneralSecurityException | OutOfMemoryError ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/firmar_xml Firma un documento xml.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {String} file Archivo XML en base64 que se desea firmar.
     * @apiParam {String} [node] Nodo del XML que se desea firmar.
     * @apiParam {String} [digest] Algoritmo utilizado para el digest por defecto SHA256.
     * @apiParam {String} [signatureAlgorithm] Algoritmo utilizado para el digest de la firma por RCA por defecto SHA256.
     * @apiParam {Boolean} [enveloped] Requerir modo enveloped aun con nodo.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678",
     *     "alias": "355409121073",
     *     "file": "MII...truncated...=="
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "xml": "MII...truncated...=="
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se firmo el archivo correctamente."
     * }
     */
    @POST
    @Path("/firmar_xml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarXml(InputStream body) {
        JSONObject json = new JSONObject();
        try {
            String pin = null, alias = null;
            Long slot = null;
            byte[] file = null;
            String node = null;
            Boolean enveloped = false;
            String digest = null;
            String signatureAlgorithm = null;
            JsonFactory factory = new ObjectMapper().getJsonFactory();
            JsonParser jsonReader = factory.createJsonParser(body);
            try {
                jsonReader.nextToken();
                while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
                    String label = jsonReader.getText();
                    jsonReader.nextToken();
                    switch (label) {
                        case "slot":
                            slot = Long.parseLong(jsonReader.readValueAs(String.class));
                            break;
                        case "pin":
                            pin = jsonReader.readValueAs(String.class);
                            break;
                        case "alias":
                            alias = jsonReader.readValueAs(String.class);
                            break;
                        case "file":
                            try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                                try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                                    jsonReader.releaseBuffered(os);
                                    byte[] buff = os.toByteArray();
                                    Base64StreamParser parser = new Base64StreamParser(is, buff);
                                    file = parser.getFile();
                                    jsonReader.close();
                                    jsonReader = factory.createJsonParser(parser.getRemanent());
                                    jsonReader.nextToken();
                                }
                            }
                            break;
                        case "node":
                            node = jsonReader.readValueAs(String.class);
                            break;
                        case "digest":
                            digest = jsonReader.readValueAs(String.class);
                            break;
                        case "signatureAlgorithm":
                            signatureAlgorithm = jsonReader.readValueAs(String.class);
                            break;
                        case "enveloped":
                            enveloped = jsonReader.readValueAs(Boolean.class);
                            break;
                        default:
                            Logger.getLogger(FirmadorRest.class.getName()).log(Level.WARNING, null, label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                Slot[] slots = gestorSlot.listarSlots(this.getOpciones());
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }
            if (slot != null && pin != null && alias != null && file != null) {
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                    FirmadorXml firmar;
                    if (node == null) {
                        firmar = FirmadorXml.getInstance(slot, alias, pin, this.getOpciones());
                    } else {
                        firmar = FirmadorXml.getInstance(slot, alias, pin, node, this.getOpciones());
                    }
                    if (digest != null) {
                        switch (digest.toUpperCase()) {
                            case "SHA1":
                                firmar.setMessageDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA1);
                                break;
                            case "SHA256":
                                firmar.setMessageDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA256);
                                break;
                            case "SHA512":
                                firmar.setMessageDigestAlgorithm(MessageDigestAlgorithm.ALGO_ID_DIGEST_SHA512);
                                break;
                            default:
                                throw new GeneralSecurityException("Algoritmos soportado para el digest SHA1, SHA256 y SHA512");
                        }
                    }
                    if (signatureAlgorithm != null) {
                        switch (signatureAlgorithm.toUpperCase()) {
                            case "SHA1":
                                firmar.setSignatureMethodAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1);
                                break;
                            case "SHA256":
                                firmar.setSignatureMethodAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
                                break;
                            case "SHA512":
                                firmar.setSignatureMethodAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512);
                                break;
                            default:
                                throw new GeneralSecurityException("Algoritmos soportado para el digest de la firma SHA1, SHA256 y SHA512");
                        }
                    }
                    try (InputStream is = new BufferedInputStream(new ByteArrayInputStream(file))) {
                        firmar.firmar(is, out, enveloped);
                    }
                    datos.put("xml", Base64.getEncoder().encodeToString(out.toByteArray()));
                }
                json.put("finalizado", true);
                json.put("mensaje", "Se firmo el archivo correctamente!");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot, pin, alias y file.");
            }
        } catch (JSONException | IOException | GeneralSecurityException | OutOfMemoryError ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/firmar_lote_pdfs Firma un lote de documentos pdf.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiHeader {String} [Content-Type=application/json] Tipo de contenido
     *
     * @apiParam {Long} [slot] Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {Object[]} pdfs Array de objectos con pdfs para firmar.
     * @apiParam {Boolean} [pdfs.bloquear] Bandera que en caso de estar presente con valor true, bloqueará la posibilidad de añadir más firmas al documento.
     * @apiParam {String} pdfs.id El identificador único para la solicitud.
     * @apiParam {String} pdfs.pdf Archivo pdf en base64 que se desea firmar.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "slot": 1,
     *     "pin": "12345678",
     *     "alias": "355409121073",
     *     "pdfs": [
     *         {
     *             "id": "documento0.pdf",
     *             "pdf": "MII...truncated...=="
     *         }, {
     *             "id": "documento1.pdf",
     *             "pdf": "MII...truncated...=="
     *         }
     *     ]
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "pdfs_firmados": [
     *             {
     *                 "id": "documento0.pdf",
     *                 "pdf_firmado": "MII...truncated...=="
     *             }, {
     *                 "id": "documento1.pdf",
     *                 "pdf_firmado": "MII...truncated...=="
     *             }
     *         ]
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se firmaron los pdfs correctamente."
     * }
     */
    @POST
    @Path("/firmar_lote_pdfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarLotePdf(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            if (!req.has("slot")) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                Slot[] slots = gestorSlot.listarSlots(this.getOpciones());
                if (slots.length == 1) {
                    req.put("slot", slots[0].getSlotID());
                }
            }
            if (req.has("slot") && req.has("pin") && req.has("alias") && req.has("pdfs")) {
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                Token token = GestorSlot.getInstance().obtenerSlot(req.getLong("slot"), this.getOpciones()).getToken();
                token.iniciar(req.getString("pin"));
                try {
                    if (token.obtenerCertificado(req.getString("alias")) == null) {
                        throw new RuntimeException("No se encontró un certificado con el alias proporcionado.");
                    }
                    JSONArray pdfs = req.getJSONArray("pdfs");
                    JSONArray pdfsFirmados = new JSONArray();
                    for (int i = 0; i < pdfs.length(); i++) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        FirmadorPdf.firmar(new ByteArrayInputStream(Base64.getDecoder().decode(pdfs.getJSONObject(i).getString("pdf"))), out, pdfs.getJSONObject(i).has("bloquear") && pdfs.getJSONObject(i).getBoolean("bloquear"), token, req.getString("alias"));
                        JSONObject pdf = new JSONObject();
                        pdf.put("id", pdfs.getJSONObject(i).getString("id"));
                        pdf.put("pdf_firmado", Base64.getEncoder().encodeToString(out.toByteArray()));
                        pdfsFirmados.put(pdf);
                    }

                    datos.put("pdfs_firmados", pdfsFirmados);
                    json.put("finalizado", true);
                    json.put("mensaje", "Se firmaron los pdfs correctamente!");
                } catch (RuntimeException ex) {
                    json.put("finalizado", false);
                    json.put("mensaje", ex.getMessage());
                }
                token.salir();
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos slot, pin, alias y pdfs.");
            }
        } catch (JSONException | IOException | GeneralSecurityException | OutOfMemoryError ex) {
            try {
                json.put("finalizado", false);
                json.put("mensaje", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return json.toString();
    }

    /**
     * @api {post} /api/token/sign Firma documentos pdf o json en bloque.
     * @apiGroup Sign
     * @apiVersion 1.0.0
     *
     * @apiParam {Array} [pdf] El array de documentos PDF agrupados por id y pdf
     * @apiParam {String} pdf.base64 El documento PDF en Base64
     * @apiParam {String} pdf.name El identificado único para la solicitud
     * @apiParam {Array} [json] El array de documentos JSON agrupados por id y json
     * @apiParam {String} json.base64 El documento JSON en Base64
     * @apiParam {String} json.name El identificado único para la solicitud
     * @apiParam {String} [ci] El número de documento de identidad
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "pdf":[{
     *         "base64":"data:application/pdf;base64,JVBERi0xLjQKMSAwIG9iago8PAovVGl0b....",
     *         "name":"22949.pdf"
     *     }],
     *     "json":[{
     *         "base64":"data:application/json;base64,JVBERi0xLjQKMSAwIG9iago8PAovVGl0b....",
     *         "name":"22950.pdf"
     *     }],
     *     "ci":"6817702"
     * }
     */
    @POST
    @Path("/sign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sign(String body) {
        JSONObject json = new JSONObject();
        try {
            JSONObject req = new JSONObject(body);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            Slot[] slots = gestorSlot.listarSlots(true, this.getOpciones());
            TokenSelected dt;
            JSONArray pdfs, jsons;
            if (req.has("pdf")) {
                pdfs = req.getJSONArray("pdf");
            } else {
                pdfs = new JSONArray();
            }
            if (req.has("json")) {
                jsons = req.getJSONArray("json");
            } else {
                jsons = new JSONArray();
            }
            if (pdfs.length() + jsons.length() == 0) {
                throw new RuntimeException("Por favor debe proveer al menos un archivo pdf o un archivo json.");
            }
            if (req.has("ci")) {
                dt = App.service(slots, req.getString("ci"), pdfs, jsons);
            } else {
                dt = App.service(slots, null, pdfs, jsons);
            }
            if (dt.getAlias() != null && dt.getPin() != null) {
                json.put("files", dt.getFiles());
                json.put("filesJson", dt.getFilesJson());
                return Response.ok(json.toString()).build();
            } else {
                json.put("message", "Se canceló la firma del documento");
                return Response.status(400).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
            }
        } catch (JSONException | RuntimeException ex) {
            try {
                json.put("message", ex.getMessage());
            } catch (JSONException e) {
                Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, e);
            }
            return Response.status(500).entity(json.toString()).type(MediaType.APPLICATION_JSON).build();
        }
    }
}
