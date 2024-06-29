package bo.firmadigital.jacobitus4.localhost9000.servicios;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;

import bo.firmadigital.jacobitus.firmador.Opciones;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.token.TokenPKCS12;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenChangePinDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenCreacionCsrDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenCreacionCsrSubjectItemDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenCreacionDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenCsrDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenDataDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenDataRespuestaDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenPemDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenPrivateCertificateDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenSolicitudDetalleDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenSolicitudDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.TokenSolicitudRespuestaDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.comun.ITokenCertificateDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.comun.RespuestaDto;
import bo.firmadigital.jacobitus4.util.Config;

public class PlataformaServicio {
    public PlataformaServicio() {
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

    public RespuestaDto<String> create(TokenCreacionDto objetoDto) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();

        try {
            respuesta.setFinalizado(false);
            if (objetoDto.getPin().length() < 8) {
                respuesta.setMensaje("El pin es muy corto.");
            } else {
                int num = 0;
                int may = 0;
                int minu = 0;
                char[] password = objetoDto.getPin().toCharArray();

                for (int i = 0; i < objetoDto.getPin().length(); ++i) {
                    if (password[i] >= '0' && password[i] <= '9') {
                        ++num;
                    } else if (password[i] >= 'A' && password[i] <= 'Z') {
                        ++may;
                    } else if (password[i] >= 'a' && password[i] <= 'z') {
                        ++minu;
                    }
                }

                if (num >= 1 && may >= 1 && minu >= 1) {
                    Config config = Config.getInstance();
                    Slot slot = new Slot(config.getTokenToCreate(), this.getOpciones());
                    TokenPKCS12 token = new TokenPKCS12(this.getOpciones(), slot);

                    try {
                        token.crear(objetoDto.getPin());
                        respuesta.setFinalizado(true);
                        respuesta.setMensaje("Token generado correctamente.");
                        token.salir();
                    } catch (GeneralSecurityException var10) {
                        respuesta.setMensaje(var10.getMessage());
                    }
                } else {
                    respuesta.setMensaje(
                            "El pin debe contener al menos un n\u00famero, una letra may\u00fascula y una letra min\u00fascula.");
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<TokenDataRespuestaDto> generateKeypar(TokenAutenticacionDto objetoDto) {
        RespuestaDto<TokenDataRespuestaDto> respuesta = new RespuestaDto<TokenDataRespuestaDto>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
            IToken token = slot.getToken();
            token.iniciar(objetoDto.getPin());
            BigInteger max = new BigInteger("1000000000000");
            BigInteger id = (new BigInteger(max.bitLength(), new SecureRandom())).mod(max);
            token.generarClaves(id.toString(), objetoDto.getPin(), Integer.parseInt(objetoDto.getSlot().toString()));
            TokenDataRespuestaDto datos = new TokenDataRespuestaDto();
            respuesta.setDatos(datos);
            TokenDataDto data_token = new TokenDataDto();
            datos.setData_token(data_token);
            data_token.setCertificates(0);
            List<ITokenCertificateDto> data = new ArrayList<ITokenCertificateDto>();
            data_token.setData(data);
            TokenPrivateCertificateDto pk = new TokenPrivateCertificateDto();
            data.add(pk);
            pk.setTipo("PRIMARY_KEY");
            pk.setTipo_desc("Clave Privada");
            pk.setAlias(id.toString());
            pk.setTiene_certificado(false);
            data_token.setPrivate_keys(1);
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se genero el par de claves correctamente.");
            token.salir();
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<TokenCsrDto> generateCsr(TokenCreacionCsrDto objetoDto) {
        RespuestaDto<TokenCsrDto> respuesta = new RespuestaDto<TokenCsrDto>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
            IToken token = slot.getToken();
            TokenCsrDto datos = new TokenCsrDto();
            respuesta.setDatos(datos);

            try {
                token.iniciar(objetoDto.getPin());
                List<TokenCreacionCsrSubjectItemDto> lista = objetoDto.getSubject();
                JSONArray array = new JSONArray();

                for (int i = 0; i < lista.size(); ++i) {
                    JSONObject item = new JSONObject();
                    item.put("oid", ((TokenCreacionCsrSubjectItemDto) lista.get(i)).getOid());
                    item.put("value", ((TokenCreacionCsrSubjectItemDto) lista.get(i)).getValue());
                    array.put(item);
                }

                datos.setCsr(token.generarCSR(objetoDto.getAlias_certificado(), array));
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Se genero el CSR correctamente");
            } catch (GeneralSecurityException ex) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(ex.getMessage());
            }

            token.salir();
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<String> cargarPem(TokenPemDto objetoDto) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
            IToken token = slot.getToken();
            respuesta.setDatos(null);

            try {
                token.iniciar(objetoDto.getPin());
                token.cargarCertificado(new String(Base64.getDecoder().decode(objetoDto.getPem()), "UTF-8"),
                        objetoDto.getId());
                respuesta.setFinalizado(true);
                respuesta.setMensaje("El certificado fue adicionado correctamente");
            } catch (UnsupportedEncodingException | GeneralSecurityException var7) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(var7.getMessage());
            }

            token.salir();
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<String> cambiarPin(TokenChangePinDto objetoDto) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());

            try {
                IToken token = slot.getToken();
                token.modificarPin(objetoDto.getOld_pin(), objetoDto.getNew_pin());
                respuesta.setFinalizado(true);
                respuesta.setMensaje("El pin se cambi\u00f3 correctamente");
            } catch (RuntimeException var6) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(var6.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<String> cambiarPinSO(TokenChangePinDto objetoDto) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());

            try {
                IToken token = slot.getToken();
                token.modificarPinSo(objetoDto.getOld_pin(), objetoDto.getNew_pin());
                respuesta.setFinalizado(true);
                respuesta.setMensaje("El pin se cambi\u00f3 correctamente");
            } catch (RuntimeException var6) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(var6.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<String> testPinSO(TokenAutenticacionDto objetoDto) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();

        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());

            try {
                IToken token = slot.getToken();
                token.test(objetoDto.getPin());
                respuesta.setFinalizado(true);
                respuesta.setMensaje("El pin se valid\u00f3 correctamente");
            } catch (RuntimeException var6) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(var6.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }

    public RespuestaDto<List<TokenSolicitudRespuestaDto>> firmarSolicitudes(TokenSolicitudDto objetoDto) {
        RespuestaDto<List<TokenSolicitudRespuestaDto>> respuesta = new RespuestaDto<List<TokenSolicitudRespuestaDto>>();
        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(this.getOpciones());
            gestorSlot.listarSlots();
            Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
            IToken token = slot.getToken();
            token.iniciar(objetoDto.getPin());
            List<TokenSolicitudDetalleDto> data = objetoDto.getData();
            List<TokenSolicitudRespuestaDto> datos = new ArrayList<TokenSolicitudRespuestaDto>();
            for (int i = 0; i < data.size(); i++) {
                TokenSolicitudRespuestaDto element = new TokenSolicitudRespuestaDto();
                element.setId(data.get(i).getId());
                PrivateKey pk = token.obtenerClavePrivada(objetoDto.getAlias());
                if (pk == null) {
                    token.salir();
                    throw new KeyStoreException("No se encontró la clave con alias: " + objetoDto.getAlias());
                }
                JWSSigner jwsSigner = new RSASSASigner(pk);
                JWSHeader.Builder builder = new JWSHeader.Builder(JWSAlgorithm.RS256);
                if (data.get(i).getUrl() == null) {
                    builder.x509CertURL(new URI("https://agencia.firmadigital.bo/services_ar/certificado?serial_number=" + token.obtenerCertificado(objetoDto.getAlias()).getSerialNumber()));
                } else {
                    if (data.get(i).getUrl().contains("?")) {
                        builder.x509CertURL(new URI(data.get(i).getUrl()));
                    } else {
                        builder.x509CertURL(new URI(data.get(i).getUrl() + "?serial_number=" + token.obtenerCertificado(objetoDto.getAlias()).getSerialNumber()));
                    }
                }
                JWSObject jwsObject = new JWSObject(builder.build(),new Payload(data.get(i).getPayload()));
                jwsObject.sign(jwsSigner);
                element.setJws(jwsObject.serialize());
                datos.add(element);
            }
            token.salir();
            respuesta.setDatos(datos);
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se firmo las solicitudes correctamente!");
        } catch (GeneralSecurityException | URISyntaxException | JOSEException ex) {
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
