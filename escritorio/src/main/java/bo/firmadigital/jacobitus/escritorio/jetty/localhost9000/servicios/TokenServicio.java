package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.servicios;

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

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.firmador.base.SmartCard;
import bo.firmadigital.jacobitus.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.escritorio.comun.Config;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenCertificateEmisorDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenCertificateTitularDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenCertificateValidezDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenConnectedDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenDataDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenDataRespuestaDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenPrivateCertificateDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenPublicCertificateDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.TokenStatusDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.comun.ITokenCertificateDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.comun.RespuestaDto;

public class TokenServicio {
    public TokenServicio() {
    }

    private ConfiguracionFirmador getConfigFirmador() {
        Config config = Config.getInstance();
        ConfiguracionFirmador configFirmador = new ConfiguracionFirmador();
        configFirmador.setControlador(config.getDriver());
        configFirmador.setSoftoken(config.getToken());
        configFirmador.setDirectorioControladores(config.getDirectorioControladores());
        configFirmador.setDispositivosCompatibles(config.getDispositivosCompatibles());
        // configFirmador.setSelloTiempoHabilitado(config.isTSEnabled());
        // configFirmador.setApiSelloTiempo(config.getTS());
        // configFirmador.setJwtSelloTiempo(config.getTSJWT());
        // configFirmador.setHsmHabilitado(config.isHsmEnabled());
        // configFirmador.setTipoHsm(config.getHsmType());
        // configFirmador.setApiHsm(config.getHsmCloud());
        // configFirmador.setJwtHsm(config.getHsmJWT());
        return configFirmador;
    }

    public RespuestaDto<TokenStatusDto> status() {
        RespuestaDto<TokenStatusDto> respuesta = new RespuestaDto<TokenStatusDto>();
        try {
            List<String> listaToken = new ArrayList<String>();
            List<JSONObject> tokens = SmartCard.cards(this.getConfigFirmador());

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
            gestorSlot.setConfigFirmador(getConfigFirmador());
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

    public RespuestaDto<TokenDataRespuestaDto> data(TokenAutenticacionDto objetoDto) {
        RespuestaDto<TokenDataRespuestaDto> respuesta = new RespuestaDto<TokenDataRespuestaDto>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        GestorSlot gestorSlot = GestorSlot.getInstance();
        gestorSlot.setConfigFirmador(getConfigFirmador());

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
                        InfoCertificado infoCertificado = new InfoCertificado(cert);
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
                            x509.getValidez().setDesde(dateFormat.format(infoCertificado.getInicioValidez()));
                            x509.getValidez().setHasta(dateFormat.format(infoCertificado.getFinValidez()));
                            TokenCertificateTitularDto titular = new TokenCertificateTitularDto();
                            titular.setDnQualifier(infoCertificado.getInfoSujeto().getTipoDocumento());
                            titular.setUidNumber(infoCertificado.getInfoSujeto().getNumeroDocumento());
                            titular.setUID(infoCertificado.getInfoSujeto().getComplemento());
                            titular.setCN(infoCertificado.getInfoSujeto().getNombreComun());
                            titular.setT(infoCertificado.getInfoSujeto().getCargo());
                            titular.setO(infoCertificado.getInfoSujeto().getOrganizacion());
                            titular.setOU(infoCertificado.getInfoSujeto().getUnidadOrganizacional());
                            titular.setEmailAddress(infoCertificado.getInfoSujeto().getCorreoElectronico());
                            titular.setDescription(infoCertificado.getInfoSujeto().getDescripcion());
                            x509.setTitular(titular);
                            x509.setCommon_name(infoCertificado.getInfoSujeto().getNombreComun());
                            x509.setEmisor(new TokenCertificateEmisorDto());
                            x509.getEmisor().setCN(infoCertificado.getInfoEmisor().getNombreComun());
                            x509.getEmisor().setO(infoCertificado.getInfoEmisor().getOrganizacion());
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

        return respuesta;
    }
}
