package bo.firmadigital.jacobitus4.jetty.localhost3200.servicios;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONObject;

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.base.SmartCard;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.validador.base.Validador;
import bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.CertificadoDto;
import bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.FirmaPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.FirmaPdfRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost4637.dtos.comun.RespuestaDto;
import bo.firmadigital.jacobitus4.util.Config;

public class FirmadorServicio {

    private static Slot[] slots;

    public FirmadorServicio() {
    }

    private bo.firmadigital.jacobitus.firmador.base.Opciones getOpcionesFirmador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.firmador.base.Opciones opciones = new bo.firmadigital.jacobitus.firmador.base.Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setDirectorioControladores(config.getDirectorioControladores());
        opciones.setDispositivosCompatibles(config.getDispositivosCompatibles());
        opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        // opciones.setApiSelloTiempo(config.getTS());
        // opciones.setJwtSelloTiempo(config.getTSJWT());
        // opciones.setHsmHabilitado(config.isHsmEnabled());
        // opciones.setTipoHsm(config.getHsmType());
        // opciones.setApiHsm(config.getHsmCloud());
        // opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }
    
    private bo.firmadigital.jacobitus.validador.base.Opciones getOpcionesValidador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.validador.base.Opciones opciones = new bo.firmadigital.jacobitus.validador.base.Opciones();
        opciones.setProxyHabilitado(config.isProxyEnabled());
        opciones.setServidorProxy(config.getProxyIP());
        opciones.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
        return opciones;
    }

    public String inicio() {
        return "Ok";
    }

    public RespuestaDto<List<String>> tokens() {
        RespuestaDto<List<String>> respuesta = new RespuestaDto<List<String>>();
        try {
            try {
                List<String> datos = new ArrayList<String>();
                List<JSONObject> tokens = SmartCard.cards(this.getOpcionesFirmador());
                for (JSONObject token : tokens) {
                    datos.add(token.getString("name"));
                }
                respuesta.setDatos(datos);
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Tokens detectados");
            } catch (RuntimeException ex) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(ex.getMessage());
            }
        } catch (org.codehaus.jettison.json.JSONException ex) {
            Logger.getLogger(FirmadorServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return respuesta;
    }

    public RespuestaDto<String> start(String pin) {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();
        if (pin == null) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje("El pin es nulo. Utilice el metodo setParametrosConexion(ruta,pin)");
        } else {
            try {
                if (slots != null) {
                    if (slots.length == 1) {
                        slots[0].getToken().salir();
                    }
                    slots = null;
                }
                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(this.getOpcionesFirmador());
                slots = gestorSlot.listarSlots();
                if (slots.length == 1) {
                    slots[0].getToken().iniciar(pin);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Autenticacion correcta");
                } else {
                    respuesta.setFinalizado(false);
                    if (slots.length > 1) {
                        respuesta.setMensaje("Se encontró más de un token conectado.");
                    } else {
                        respuesta.setMensaje("No se encontró ningún token conectado.");
                    }
                    slots = null;
                }
            } catch (RuntimeException | GeneralSecurityException ex) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(ex.getMessage());
            }
        }
        return respuesta;
    }

    public RespuestaDto<List<CertificadoDto>> certs() {
        RespuestaDto<List<CertificadoDto>> respuesta = new RespuestaDto<List<CertificadoDto>>();
        try {
            if (slots == null) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Primero debe iniciar sesión.");
            } else {
                if (slots.length == 1) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    IToken token = slots[0].getToken();
                    List<String> listaAlias = token.listarIdentificadorClaves();
                    List<CertificadoDto> listaCertificadoDto = new ArrayList<CertificadoDto>();
                    for (String alias : listaAlias) {
                        InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                        CertificadoDto certificadoDto = new CertificadoDto();
                        certificadoDto.setEsFirmaBolivia(Validador.verificarPKI(infoCertificado.getX509certificado()));
                        certificadoDto.setNumeroSerie(infoCertificado.getX509certificado().getSerialNumber());
                        certificadoDto.setNombreComunIssuer(infoCertificado.getInfoEmisor().getNombreComun());
                        certificadoDto.setOrganizacionIssuer(infoCertificado.getInfoEmisor().getOrganizacion());
                        certificadoDto.setNombreComunSubject(infoCertificado.getInfoSujeto().getNombreComun());
                        certificadoDto.setCi(infoCertificado.getInfoSujeto().getNumeroDocumento());
                        certificadoDto.setComplemento(infoCertificado.getInfoSujeto().getComplemento());
                        certificadoDto.setOrganizacionSubject(infoCertificado.getInfoSujeto().getOrganizacion());
                        certificadoDto.setUnidadOrganizacionalSubject(infoCertificado.getInfoSujeto().getUnidadOrganizacional());
                        certificadoDto.setInicioValidez(dateFormat.format(infoCertificado.getInicioValidez()));
                        certificadoDto.setFinValidez(dateFormat.format(infoCertificado.getFinValidez()));
                        certificadoDto.setAlias(alias);
                        certificadoDto.setEsValido(infoCertificado.getInicioValidez().compareTo(new Date()) < 0 && infoCertificado.getFinValidez().compareTo(new Date()) > 0);
                        Validador.OCSPState state = Validador.verificarOcsp(infoCertificado.getX509certificado(), new Date(), this.getOpcionesValidador()).getState();
                        if (state == Validador.OCSPState.OK) {
                            certificadoDto.setOCSP("no revocado");
                        } else {
                            certificadoDto.setOCSP(state.toString());
                        }
                        listaCertificadoDto.add(certificadoDto);
                    }
                    respuesta.setDatos(listaCertificadoDto);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Certificados obtenidos correctamente");
                } else {
                    respuesta.setFinalizado(false);
                    if (slots.length > 1) {
                        respuesta.setMensaje("Se encontró más de un token conectado.");
                    } else {
                        respuesta.setMensaje("Por favor no desconecte el token.");
                    }
                }
            }
        } catch (RuntimeException | GeneralSecurityException ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
        }
        return respuesta;
    }

    public RespuestaDto<FirmaPdfRespuestaDto> sign(FirmaPdfDto objetoDto) {
        RespuestaDto<FirmaPdfRespuestaDto> respuesta = new RespuestaDto<FirmaPdfRespuestaDto>();
        try {
            if (slots == null) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje("Primero debe iniciar sesión.");
            } else {
                if (slots.length == 1) {
                    if (objetoDto.getNombre_archivo() != null && objetoDto.getAlias() != null && objetoDto.getPdf_base64() != null) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        IToken token = slots[0].getToken();
                        X509Certificate certificate = token.obtenerCertificado(objetoDto.getAlias());
                        if (certificate == null) {
                            respuesta.setFinalizado(false);
                            respuesta.setMensaje("No se encontró un certificado con el alias solicitado.");
                        } else {
                            InfoCertificado infoCertificado = new InfoCertificado(objetoDto.getAlias(), certificate);
                            CertificadoDto certificadoDto = new CertificadoDto();
                            certificadoDto.setEsFirmaBolivia(Validador.verificarPKI(infoCertificado.getX509certificado()));
                            certificadoDto.setNumeroSerie(infoCertificado.getX509certificado().getSerialNumber());
                            certificadoDto.setNombreComunIssuer(infoCertificado.getInfoEmisor().getNombreComun());
                            certificadoDto.setOrganizacionIssuer(infoCertificado.getInfoEmisor().getOrganizacion());
                            certificadoDto.setNombreComunSubject(infoCertificado.getInfoSujeto().getNombreComun());
                            certificadoDto.setCi(infoCertificado.getInfoSujeto().getNumeroDocumento());
                            certificadoDto.setComplemento(infoCertificado.getInfoSujeto().getComplemento());
                            certificadoDto.setOrganizacionSubject(infoCertificado.getInfoSujeto().getOrganizacion());
                            certificadoDto.setUnidadOrganizacionalSubject(infoCertificado.getInfoSujeto().getUnidadOrganizacional());
                            certificadoDto.setInicioValidez(dateFormat.format(infoCertificado.getInicioValidez()));
                            certificadoDto.setFinValidez(dateFormat.format(infoCertificado.getFinValidez()));
                            certificadoDto.setAlias(objetoDto.getAlias());
                            certificadoDto.setEsValido(infoCertificado.getInicioValidez().compareTo(new Date()) < 0 && infoCertificado.getFinValidez().compareTo(new Date()) > 0);
                            Validador.OCSPState state = Validador.verificarOcsp(infoCertificado.getX509certificado(), new Date(), this.getOpcionesValidador()).getState();
                            if (state == Validador.OCSPState.OK) {
                                certificadoDto.setOCSP("no revocado");
                            } else {
                                certificadoDto.setOCSP(state.toString());
                            }
                            FirmaPdfRespuestaDto datos = new FirmaPdfRespuestaDto();
                            byte[] pdf = Base64.getDecoder().decode(objetoDto.getPdf_base64());
                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                            FirmadorPdf.firmar(new ByteArrayInputStream(pdf), os, false, token, objetoDto.getAlias());
                            datos.setPdf_base64(Base64.getEncoder().encodeToString(os.toByteArray()));
                            datos.setNombre_archivo(objetoDto.getNombre_archivo());
                            datos.setCertificado(certificadoDto);
                            respuesta.setDatos(datos);
                            respuesta.setFinalizado(true);
                            respuesta.setMensaje("Certificados obtenidos correctamente");
                        }
                    } else {
                        respuesta.setFinalizado(false);
                        respuesta.setMensaje("Parámetros obligatorios (nombre_archivo, alias, pdf_base64).");
                    }
                } else {
                    respuesta.setFinalizado(false);
                    if (slots.length > 1) {
                        respuesta.setMensaje("Se encontró más de un token conectado.");
                    } else {
                        respuesta.setMensaje("Por favor no desconecte el token.");
                    }
                }
            }
        } catch (RuntimeException | GeneralSecurityException | IOException ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
        }
        return respuesta;
    }

    public RespuestaDto<String> finish() {
        RespuestaDto<String> respuesta = new RespuestaDto<String>();
        if (slots == null) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje("No se encontró una sesión activa.");
        } else {
            try {
                if (slots.length == 1) {
                    slots[0].getToken().salir();
                    slots = null;
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Cerrado sesión correctamente");
                } else {
                    respuesta.setFinalizado(false);
                    if (slots.length > 1) {
                        respuesta.setMensaje("Se encontró más de un token conectado.");
                    } else {
                        respuesta.setMensaje("No se encontró ningún token conectado.");
                    }
                }
            } catch (RuntimeException ex) {
                respuesta.setFinalizado(false);
                respuesta.setMensaje(ex.getMessage());
            }
        }
        return respuesta;
    }

    public RespuestaDto<Void> estadoToken() {
        RespuestaDto<Void> respuesta = new RespuestaDto<Void>();
        respuesta.setFinalizado(true);
        respuesta.setMensaje("False");
        return respuesta;
    }

    public String matarProceso() {
        return "Ok";
    }

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
