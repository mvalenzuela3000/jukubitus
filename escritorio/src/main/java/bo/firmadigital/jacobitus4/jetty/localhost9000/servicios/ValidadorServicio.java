package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.validador.ValidadorJws;
import bo.firmadigital.jacobitus.validador.ValidadorPKCS7;
import bo.firmadigital.jacobitus.validador.ValidadorPdf;
import bo.firmadigital.jacobitus.validador.ValidadorXml;
import bo.firmadigital.jacobitus.validador.base.ConfiguracionValidador;
import bo.firmadigital.jacobitus.validador.comun.Firma;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.CertificadoDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.ValidacionArchivoDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.ValidacionArchivoRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.ValidacionPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.ValidacionPdfRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.RespuestaDto;

public class ValidadorServicio {
    public ValidadorServicio() {
    }

    public RespuestaDto<ValidacionPdfRespuestaDto> validarPdf(ValidacionPdfDto objetoDto) {
        RespuestaDto<ValidacionPdfRespuestaDto> respuesta = new RespuestaDto<ValidacionPdfRespuestaDto>();
        try {
            byte[] file = Base64.getDecoder().decode(objetoDto.getPdf().getBytes(StandardCharsets.UTF_8));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            ConfiguracionValidador configValidador = new ConfiguracionValidador();
            ValidadorPdf validar = new ValidadorPdf(new ByteArrayInputStream(file), configValidador);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.getIntegridad());
                firmaDto.setCadenaConfianza(firma.getCadenaConfianza());
                firmaDto.setFirmadoDuranteVigencia(firma.getCertVigente());
                firmaDto.setFirmadoAntesRevocacion(firma.getCertNoRevocado());
                firmaDto.setVersionado(firma.getIntegridadConObservaciones());
                firmaDto.setTimeStamp(firma.getSelloTiempo() != null);
                firmaDto.setFechaFirma(dateFormat.format(firma.getFecFirma()));

                CertificadoDto certificadoDto = this.obtenerCertificadoDto(firma);

                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionPdfRespuestaDto validacion = new ValidacionPdfRespuestaDto();
            validacion.setFirmas(firmas);

            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se validó las firmas correctamente!");
            respuesta.setDatos(validacion);
            return respuesta;
        } catch (Exception ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    public RespuestaDto<ValidacionArchivoRespuestaDto> validarPkcs7(ValidacionArchivoDto objetoDto) {
        RespuestaDto<ValidacionArchivoRespuestaDto> respuesta = new RespuestaDto<ValidacionArchivoRespuestaDto>();
        try {
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes(StandardCharsets.UTF_8));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            ConfiguracionValidador configValidador = new ConfiguracionValidador();
            ValidadorPKCS7 validar = new ValidadorPKCS7(new ByteArrayInputStream(file), configValidador);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.getIntegridad());
                firmaDto.setCadenaConfianza(firma.getCadenaConfianza());
                firmaDto.setFirmadoDuranteVigencia(firma.getCertVigente());
                firmaDto.setFirmadoAntesRevocacion(firma.getCertNoRevocado());
                firmaDto.setVersionado(firma.getIntegridadConObservaciones());
                firmaDto.setTimeStamp(firma.getSelloTiempo() != null);
                firmaDto.setFechaFirma(dateFormat.format(firma.getFecFirma()));
                
                CertificadoDto certificadoDto = this.obtenerCertificadoDto(firma);
                
                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionArchivoRespuestaDto validacion = new ValidacionArchivoRespuestaDto();
            validacion.setFirmas(firmas);
            validacion.setFile(validar.obtenerContenidoBase64());
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se validó las firmas correctamente!");
            respuesta.setDatos(validacion);
            return respuesta;
        } catch (Exception ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    public RespuestaDto<ValidacionArchivoRespuestaDto> validarXml(ValidacionArchivoDto objetoDto) {
        RespuestaDto<ValidacionArchivoRespuestaDto> respuesta = new RespuestaDto<ValidacionArchivoRespuestaDto>();
        try {
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes(StandardCharsets.UTF_8));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            ConfiguracionValidador configValidador = new ConfiguracionValidador();
            ValidadorXml validar = new ValidadorXml(new ByteArrayInputStream(file), objetoDto.getDate(), configValidador);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.getIntegridad());
                firmaDto.setCadenaConfianza(firma.getCadenaConfianza());
                firmaDto.setFirmadoDuranteVigencia(firma.getCertVigente());
                firmaDto.setFirmadoAntesRevocacion(firma.getCertNoRevocado());
                firmaDto.setVersionado(firma.getIntegridadConObservaciones());
                firmaDto.setTimeStamp(firma.getSelloTiempo() != null);
                if (objetoDto.getDate() != null) {
                    firmaDto.setFechaFirma(dateFormat.format(firma.getFecFirma()));
                }

                CertificadoDto certificadoDto = this.obtenerCertificadoDto(firma);

                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionArchivoRespuestaDto validacion = new ValidacionArchivoRespuestaDto();
            validacion.setFirmas(firmas);
            validacion.setFile(objetoDto.getFile());
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se validó las firmas correctamente!");
            respuesta.setDatos(validacion);
            return respuesta;
        } catch (Exception ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    public RespuestaDto<ValidacionArchivoRespuestaDto> validarJws(ValidacionArchivoDto objetoDto) {
        RespuestaDto<ValidacionArchivoRespuestaDto> respuesta = new RespuestaDto<ValidacionArchivoRespuestaDto>();
        try {
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes(StandardCharsets.UTF_8));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            ConfiguracionValidador configValidador = new ConfiguracionValidador();
            ValidadorJws validar = new ValidadorJws(new ByteArrayInputStream(file), objetoDto.getDate(), configValidador);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            Firma firma = validar.iterator().next(); {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.getIntegridad());
                firmaDto.setCadenaConfianza(firma.getCadenaConfianza());
                firmaDto.setFirmadoDuranteVigencia(firma.getCertVigente());
                firmaDto.setFirmadoAntesRevocacion(firma.getCertNoRevocado());
                firmaDto.setVersionado(firma.getIntegridadConObservaciones());
                firmaDto.setTimeStamp(firma.getSelloTiempo() != null);
                if (objetoDto.getDate() != null) {
                    firmaDto.setFechaFirma(dateFormat.format(firma.getFecFirma()));
                }

                CertificadoDto certificadoDto = this.obtenerCertificadoDto(firma);

                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionArchivoRespuestaDto validacion = new ValidacionArchivoRespuestaDto();
            validacion.setFirmas(firmas);
            validacion.setFile(validar.obtenerContenidoBase64());
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Se validó las firmas correctamente!");
            respuesta.setDatos(validacion);
            return respuesta;
        } catch (Exception ex) {
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
            return respuesta;
        }
    }

    private CertificadoDto obtenerCertificadoDto(Firma firma) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        CertificadoDto certificadoDto = new CertificadoDto();
        InfoCertificado infoCertificado = firma.getInfoCertificado();
        if (infoCertificado.getInfoSujeto().getComplemento() != null && !infoCertificado.getInfoSujeto().getComplemento().equals("")) {
            certificadoDto.setCi(infoCertificado.getInfoSujeto().getNumeroDocumento() + "-" + infoCertificado.getInfoSujeto().getComplemento());
        } else {
            certificadoDto.setCi(infoCertificado.getInfoSujeto().getNumeroDocumento());
        }

        certificadoDto.setNombreSignatario(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
        certificadoDto.setCargoSignatario(firma.getInfoCertificado().getInfoSujeto().getCargo());
        certificadoDto.setOrganizacionSignatario(firma.getInfoCertificado().getInfoSujeto().getOrganizacion());
        certificadoDto.setEmailSignatario(firma.getInfoCertificado().getInfoSujeto().getCorreoElectronico());
        certificadoDto.setNombreECA(firma.getInfoCertificado().getInfoEmisor().getNombreComun());
        certificadoDto.setDescripcionECA(firma.getInfoCertificado().getInfoEmisor().getOrganizacion());
        certificadoDto.setInicioValidez(dateFormat.format(firma.getInfoCertificado().getInicioValidez()));
        certificadoDto.setFinValidez(dateFormat.format(firma.getInfoCertificado().getFinValidez()));
        if (firma.getRevocacion() != null && firma.getRevocacion().getFecha() != null) {
            certificadoDto.setRevocado(dateFormat.format(firma.getRevocacion().getFecha()));
        }

        certificadoDto.setNumeroSerie(((X509Certificate) firma.getCertificate()).getSerialNumber().toString(16));        

        return certificadoDto;
    }
}
