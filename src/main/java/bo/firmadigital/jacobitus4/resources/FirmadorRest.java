/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import bo.firmadigital.firmar.Firmar;
import bo.firmadigital.firmar.FirmarPdf;
import bo.firmadigital.jacobitus4.pojo.CompleteSign;
import bo.firmadigital.jacobitus4.pojo.Signs;
import bo.firmadigital.jacobitus4.util.Base64StreamParser;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Slot;
import bo.firmadigital.token.Token;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
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

/**
 *
 * @author ADSIB
 */
@Path("/token")
public class FirmadorRest {
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
            gestorSlot.listarSlots();
            Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
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
     * @api {post} https://localhost:9000/api/token/firmar_pdf Firma un documento pdf.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
     *
     * @apiParam {Long} slot Número de slot en el cual se encuentra conectado el token.
     * @apiParam {String} pin Clave de seguridad requerida para acceder al token.
     * @apiParam {String} alias Identificador del certificado o clave privada contenida en el token y que se utilizará para firmar.
     * @apiParam {Boolean} [bloquear] Bandera que en caso de estar presente con valor true, bloqueará la posibilidad de añadir más firmas al documento.
     * @apiParam {String} pdf Archivo pdf en base64 que se desea firmar.
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
            String pin = null, alias = null;
            Long slot = null;
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
                        default:
                            throw new IOException("No se esperaba la etiqueta: " + label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (slot != null && pin != null && alias != null && file != null) {
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Firmar firmar = FirmarPdf.getInstance(slot, alias, pin);
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
            gestorSlot.listarSlots();
            Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
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
     * @api {post} https://localhost:9000/api/token/firmar_hash Obtiene la firma encriptando el hash.
     * @apiGroup Firmador
     * @apiVersion 1.0.0
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
            gestorSlot.listarSlots();
            Slot slot = gestorSlot.obtenerSlot(req.getLong("slot"));
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
}
