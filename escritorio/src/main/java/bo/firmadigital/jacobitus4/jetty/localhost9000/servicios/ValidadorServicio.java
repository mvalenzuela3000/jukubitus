package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import bo.firmadigital.jacobitus.validador.ValidadorJws;
import bo.firmadigital.jacobitus.validador.ValidadorPKCS7;
import bo.firmadigital.jacobitus.validador.ValidadorPdf;
import bo.firmadigital.jacobitus.validador.ValidadorXml;
import bo.firmadigital.jacobitus.validador.base.Opciones;
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
            byte[] file = Base64.getDecoder().decode(objetoDto.getPdf().getBytes("UTF-8"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Opciones opciones = new Opciones();
            ValidadorPdf validar = new ValidadorPdf(new ByteArrayInputStream(file), opciones);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.isValid());
                firmaDto.setCadenaConfianza(firma.isPKI());
                firmaDto.setFirmadoDuranteVigencia(firma.isActive());
                firmaDto.setFirmadoAntesRevocacion(firma.isOCSP());
                firmaDto.setVersionado(firma.isValidAlerted());
                firmaDto.setTimeStamp(firma.getTimeStamp() != null);
                firmaDto.setFechaFirma(dateFormat.format(firma.getSignDate()));
                CertificadoDto certificadoDto = new CertificadoDto();
                if (firma.getInfoCertificado().getInfoSujeto().getComplemento() != null && !firma.getInfoCertificado().getInfoSujeto().getComplemento().equals("")) {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento() + "-" + firma.getInfoCertificado().getInfoSujeto().getComplemento());
                } else {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento());
                }

                certificadoDto.setNombreSignatario(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
                certificadoDto.setCargoSignatario(firma.getInfoCertificado().getInfoSujeto().getCargo());
                certificadoDto.setOrganizacionSignatario(firma.getInfoCertificado().getInfoSujeto().getOrganizacion());
                certificadoDto.setEmailSignatario(firma.getInfoCertificado().getInfoSujeto().getCorreoElectronico());
                certificadoDto.setNombreECA(firma.getInfoCertificado().getInfoEmisor().getNombreComun());
                certificadoDto.setDescripcionECA(firma.getInfoCertificado().getInfoEmisor().getOrganizacion());
                certificadoDto.setInicioValidez(dateFormat.format(firma.getInfoCertificado().getInicioValidez()));
                certificadoDto.setFinValidez(dateFormat.format(firma.getInfoCertificado().getFinValidez()));
                if (firma.getOCSP().getDate() != null) {
                    certificadoDto.setRevocado(dateFormat.format(firma.getOCSP().getDate()));
                }

                certificadoDto.setNumeroSerie(((X509Certificate) firma.getCertificate()).getSerialNumber().toString(16));
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
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes("UTF-8"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Opciones opciones = new Opciones();
            ValidadorPKCS7 validar = new ValidadorPKCS7(new ByteArrayInputStream(file), opciones);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.isValid());
                firmaDto.setCadenaConfianza(firma.isPKI());
                firmaDto.setFirmadoDuranteVigencia(firma.isActive());
                firmaDto.setFirmadoAntesRevocacion(firma.isOCSP());
                firmaDto.setVersionado(firma.isValidAlerted());
                firmaDto.setTimeStamp(firma.getTimeStamp() != null);
                firmaDto.setFechaFirma(dateFormat.format(firma.getSignDate()));
                CertificadoDto certificadoDto = new CertificadoDto();
                if (firma.getInfoCertificado().getInfoSujeto().getComplemento() != null && !firma.getInfoCertificado().getInfoSujeto().getComplemento().equals("")) {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento() + "-" + firma.getInfoCertificado().getInfoSujeto().getComplemento());
                } else {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento());
                }

                certificadoDto.setNombreSignatario(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
                certificadoDto.setCargoSignatario(firma.getInfoCertificado().getInfoSujeto().getCargo());
                certificadoDto.setOrganizacionSignatario(firma.getInfoCertificado().getInfoSujeto().getOrganizacion());
                certificadoDto.setEmailSignatario(firma.getInfoCertificado().getInfoSujeto().getCorreoElectronico());
                certificadoDto.setNombreECA(firma.getInfoCertificado().getInfoEmisor().getNombreComun());
                certificadoDto.setDescripcionECA(firma.getInfoCertificado().getInfoEmisor().getOrganizacion());
                certificadoDto.setInicioValidez(dateFormat.format(firma.getInfoCertificado().getInicioValidez()));
                certificadoDto.setFinValidez(dateFormat.format(firma.getInfoCertificado().getFinValidez()));
                if (firma.getOCSP().getDate() != null) {
                    certificadoDto.setRevocado(dateFormat.format(firma.getOCSP().getDate()));
                }

                certificadoDto.setNumeroSerie(((X509Certificate) firma.getCertificate()).getSerialNumber().toString(16));
                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionArchivoRespuestaDto validacion = new ValidacionArchivoRespuestaDto();
            validacion.setFirmas(firmas);
            validacion.setFile(validar.exportB64(new ByteArrayInputStream(file)));
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
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes("UTF-8"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Opciones opciones = new Opciones();
            ValidadorXml validar = new ValidadorXml(new ByteArrayInputStream(file), objetoDto.getDate(), opciones);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            for (Firma firma : validar) {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.isValid());
                firmaDto.setCadenaConfianza(firma.isPKI());
                firmaDto.setFirmadoDuranteVigencia(firma.isActive());
                firmaDto.setFirmadoAntesRevocacion(firma.isOCSP());
                firmaDto.setVersionado(firma.isValidAlerted());
                firmaDto.setTimeStamp(firma.getTimeStamp() != null);
                if (objetoDto.getDate() != null) {
                    firmaDto.setFechaFirma(dateFormat.format(firma.getSignDate()));
                }
                CertificadoDto certificadoDto = new CertificadoDto();
                if (firma.getInfoCertificado().getInfoSujeto().getComplemento() != null && !firma.getInfoCertificado().getInfoSujeto().getComplemento().equals("")) {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento() + "-" + firma.getInfoCertificado().getInfoSujeto().getComplemento());
                } else {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento());
                }

                certificadoDto.setNombreSignatario(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
                certificadoDto.setCargoSignatario(firma.getInfoCertificado().getInfoSujeto().getCargo());
                certificadoDto.setOrganizacionSignatario(firma.getInfoCertificado().getInfoSujeto().getOrganizacion());
                certificadoDto.setEmailSignatario(firma.getInfoCertificado().getInfoSujeto().getCorreoElectronico());
                certificadoDto.setNombreECA(firma.getInfoCertificado().getInfoEmisor().getNombreComun());
                certificadoDto.setDescripcionECA(firma.getInfoCertificado().getInfoEmisor().getOrganizacion());
                certificadoDto.setInicioValidez(dateFormat.format(firma.getInfoCertificado().getInicioValidez()));
                certificadoDto.setFinValidez(dateFormat.format(firma.getInfoCertificado().getFinValidez()));
                if (firma.getOCSP() != null && firma.getOCSP().getDate() != null) {
                    certificadoDto.setRevocado(dateFormat.format(firma.getOCSP().getDate()));
                }

                certificadoDto.setNumeroSerie(((X509Certificate) firma.getCertificate()).getSerialNumber().toString(16));
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
            byte[] file = Base64.getDecoder().decode(objetoDto.getFile().getBytes("UTF-8"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            Opciones opciones = new Opciones();
            ValidadorJws validar = new ValidadorJws(new ByteArrayInputStream(file), objetoDto.getDate(), opciones);
            List<FirmaDto> firmas = new ArrayList<FirmaDto>();

            Firma firma = (Firma) validar.iterator().next(); {
                FirmaDto firmaDto = new FirmaDto();
                firmaDto.setNoModificado(firma.isValid());
                firmaDto.setCadenaConfianza(firma.isPKI());
                firmaDto.setFirmadoDuranteVigencia(firma.isActive());
                firmaDto.setFirmadoAntesRevocacion(firma.isOCSP());
                firmaDto.setVersionado(firma.isValidAlerted());
                firmaDto.setTimeStamp(firma.getTimeStamp() != null);
                if (objetoDto.getDate() != null) {
                    firmaDto.setFechaFirma(dateFormat.format(firma.getSignDate()));
                }
                CertificadoDto certificadoDto = new CertificadoDto();
                if (firma.getInfoCertificado().getInfoSujeto().getComplemento() != null && !firma.getInfoCertificado().getInfoSujeto().getComplemento().equals("")) {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento() + "-" + firma.getInfoCertificado().getInfoSujeto().getComplemento());
                } else {
                    certificadoDto.setCi(firma.getInfoCertificado().getInfoSujeto().getNumeroDocumento());
                }

                certificadoDto.setNombreSignatario(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
                certificadoDto.setCargoSignatario(firma.getInfoCertificado().getInfoSujeto().getCargo());
                certificadoDto.setOrganizacionSignatario(firma.getInfoCertificado().getInfoSujeto().getOrganizacion());
                certificadoDto.setEmailSignatario(firma.getInfoCertificado().getInfoSujeto().getCorreoElectronico());
                certificadoDto.setNombreECA(firma.getInfoCertificado().getInfoEmisor().getNombreComun());
                certificadoDto.setDescripcionECA(firma.getInfoCertificado().getInfoEmisor().getOrganizacion());
                certificadoDto.setInicioValidez(dateFormat.format(firma.getInfoCertificado().getInicioValidez()));
                certificadoDto.setFinValidez(dateFormat.format(firma.getInfoCertificado().getFinValidez()));
                if (firma.getOCSP() != null && firma.getOCSP().getDate() != null) {
                    certificadoDto.setRevocado(dateFormat.format(firma.getOCSP().getDate()));
                }

                certificadoDto.setNumeroSerie(((X509Certificate) firma.getCertificate()).getSerialNumber().toString(16));
                firmaDto.setCertificado(certificadoDto);
                firmas.add(firmaDto);
            }

            ValidacionArchivoRespuestaDto validacion = new ValidacionArchivoRespuestaDto();
            validacion.setFirmas(firmas);
            validacion.setFile(validar.exportB64(new ByteArrayInputStream(file)));
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
}
