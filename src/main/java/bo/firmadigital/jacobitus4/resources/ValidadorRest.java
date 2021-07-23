/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.resources;

import bo.firmadigital.jacobitus4.util.Base64StreamParser;
import bo.firmadigital.validar.CertDate;
import bo.firmadigital.validar.Validar;
import bo.firmadigital.validar.ValidarPKCS7;
import bo.firmadigital.validar.ValidarPdf;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
@Path("/")
public class ValidadorRest {
    /**
     * @api {post} https://localhost:9000/api/validar_pdf Validar firma de un documento pdf.
     * @apiGroup Validador
     * @apiVersion 1.0.0
     *
     * @apiParam {String} pdf Archivo pdf en base64 que se desea firmar.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "pdf": "MII...truncated...=="
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "firmas": [
     *             {
     *                 "noModificado": true,
     *                 "cadenaConfianza": true,
     *                 "firmadoDuranteVigencia": true,
     *                 "firmadoAntesRevocacion": true,
     *                 "versionado": false,
     *                 "timeStamp": false,
     *                 "fechaFirma": "2018-05-25T23:46:42.000Z",
     *                 "certificado": {
     *                     "ci": "1234567",
     *                     "nombreSignatario": "Juan Perez",
     *                     "cargoSignatario": "Director Ejecutivo",
     *                     "organizacionSignatario": "Perez S.A.",
     *                     "emailSignatario": "jperez@mail.com",
     *                     "nombreECA": "ADSIB",
     *                     "descripcionECA": "Entidad Certificadora Publica ADSIB",
     *                     "inicioValidez": "2017-08-04T22:46:42.000Z",
     *                     "finValidez": "2018-08-04T22:46:42.000Z",
     *                     "revocado": "2018-06-01T12:30:02.000Z"
     *                 }
     *             }
     *         ]
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se validó las firmas correctamente!"
     * }
     */
    @POST
    @Path("/validar_pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarPdf(InputStream body) {
        JSONObject json = new JSONObject();
        try {
            byte[] file = null;
            JsonFactory factory = new ObjectMapper().getJsonFactory();
            JsonParser jsonReader = factory.createJsonParser(body);
            try {
                jsonReader.nextToken();
                while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
                    String label = jsonReader.getText();
                    jsonReader.nextToken();
                    switch (label) {
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
                            Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (file != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                Validar validar = new ValidarPdf(new ByteArrayInputStream(file));
                JSONArray firmas = new JSONArray();
                datos.append("firmas", firmas);
                for (CertDate cert : validar) {
                    JSONObject firma = new JSONObject();
                    firma.put("noModificado", cert.isValid());
                    firma.put("cadenaConfianza", cert.isPKI());
                    firma.put("firmadoDuranteVigencia", cert.isActive());
                    firma.put("firmadoAntesRevocacion", cert.isOCSP());
                    firma.put("versionado", cert.isValidAlerted());
                    firma.put("timeStamp", cert.getTimeStamp() != null);
                    firma.put("fechaFirma", dateFormat.format(cert.getSignDate()));
                    JSONObject certificado = new JSONObject();
                    if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                        certificado.put("ci", cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                    } else {
                        certificado.put("ci", cert.getDatos().getNumeroDocumentoSubject());
                    }
                    certificado.put("nombreSignatario", cert.getDatos().getNombreComunSubject());
                    certificado.put("cargoSignatario", cert.getDatos().getCargoSubject());
                    certificado.put("organizacionSignatario", cert.getDatos().getOrganizacionSubject());
                    certificado.put("emailSignatario", cert.getDatos().getCorreoSubject());
                    certificado.put("nombreECA", cert.getDatos().getNombreComunIssuer());
                    certificado.put("descripcionECA", cert.getDatos().getDescripcionSubject());
                    certificado.put("inicioValidez", dateFormat.format(cert.getDatos().getInicioValidez()));
                    certificado.put("finValidez", dateFormat.format(cert.getDatos().getFinValidez()));
                    certificado.put("revocado", dateFormat.format(cert.getOCSP().getDate()));
                    firma.put("certificado", certificado);
                    firmas.put(firma);
                }

                json.put("finalizado", true);
                json.put("mensaje", "Se validó las firmas correctamente!");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos pdf.");
            }
        } catch (JSONException | IOException ex) {
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
     * @api {post} https://localhost:9000/api/validar_pkcs7 Validar firma de un documento p7s.
     * @apiGroup Validador
     * @apiVersion 1.0.0
     *
     * @apiParam {String} file Archivo p7s en base64 que se desea firmar.
     *
     * @apiParamExample {json} Request-Example:
     * {
     *     "file": "MII...truncated...=="
     * }
     *
     * @apiSuccessExample {json} Success-Response:
     * {
     *     "datos": {
     *         "firmas": [
     *             {
     *                 "noModificado": true,
     *                 "cadenaConfianza": true,
     *                 "firmadoDuranteVigencia": true,
     *                 "firmadoAntesRevocacion": true,
     *                 "versionado": false,
     *                 "timeStamp": false,
     *                 "fechaFirma": "2018-05-25T23:46:42.000Z",
     *                 "certificado": {
     *                     "ci": "1234567",
     *                     "nombreSignatario": "Juan Perez",
     *                     "cargoSignatario": "Director Ejecutivo",
     *                     "organizacionSignatario": "Perez S.A.",
     *                     "emailSignatario": "jperez@mail.com",
     *                     "nombreECA": "ADSIB",
     *                     "descripcionECA": "Entidad Certificadora Publica ADSIB",
     *                     "inicioValidez": "2017-08-04T22:46:42.000Z",
     *                     "finValidez": "2018-08-04T22:46:42.000Z",
     *                     "revocado": "2018-06-01T12:30:02.000Z"
     *                 }
     *             }
     *         ]
     *     },
     *     "finalizado": true,
     *     "mensaje": "Se validó las firmas correctamente!"
     * }
     */
    @POST
    @Path("/validar_pkcs7")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarPKCS7(InputStream body) {
        JSONObject json = new JSONObject();
        try {
            byte[] file = null;
            JsonFactory factory = new ObjectMapper().getJsonFactory();
            JsonParser jsonReader = factory.createJsonParser(body);
            try {
                jsonReader.nextToken();
                while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
                    String label = jsonReader.getText();
                    jsonReader.nextToken();
                    switch (label) {
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
                            Logger.getLogger(FirmadorRest.class.getName()).log(Level.SEVERE, null, label);
                    }
                }
            } finally {
                jsonReader.close();
            }
            if (file != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                JSONObject datos = new JSONObject();
                json.put("datos", datos);

                Validar validar = new ValidarPKCS7(new ByteArrayInputStream(file));
                JSONArray firmas = new JSONArray();
                datos.append("firmas", firmas);
                for (CertDate cert : validar) {
                    JSONObject firma = new JSONObject();
                    firma.put("noModificado", cert.isValid());
                    firma.put("cadenaConfianza", cert.isPKI());
                    firma.put("firmadoDuranteVigencia", cert.isActive());
                    firma.put("firmadoAntesRevocacion", cert.isOCSP());
                    firma.put("timeStamp", cert.getTimeStamp() != null);
                    firma.put("fechaFirma", dateFormat.format(cert.getSignDate()));
                    JSONObject certificado = new JSONObject();
                    if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                        certificado.put("ci", cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                    } else {
                        certificado.put("ci", cert.getDatos().getNumeroDocumentoSubject());
                    }
                    certificado.put("nombreSignatario", cert.getDatos().getNombreComunSubject());
                    certificado.put("cargoSignatario", cert.getDatos().getCargoSubject());
                    certificado.put("organizacionSignatario", cert.getDatos().getOrganizacionSubject());
                    certificado.put("emailSignatario", cert.getDatos().getCorreoSubject());
                    certificado.put("nombreECA", cert.getDatos().getNombreComunIssuer());
                    certificado.put("descripcionECA", cert.getDatos().getDescripcionSubject());
                    certificado.put("inicioValidez", dateFormat.format(cert.getDatos().getInicioValidez()));
                    certificado.put("finValidez", dateFormat.format(cert.getDatos().getFinValidez()));
                    certificado.put("revocado", dateFormat.format(cert.getOCSP().getDate()));
                    firma.put("certificado", certificado);
                    firmas.put(firma);
                }

                json.put("finalizado", true);
                json.put("mensaje", "Se validó las firmas correctamente!");
            } else {
                json.put("finalizado", false);
                json.put("mensaje", "Datos requeridos file.");
            }
        } catch (JSONException | IOException ex) {
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
