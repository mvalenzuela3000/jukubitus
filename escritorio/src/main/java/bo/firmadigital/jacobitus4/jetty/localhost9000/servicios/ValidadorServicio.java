package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.io.ByteArrayInputStream;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import bo.firmadigital.jacobitus.validador.CertDate;
import bo.firmadigital.jacobitus.validador.Opciones;
import bo.firmadigital.jacobitus.validador.ValidadorJws;
import bo.firmadigital.jacobitus.validador.ValidadorPKCS7;
import bo.firmadigital.jacobitus.validador.ValidadorPdf;
import bo.firmadigital.jacobitus.validador.ValidadorXml;
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

            for (CertDate cert : validar) {
                FirmaDto firma = new FirmaDto();
                firma.setNoModificado(cert.isValid());
                firma.setCadenaConfianza(cert.isPKI());
                firma.setFirmadoDuranteVigencia(cert.isActive());
                firma.setFirmadoAntesRevocacion(cert.isOCSP());
                firma.setVersionado(cert.isValidAlerted());
                firma.setTimeStamp(cert.getTimeStamp() != null);
                firma.setFechaFirma(dateFormat.format(cert.getSignDate()));
                CertificadoDto certificado = new CertificadoDto();
                if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                } else {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject());
                }

                certificado.setNombreSignatario(cert.getDatos().getNombreComunSubject());
                certificado.setCargoSignatario(cert.getDatos().getCargoSubject());
                certificado.setOrganizacionSignatario(cert.getDatos().getOrganizacionSubject());
                certificado.setEmailSignatario(cert.getDatos().getCorreoSubject());
                certificado.setNombreECA(cert.getDatos().getNombreComunIssuer());
                certificado.setDescripcionECA(cert.getDatos().getDescripcionSubject());
                certificado.setInicioValidez(dateFormat.format(cert.getDatos().getInicioValidez()));
                certificado.setFinValidez(dateFormat.format(cert.getDatos().getFinValidez()));
                if (cert.getOCSP().getDate() != null) {
                    certificado.setRevocado(dateFormat.format(cert.getOCSP().getDate()));
                }

                certificado.setNumeroSerie(((X509Certificate) cert.getCertificate()).getSerialNumber().toString(16));
                firma.setCertificado(certificado);
                firmas.add(firma);
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

            for (CertDate cert : validar) {
                FirmaDto firma = new FirmaDto();
                firma.setNoModificado(cert.isValid());
                firma.setCadenaConfianza(cert.isPKI());
                firma.setFirmadoDuranteVigencia(cert.isActive());
                firma.setFirmadoAntesRevocacion(cert.isOCSP());
                firma.setVersionado(cert.isValidAlerted());
                firma.setTimeStamp(cert.getTimeStamp() != null);
                firma.setFechaFirma(dateFormat.format(cert.getSignDate()));
                CertificadoDto certificado = new CertificadoDto();
                if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                } else {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject());
                }

                certificado.setNombreSignatario(cert.getDatos().getNombreComunSubject());
                certificado.setCargoSignatario(cert.getDatos().getCargoSubject());
                certificado.setOrganizacionSignatario(cert.getDatos().getOrganizacionSubject());
                certificado.setEmailSignatario(cert.getDatos().getCorreoSubject());
                certificado.setNombreECA(cert.getDatos().getNombreComunIssuer());
                certificado.setDescripcionECA(cert.getDatos().getDescripcionSubject());
                certificado.setInicioValidez(dateFormat.format(cert.getDatos().getInicioValidez()));
                certificado.setFinValidez(dateFormat.format(cert.getDatos().getFinValidez()));
                if (cert.getOCSP().getDate() != null) {
                    certificado.setRevocado(dateFormat.format(cert.getOCSP().getDate()));
                }

                certificado.setNumeroSerie(((X509Certificate) cert.getCertificate()).getSerialNumber().toString(16));
                firma.setCertificado(certificado);
                firmas.add(firma);
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

            for (CertDate cert : validar) {
                FirmaDto firma = new FirmaDto();
                firma.setNoModificado(cert.isValid());
                firma.setCadenaConfianza(cert.isPKI());
                firma.setFirmadoDuranteVigencia(cert.isActive());
                firma.setFirmadoAntesRevocacion(cert.isOCSP());
                firma.setVersionado(cert.isValidAlerted());
                firma.setTimeStamp(cert.getTimeStamp() != null);
                if (objetoDto.getDate() != null) {
                    firma.setFechaFirma(dateFormat.format(cert.getSignDate()));
                }
                CertificadoDto certificado = new CertificadoDto();
                if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                } else {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject());
                }

                certificado.setNombreSignatario(cert.getDatos().getNombreComunSubject());
                certificado.setCargoSignatario(cert.getDatos().getCargoSubject());
                certificado.setOrganizacionSignatario(cert.getDatos().getOrganizacionSubject());
                certificado.setEmailSignatario(cert.getDatos().getCorreoSubject());
                certificado.setNombreECA(cert.getDatos().getNombreComunIssuer());
                certificado.setDescripcionECA(cert.getDatos().getDescripcionSubject());
                certificado.setInicioValidez(dateFormat.format(cert.getDatos().getInicioValidez()));
                certificado.setFinValidez(dateFormat.format(cert.getDatos().getFinValidez()));
                if (cert.getOCSP() != null && cert.getOCSP().getDate() != null) {
                    certificado.setRevocado(dateFormat.format(cert.getOCSP().getDate()));
                }

                certificado.setNumeroSerie(((X509Certificate) cert.getCertificate()).getSerialNumber().toString(16));
                firma.setCertificado(certificado);
                firmas.add(firma);
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

            CertDate cert = (CertDate) validar.iterator().next(); {
                FirmaDto firma = new FirmaDto();
                firma.setNoModificado(cert.isValid());
                firma.setCadenaConfianza(cert.isPKI());
                firma.setFirmadoDuranteVigencia(cert.isActive());
                firma.setFirmadoAntesRevocacion(cert.isOCSP());
                firma.setVersionado(cert.isValidAlerted());
                firma.setTimeStamp(cert.getTimeStamp() != null);
                if (objetoDto.getDate() != null) {
                    firma.setFechaFirma(dateFormat.format(cert.getSignDate()));
                }
                CertificadoDto certificado = new CertificadoDto();
                if (cert.getDatos().getComplementoSubject() != null && !cert.getDatos().getComplementoSubject().equals("")) {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject() + "-" + cert.getDatos().getComplementoSubject());
                } else {
                    certificado.setCi(cert.getDatos().getNumeroDocumentoSubject());
                }

                certificado.setNombreSignatario(cert.getDatos().getNombreComunSubject());
                certificado.setCargoSignatario(cert.getDatos().getCargoSubject());
                certificado.setOrganizacionSignatario(cert.getDatos().getOrganizacionSubject());
                certificado.setEmailSignatario(cert.getDatos().getCorreoSubject());
                certificado.setNombreECA(cert.getDatos().getNombreComunIssuer());
                certificado.setDescripcionECA(cert.getDatos().getDescripcionSubject());
                certificado.setInicioValidez(dateFormat.format(cert.getDatos().getInicioValidez()));
                certificado.setFinValidez(dateFormat.format(cert.getDatos().getFinValidez()));
                if (cert.getOCSP() != null && cert.getOCSP().getDate() != null) {
                    certificado.setRevocado(dateFormat.format(cert.getOCSP().getDate()));
                }

                certificado.setNumeroSerie(((X509Certificate) cert.getCertificate()).getSerialNumber().toString(16));
                firma.setCertificado(certificado);
                firmas.add(firma);
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
