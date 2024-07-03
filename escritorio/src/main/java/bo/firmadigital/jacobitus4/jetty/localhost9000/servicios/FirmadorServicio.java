package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;

import bo.firmadigital.jacobitus.firmador.FirmadorPKCS7;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.FirmadorXml;
import bo.firmadigital.jacobitus.firmador.IFirmador;
import bo.firmadigital.jacobitus.firmador.Opciones;
import bo.firmadigital.jacobitus.firmador.TokenSelected;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus4.App;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaHashDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaHashRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaJsonDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaJsonItemRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaJsonRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaLotePdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaLotePdfRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaModoSeguroRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPdfItemDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPdfItemRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPdfRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPkcs7Dto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPkcs7RespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaXmlDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaXmlRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.RespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.pojos.CompleteSign;
import bo.firmadigital.jacobitus4.jetty.localhost9000.pojos.Signs;
import bo.firmadigital.jacobitus4.util.Config;

public class FirmadorServicio {

    public FirmadorServicio() {
    }

    private Opciones getOpciones() {
        Config config = Config.getInstance();
        Opciones opciones = new Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setDirectorioControladores(config.getDirectorioControladores());
        opciones.setDispositivosCompatibles(config.getDispositivosCompatibles());
        // opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        // opciones.setApiSelloTiempo(config.getTS());
        // opciones.setJwtSelloTiempo(config.getTSJWT());
        // opciones.setHsmHabilitado(config.isHsmEnabled());
        // opciones.setTipoHsm(config.getHsmType());
        // opciones.setApiHsm(config.getHsmCloud());
        // opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }

    public RespuestaDto<FirmaPdfRespuestaDto> firmarPdf(FirmaPdfDto objetoDto) throws Exception {
        RespuestaDto<FirmaPdfRespuestaDto> respuesta = new RespuestaDto<FirmaPdfRespuestaDto>();

        try {
            Long slot = objetoDto.getSlot();
            String pin = objetoDto.getPin();
            String alias = objetoDto.getAlias();
            boolean bloquear = objetoDto.getBloquear();
            byte[] file = Base64.getDecoder().decode(objetoDto.getPdf().getBytes("UTF-8"));
            Integer x = objetoDto.getPoint() != null ? objetoDto.getPoint().getX() : null;
            Integer y = objetoDto.getPoint() != null ? objetoDto.getPoint().getY() : null;
            String image = objetoDto.getImage() != null ? objetoDto.getImage() : null;
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpciones());
                Slot[] slots = gestorSlot.listarSlots();
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }
            if (slot != null && pin != null && alias != null && file != null) {
                FirmaPdfRespuestaDto datos = new FirmaPdfRespuestaDto();
                respuesta.setDatos(datos);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                FirmadorPdf firmar;
                if (x == null) {
                    firmar = FirmadorPdf.getInstance(slot, alias, pin, this.getOpciones());
                } else {
                    firmar = FirmadorPdf.getInstance(slot, alias, pin, image, x, y, this.getOpciones());
                }

                firmar.firmar(new ByteArrayInputStream(file), out, bloquear);
                datos.setPdf_firmado(Base64.getEncoder().encodeToString(out.toByteArray()));
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Se firmo el pdf correctamente!");
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot, pin, alias y pdf.");
            }
        } catch (GeneralSecurityException | OutOfMemoryError | IOException ex) {
            try {
                String mensaje = ex.getMessage();
                if (ex.getCause() instanceof IOException) {
                    if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                        mensaje = "Pin incorrecto, intente nuevamente.";
                    }
                }
                if (ex instanceof java.security.cert.CertificateExpiredException) {
                    mensaje = "El certificado se encuentra expirado.";
                }
                if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                    mensaje = "El certificado aún no está vigente.";
                }
                if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                    if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                        mensaje = "Por favor verifique el pin.";
                    }
                }
                if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                    if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                        mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                    }
                }
                respuesta.setFinalizado(false);
                respuesta.setMensaje(mensaje);
            } catch (Exception ex1) {
                Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
            }
        }

        return respuesta;
    }

    public RespuestaDto<FirmaPkcs7RespuestaDto> firmarPkcs7(FirmaPkcs7Dto objetoDto) throws Exception {
        RespuestaDto<FirmaPkcs7RespuestaDto> respuesta = new RespuestaDto<FirmaPkcs7RespuestaDto>();

        try {
            Long slot = objetoDto.getSlot();
            String pin = objetoDto.getPin();
            String alias = objetoDto.getAlias();
            Boolean detached = objetoDto.getDetached();
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes("UTF-8"));
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpciones());
                Slot[] slots = gestorSlot.listarSlots();
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }

            if (slot != null && pin != null && alias != null && file != null) {
                FirmaPkcs7RespuestaDto datos = new FirmaPkcs7RespuestaDto();
                respuesta.setDatos(datos);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                IFirmador firmar = FirmadorPKCS7.getInstance(slot, alias, pin, this.getOpciones());
                firmar.firmar(new ByteArrayInputStream(file), out, detached);
                datos.setPkcs7(Base64.getEncoder().encodeToString(out.toByteArray()));
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Se firmo el archivo correctamente!");
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot, pin, alias y file.");
            }
        } catch (GeneralSecurityException | OutOfMemoryError | IOException ex) {
            try {
                String mensaje = ex.getMessage();
                if (ex.getCause() instanceof IOException) {
                    if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                        mensaje = "Pin incorrecto, intente nuevamente.";
                    }
                }
                if (ex instanceof java.security.cert.CertificateExpiredException) {
                    mensaje = "El certificado se encuentra expirado.";
                }
                if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                    mensaje = "El certificado aún no está vigente.";
                }
                if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                    if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                        mensaje = "Por favor verifique el pin.";
                    }
                }
                if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                    if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                        mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                    }
                }
                respuesta.setFinalizado(false);
                respuesta.setMensaje(mensaje);
            } catch (Exception ex1) {
                Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex1);
            }
        }

        return respuesta;
    }

    public RespuestaDto<FirmaXmlRespuestaDto> firmarXml(FirmaXmlDto objetoDto) {
        RespuestaDto<FirmaXmlRespuestaDto> respuesta = new RespuestaDto<FirmaXmlRespuestaDto>();

        try {
            Long slot = objetoDto.getSlot();
            String pin = objetoDto.getPin();
            String alias = objetoDto.getAlias();
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes("UTF-8"));
            String node = objetoDto.getNode();
            String digest = objetoDto.getDigest();
            String signatureAlgorithm = objetoDto.getSignatureAlgorithm();
            Boolean enveloped = objetoDto.getEnveloped();
            Boolean prefix = objetoDto.getPrefix();
            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpciones());
                Slot[] slots = gestorSlot.listarSlots();
                if (slots.length == 1) {
                    slot = slots[0].getSlotID();
                }
            }

            if (slot != null && pin != null && alias != null && file != null) {
                FirmaXmlRespuestaDto datos = new FirmaXmlRespuestaDto();
                respuesta.setDatos(datos);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
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

                firmar.firmar(new ByteArrayInputStream(file), out, enveloped, prefix);
                datos.setXml(Base64.getEncoder().encodeToString(out.toByteArray()));
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Se firmo el archivo correctamente!");
                return respuesta;
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot, pin, alias y file.");
            }
        } catch (GeneralSecurityException | OutOfMemoryError | IOException ex) {
            try {
                String mensaje = ex.getMessage();
                if (ex.getCause() instanceof IOException) {
                    if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                        mensaje = "Pin incorrecto, intente nuevamente.";
                    }
                }
                if (ex instanceof java.security.cert.CertificateExpiredException) {
                    mensaje = "El certificado se encuentra expirado.";
                }
                if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                    mensaje = "El certificado aún no está vigente.";
                }
                if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                    if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                        mensaje = "Por favor verifique el pin.";
                    }
                }
                if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                    if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                        mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                    }
                }
                respuesta.setFinalizado(false);
                respuesta.setMensaje(mensaje);
            } catch (Exception ex1) {
                Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
            }
        }

        return respuesta;
    }

    public RespuestaDto<FirmaJsonRespuestaDto> firmarJson(FirmaJsonDto objetoDto) {
        RespuestaDto<FirmaJsonRespuestaDto> respuesta = new RespuestaDto<FirmaJsonRespuestaDto>();

        try {
            byte[] dataByte = Base64.getDecoder().decode(objetoDto.getData());
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
            IToken token = slot.getToken();
            token.iniciar(objetoDto.getPin());
            PrivateKey pk = token.obtenerClavePrivada(objetoDto.getAlias());
            if (pk == null) {
                token.salir();
                throw new KeyStoreException("No se encontró la clave con alias: " + objetoDto.getAlias());
            }

            JWSSigner signer = new RSASSASigner(pk);
            boolean inicial = true;

            CompleteSign enviadoJson;
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
                jwsObject = new JWSObject((new JWSHeader.Builder(JWSAlgorithm.RS256)).build(), new Payload(dataByte));
                enviadoJson.setPayload(new String(Base64.getEncoder().encode(dataByte), StandardCharsets.UTF_8));
                enviadoJson.setSignatures(new ArrayList<Signs>());
            } else {
                jwsObject = new JWSObject((new JWSHeader.Builder(JWSAlgorithm.RS256)).build(),
                        new Payload(enviadoJson.getPayload()));
            }

            try {
                jwsObject.sign(signer);
            } catch (JOSEException ex) {
                throw new RuntimeException("Error al firmar: " + ex.getMessage());
            }

            X509Certificate cert = token.obtenerCertificado(objetoDto.getAlias());
            String pemCert = Base64.getEncoder().encodeToString(cert.getEncoded());
            token.salir();
            Signs sign = new Signs();
            Map<String, Object> mapa = new HashMap<String, Object>();
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
            String resultado = (String) buffer.lines().collect(Collectors.joining("\n"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            Calendar calendar = Calendar.getInstance();
            String fechaFirma = dateFormat.format(new Timestamp(calendar.getTime().getTime()));
            FirmaJsonRespuestaDto datos = new FirmaJsonRespuestaDto();
            datos.setJson_firmado(Base64.getEncoder().encodeToString(resultado.getBytes()));
            X500Name x500Name = (new JcaX509CertificateHolder(token.obtenerCertificado(objetoDto.getAlias())))
                    .getSubject();
            datos.setCn(IETFUtils
                    .valueToString(x500Name.getRDNs(new ASN1ObjectIdentifier("2.5.4.3"))[0].getFirst().getValue()));
            datos.setFecha_firma(fechaFirma);
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se firmo la solicitud correctamente!");
            respuesta.setDatos(datos);
        } catch (IOException | GeneralSecurityException ex) {
            try {
                String mensaje = ex.getMessage();
                if (ex.getCause() instanceof IOException) {
                    if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                        mensaje = "Pin incorrecto, intente nuevamente.";
                    }
                }
                if (ex instanceof java.security.cert.CertificateExpiredException) {
                    mensaje = "El certificado se encuentra expirado.";
                }
                if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                    mensaje = "El certificado aún no está vigente.";
                }
                if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                    if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                        mensaje = "Por favor verifique el pin.";
                    }
                }
                if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                    if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                        mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                    }
                }
                respuesta.setFinalizado(false);
                respuesta.setMensaje(mensaje);
            } catch (Exception ex1) {
                Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
            }
        }

        return respuesta;
    }

    public RespuestaDto<FirmaLotePdfRespuestaDto> firmarLotePdfs(FirmaLotePdfDto objetoDto) {
        RespuestaDto<FirmaLotePdfRespuestaDto> respuesta = new RespuestaDto<FirmaLotePdfRespuestaDto>();
        try {
            Long slot = objetoDto.getSlot();
            String pin = objetoDto.getPin();
            String alias = objetoDto.getAlias();
            List<FirmaPdfItemDto> pdfs = objetoDto.getPdfs();

            if (slot == null) {
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(this.getOpciones());
                Slot[] slots = gestorSlot.listarSlots();
                if (slots.length == 1) {
                    objetoDto.setSlot(slots[0].getSlotID());
                }
            }
            if (slot != null && pin != null && alias != null && pdfs != null) {
                FirmaLotePdfRespuestaDto datos = new FirmaLotePdfRespuestaDto();
                
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(this.getOpciones());
                IToken token = gestorSlot.obtenerSlot(slot).getToken();
                token.iniciar(pin);
                try {
                    if (token.obtenerCertificado(alias) == null) {
                        throw new RuntimeException("No se encontró un certificado con el alias proporcionado.");
                    }
                    List<FirmaPdfItemRespuestaDto> pdfsFirmados = new ArrayList<FirmaPdfItemRespuestaDto>();
                    for (int i = 0; i < pdfs.size(); i++) {
                        boolean bloquear = pdfs.get(i).getBloquear() != null && pdfs.get(i).getBloquear();
                        byte[] file = Base64.getDecoder().decode(pdfs.get(i).getPdf().getBytes("UTF-8"));

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        FirmadorPdf.firmar(new ByteArrayInputStream(file), out, bloquear, token, alias);
                        FirmaPdfItemRespuestaDto pdfItem = new FirmaPdfItemRespuestaDto();
                        pdfItem.setId(pdfs.get(i).getId());
                        pdfItem.setPdf_firmado(Base64.getEncoder().encodeToString(out.toByteArray()));
                        pdfsFirmados.add(pdfItem);
                    }

                    datos.setPdfs_firmados(pdfsFirmados);

                    respuesta.setDatos(datos);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Se firmaron los pdfs correctamente!");
                } catch (RuntimeException ex) {
                    respuesta.setFinalizado(false);
                    respuesta.setMensaje(ex.getMessage());
                }
                token.salir();
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot, pin, alias y pdfs.");
            }
        } catch (IOException | GeneralSecurityException | OutOfMemoryError ex) {
            String mensaje = ex.getMessage();
            if (ex.getCause() instanceof IOException) {
                if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                    mensaje = "Pin incorrecto, intente nuevamente.";
                }
            }
            if (ex instanceof java.security.cert.CertificateExpiredException) {
                mensaje = "El certificado se encuentra expirado.";
            }
            if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                mensaje = "El certificado aún no está vigente.";
            }
            if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                    mensaje = "Por favor verifique el pin.";
                }
            }
            if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                    mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                }
            }
            respuesta.setFinalizado(false);
            respuesta.setMensaje(mensaje);
        }
        return respuesta;
        
    }

    public RespuestaDto<FirmaHashRespuestaDto> firmarHash(FirmaHashDto objetoDto) {
        RespuestaDto<FirmaHashRespuestaDto> respuesta = new RespuestaDto<FirmaHashRespuestaDto>();
        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot[] slots = gestorSlot.listarSlots();
            if (objetoDto.getSlot() == null && slots.length == 1) {
                objetoDto.setSlot(slots[0].getSlotID());
            }

            if (objetoDto.getSlot() != null && objetoDto.getPin() != null && objetoDto.getAlias() != null
                    && objetoDto.getHash() != null) {
                Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
                IToken token = slot.getToken();
                token.iniciar(objetoDto.getPin());
                FirmaHashRespuestaDto datos = new FirmaHashRespuestaDto();
                Signature signature = Signature.getInstance("SHA256withRSA");
                PrivateKey pk = token.obtenerClavePrivada(objetoDto.getAlias());
                if (pk == null) {
                    token.salir();
                    throw new KeyStoreException("No se encontró la clave con alias: " + objetoDto.getAlias());
                }

                signature.initSign(pk);
                signature.update(Base64.getDecoder().decode(objetoDto.getHash()));
                byte[] signed = signature.sign();
                token.salir();
                datos.setFirma(Base64.getEncoder().encodeToString(signed));
                respuesta.setDatos(datos);
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Firma realizada correctamente.");
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot, pin, alias y hash.");
            }
        } catch (GeneralSecurityException ex) {
            try {
                String mensaje = ex.getMessage();
                if (ex.getCause() instanceof IOException) {
                    if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                        mensaje = "Pin incorrecto, intente nuevamente.";
                    }
                }
                if (ex instanceof java.security.cert.CertificateExpiredException) {
                    mensaje = "El certificado se encuentra expirado.";
                }
                if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                    mensaje = "El certificado aún no está vigente.";
                }
                if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                    if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                        mensaje = "Por favor verifique el pin.";
                    }
                }
                if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                    if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                        mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                    }
                }
                respuesta.setFinalizado(false);
                respuesta.setMensaje(mensaje);
            } catch (Exception ex1) {
                Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex1.getMessage(), ex1);
            }
        }

        return respuesta;
    }

    public RespuestaDto<FirmaModoSeguroRespuestaDto> firmarModoSeguro(FirmaModoSeguroDto objetoDto) throws Exception {
        RespuestaDto<FirmaModoSeguroRespuestaDto> respuesta = new RespuestaDto<FirmaModoSeguroRespuestaDto>();
        GestorSlot gestorSlot = GestorSlot.getInstance();
        gestorSlot.setOpciones(this.getOpciones());
        Slot[] slots = gestorSlot.listarSlots(true);
        TokenSelected tokenSelected;
        try {
            FirmaModoSeguroRespuestaDto datos = new FirmaModoSeguroRespuestaDto();
            try {
                ObjectMapper om = new ObjectMapper();
                JSONArray pdfs, jsons;
                if (objetoDto.getPdfs() != null) {
                    pdfs = new JSONArray(om.writeValueAsString(objetoDto.getPdfs()));
                } else {
                    pdfs = new JSONArray();
                }
                if (objetoDto.getJsons() != null) {
                    jsons = new JSONArray(om.writeValueAsString(objetoDto.getJsons()));
                } else {
                    jsons = new JSONArray();
                }
                if (pdfs.length() + jsons.length() == 0) {
                    throw new RuntimeException("Por favor debe proveer al menos un archivo pdf o un archivo json.");
                }
                if (objetoDto.getCi() != null) {
                    tokenSelected = App.service(slots, objetoDto.getCi(), pdfs, jsons);
                } else {
                    tokenSelected = App.service(slots, null, pdfs, jsons);
                }
                if (tokenSelected.getAlias() != null && tokenSelected.getPin() != null) {
                    List<FirmaPdfItemRespuestaDto> listaPdf = Arrays.asList(om.readValue(tokenSelected.getFiles().toString(), FirmaPdfItemRespuestaDto[].class));
                    datos.setPdfs_firmados(listaPdf);
                    List<FirmaJsonItemRespuestaDto> listaJson = Arrays.asList(om.readValue(tokenSelected.getFilesJson().toString(), FirmaJsonItemRespuestaDto[].class));
                    datos.setJsons_firmados(listaJson);
                    respuesta.setDatos(datos);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Se firmaron los documentos correctamente!");
                } else {
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Se canceló la firma de los documentos");
                }
            } catch (RuntimeException ex) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(ex.getMessage());
            }
        } catch (Exception ex) {
            String mensaje = ex.getMessage();
            if (ex.getCause() instanceof IOException) {
                if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                    mensaje = "Pin incorrecto, intente nuevamente.";
                }
            }
            if (ex instanceof java.security.cert.CertificateExpiredException) {
                mensaje = "El certificado se encuentra expirado.";
            }
            if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                mensaje = "El certificado aún no está vigente.";
            }
            if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                    mensaje = "Por favor verifique el pin.";
                }
            }
            if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                    mensaje = "El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.";
                }
            }
            respuesta.setFinalizado(false);
            respuesta.setMensaje(mensaje);
        }
        return respuesta;
    }
}
