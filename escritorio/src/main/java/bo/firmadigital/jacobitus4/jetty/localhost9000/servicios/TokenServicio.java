package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import bo.firmadigital.jacobitus.firmador.base.Opciones;
import bo.firmadigital.jacobitus.firmador.base.SmartCard;
import bo.firmadigital.jacobitus.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.validador.comun.DatosCertificado;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCertificateEmisorDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCertificateTitularDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCertificateValidezDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenConnectedDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenDataDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenDataRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenPrivateCertificateDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenPublicCertificateDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenStatusDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.ITokenCertificateDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.RespuestaDto;
import bo.firmadigital.jacobitus4.util.Config;

public class TokenServicio {
    public TokenServicio() {
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

    public RespuestaDto<TokenStatusDto> status() {
        RespuestaDto<TokenStatusDto> respuesta = new RespuestaDto<TokenStatusDto>();
        try {
            List<String> listaToken = new ArrayList<String>();
            List<JSONObject> tokens = SmartCard.cards(this.getOpciones());

            for (JSONObject token : tokens) {
                listaToken.add(token.get("name").toString());
            }

            TokenStatusDto datos = new TokenStatusDto();
            datos.setConnected(listaToken.size() > 0);
            datos.setTokens(listaToken);
            respuesta = new RespuestaDto<TokenStatusDto>();
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Lista de Tokens obtenida");
            respuesta.setDatos(datos);
            return respuesta;
        } catch (JSONException | RuntimeException ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    public RespuestaDto<TokenConnectedDto> connected() {
        RespuestaDto<TokenConnectedDto> respuesta = new RespuestaDto<TokenConnectedDto>();
        try {
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());
            Slot[] slots = gestorSlot.listarSlots();
            List<TokenDto> listaToken = new ArrayList<TokenDto>();

            for (int i = 0; i < slots.length; ++i) {
                Slot slot = slots[i];
                CK_TOKEN_INFO info = slot.detalleToken();
                TokenDto token = new TokenDto();
                token.setSlot(slot.getSlotID());
                token.setSerial((new String(info.serialNumber)).trim());
                token.setName((new String(info.manufacturerID)).trim());
                token.setModel((new String(info.model)).trim());
                listaToken.add(token);
            }

            TokenConnectedDto datos = new TokenConnectedDto();
            datos.setConnected(listaToken.size() > 0);
            datos.setTokens(listaToken);
            
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Lista de Tokens obtenida");
            respuesta.setDatos(datos);
            return respuesta;
        } catch (RuntimeException ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    @SuppressWarnings("unchecked")
    public RespuestaDto<TokenDataRespuestaDto> data(TokenAutenticacionDto objetoDto) {
        RespuestaDto<TokenDataRespuestaDto> respuesta = new RespuestaDto<TokenDataRespuestaDto>();
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("firmadigital_bo.crt");
            List<X509Certificate> intermediates = (List<X509Certificate>)fact.generateCertificates(is);
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(getOpciones());

            if (objetoDto.getSlot() != null && objetoDto.getPin() != null) {
                Slot slot = gestorSlot.obtenerSlot(objetoDto.getSlot());
                if (slot != null) {
                    IToken token = slot.getToken();
                    respuesta.setDatos(new TokenDataRespuestaDto());
                    try {
                        token.iniciar(objetoDto.getPin());
                        respuesta.setFinalizado(true);
                        respuesta.setMensaje("Datos de token obtenidos correctamente");
                        List<String> llaves = token.listarIdentificadorClaves();
                        TokenDataDto data = new TokenDataDto();
                        ((TokenDataRespuestaDto) respuesta.getDatos()).setData_token(data);
                        data.setCertificates(llaves.size());
                        data.setData(new ArrayList<ITokenCertificateDto>());
    
                        for (int i = 0; i < llaves.size(); ++i) {
                            TokenPrivateCertificateDto key = new TokenPrivateCertificateDto();
                            key.setTipo("PRIMARY_KEY");
                            key.setTipo_desc("Clave Privada");
                            key.setAlias((String) llaves.get(i));
                            key.setId((String) llaves.get(i));
                            X509Certificate cert = token.obtenerCertificado((String) llaves.get(i));
                            DatosCertificado datos = new DatosCertificado(cert);
                            key.setTiene_certificado(cert != null);
                            data.getData().add(key);
    
                            if (key.getTiene_certificado()) {
                                TokenPublicCertificateDto x509 = new TokenPublicCertificateDto();
                                x509.setTipo("X509_CERTIFICATE");
                                x509.setTipo_desc("Certificado");
                                x509.setSerialNumber(cert.getSerialNumber().toString(16));
                                x509.setAlias((String) llaves.get(i));
                                x509.setId((String) llaves.get(i));
                                String pem = "-----BEGIN CERTIFICATE-----\n";
                                pem = pem + Base64.getEncoder().encodeToString(cert.getEncoded());
                                pem = pem + "\n-----END CERTIFICATE-----";
                                x509.setPem(pem);
                                x509.setValidez(new TokenCertificateValidezDto());
                                x509.getValidez().setDesde(dateFormat.format(datos.getInicioValidez()));
                                x509.getValidez().setHasta(dateFormat.format(datos.getFinValidez()));
                                TokenCertificateTitularDto titular = new TokenCertificateTitularDto();
                                titular.setDnQualifier(datos.getTipoDocumentoSubject());
                                titular.setUidNumber(datos.getNumeroDocumentoSubject());
                                titular.setUID(datos.getComplementoSubject());
                                titular.setCN(datos.getNombreComunSubject());
                                titular.setT(datos.getCargoSubject());
                                titular.setO(datos.getOrganizacionSubject());
                                titular.setOU(datos.getUnidadOrganizacionalSubject());
                                titular.setEmailAddress(datos.getCorreoSubject());
                                titular.setDescription(datos.getDescripcionSubject());
                                x509.setTitular(titular);
                                x509.setCommon_name(datos.getNombreComunSubject());
                                x509.setEmisor(new TokenCertificateEmisorDto());
                                x509.getEmisor().setCN(datos.getNombreComunIssuer());
                                x509.getEmisor().setO(datos.getOrganizacionIssuer());
                                data.getData().add(x509);
                            }
                        }
    
                        data.setPrivate_keys(llaves.size());
                    } catch (GeneralSecurityException ex) {
                        respuesta.setFinalizado(false);
                        respuesta.setMensaje(ex.getMessage());
                    }
                    token.salir();
                } else {
                    respuesta.setFinalizado(false);
                    respuesta.setMensaje("El slot " + objetoDto.getSlot() + " no se encuentra disponible.");
                }
            } else {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Datos requeridos slot y pin.");
            }
        } catch (CertificateException ex) {
            Logger.getLogger(TokenServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }

        return respuesta;
    }
}
