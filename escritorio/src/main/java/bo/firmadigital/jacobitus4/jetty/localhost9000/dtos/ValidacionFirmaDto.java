package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;

import bo.firmadigital.jacobitus.validador.CertDate;
import bo.firmadigital.jacobitus.validador.Validador.OCSPState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionFirmaDto {
    private String certificado;
    private String certificadoTitular;
    private String certificadoSelladoTiempo;
    private String documentoEstado;
    private String documentoDescripcion;
    private String cadenaConfianzaEstado;
    private String cadenaConfianzaDescripcion;
    private String periodoValidez;
    private String periodoValidezEstado;
    private String periodoValidezDescripcion;
    private String revocacion;
    private String revocacionEstado;
    private String revocacionDescripcion;

    public ValidacionFirmaDto(CertDate cert) {
        if (cert.isOk()) {
            if (cert.isAlerted()) {
                this.certificado = "ERROR";
            } else {
                this.certificado = "OK";
            }

            this.certificadoTitular = cert.getDatos().getNombreComunSubject();
            this.certificadoSelladoTiempo = cert.getTimeStampStr();
        } else {
            this.certificado = "ERROR";
            this.certificadoTitular = cert.getDatos().getNombreComunSubject();
            this.certificadoSelladoTiempo = cert.getTimeStampStr();
        }

        if (cert.isValid()) {
            if (cert.isValidAlerted()) {
                this.documentoEstado = "Documento modificado";
                switch (cert.getValidAdd()) {
                    case widget_firma_agregado:
                        this.documentoDescripcion = "Se agregaron firmas posteriormente a esta firma";
                        break;
                    case widget_otro_agregado:
                        this.documentoDescripcion = "Se agregaron widgets posteriormente a esta firma";
                        break;
                    default:
                        this.documentoDescripcion = "Se modific\u00f3 el contenido de widgets posteriormente a esta firma";
                }
            } else {
                this.documentoEstado = "Documento aut\u00e9ntico";
                this.documentoDescripcion = "El documento no ha sido modificado despu\u00e9s de la firma";
            }
        } else {
            this.documentoEstado = "Documento modificado";
            this.documentoDescripcion = "El documento ha sido modificado despu\u00e9s de la firma";
        }

        if (cert.isPKI()) {
            this.cadenaConfianzaEstado = "Cadena de confianza";
            this.cadenaConfianzaDescripcion = "La cadena de confianza est\u00e1 bajo la Infraestructura de Clave P\u00fablica del Estado Plurinacional de Bolivia, y por lo tanto, tiene valor legal";
        } else {
            this.cadenaConfianzaEstado = "Cadena de confianza";
            this.cadenaConfianzaDescripcion = "La cadena de confianza no est\u00e1 bajo la Infraestructura de Clave P\u00fablica del Estado Plurinacional de Bolivia, y por lo tanto, no tiene valor legal";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        String periodo = dateFormat.format(((X509Certificate) cert.getCertificate()).getNotBefore()) + " y " + dateFormat.format(((X509Certificate) cert.getCertificate()).getNotAfter());
        if (cert.isActive()) {
            if (cert.isActiveAlerted()) {
                this.periodoValidez = "ERROR";
                this.periodoValidezEstado = "Firmado en el periodo de vigencia (Sin sellado de tiempo)";
            } else {
                this.periodoValidez = "OK";
                this.periodoValidezEstado = "Firmado en el periodo de vigencia";
            }

            this.periodoValidezDescripcion = "La firma fue realizada dentro del periodo comprendido entre " + periodo;
        } else {
            this.periodoValidez = "ERROR";
            this.periodoValidezEstado = "Firmado fuera del periodo de vigencia";
            this.periodoValidezDescripcion = "La firma fue realizada fuera del periodo comprendido entre " + periodo;
        }

        if (cert.isOCSP()) {
            if (cert.isOCSPAlerted()) {
                this.revocacion = "ERROR";
                this.revocacionEstado = "Firmado con certificado v\u00e1lido, revocado despu\u00e9s de la firma (Sin sellado de tiempo)";
                this.revocacionDescripcion = "El documento fue firmado con un certificado revocado antes de la firma";
            } else {
                this.revocacion = "OK";
                this.revocacionEstado = "Firmado con certificado no revocado";
                this.revocacionDescripcion = "El documento fue firmado con un certificado no revocado";
            }
        } else {
            this.revocacion = "ERROR";
            this.revocacionEstado = "Firmado con certificado revocado";
            if (cert.getOCSP().getState() == OCSPState.CONNECTION) {
                this.revocacionDescripcion = "No se pudo acceder al servicio para verificar el estado del certificado.";
            } else {
                this.revocacionDescripcion = "El documento fue firmado con un certificado revocado antes de la firma";
            }
        }

    }

    public void imprimir() {
        System.out.println("CERTIFICADO");
        System.out.println(this.certificado);
        System.out.println(this.certificadoTitular);
        System.out.println(this.certificadoSelladoTiempo);
        System.out.println("DOCUMENTO");
        System.out.println(this.documentoEstado);
        System.out.println(this.documentoDescripcion);
        System.out.println("CADENA DE CONFIANZA");
        System.out.println(this.cadenaConfianzaEstado);
        System.out.println(this.cadenaConfianzaDescripcion);
        System.out.println("PERIODO DE VALIDEZ");
        System.out.println(this.periodoValidez);
        System.out.println(this.periodoValidezEstado);
        System.out.println(this.periodoValidezDescripcion);
        System.out.println("REVOCACION");
        System.out.println(this.revocacion);
        System.out.println(this.revocacionEstado);
        System.out.println(this.revocacionDescripcion);
    }
}
