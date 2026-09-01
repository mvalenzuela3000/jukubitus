package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDto {
     /*
    |--------------------------------------------------------------------------
    | TITULAR
    |--------------------------------------------------------------------------
    */
    private String ci;
    private String nombreSignatario;
    private String organizacionSignatario;
    private String unidadOrganizacionalSignatario;
    private String cargoSignatario;
    private String emailSignatario;
    /*
    |--------------------------------------------------------------------------
    | EMISOR / ECA
    |--------------------------------------------------------------------------
    */
    private String nombreECA;
    private String descripcionECA;
    /*
    |--------------------------------------------------------------------------
    | PERIODO DE VALIDEZ
    |--------------------------------------------------------------------------
    */
    private String inicioValidez;
    private String finValidez;
    /*
    |--------------------------------------------------------------------------
    | REVOCACION
    |--------------------------------------------------------------------------
    */
    private String revocado;
    /*
    |--------------------------------------------------------------------------
    | INFORMACION TECNICA
    |--------------------------------------------------------------------------
    */
    private String numeroSerie;
    /*
    |--------------------------------------------------------------------------
    | POLITICA DEL CERTIFICADO
    |--------------------------------------------------------------------------
    */
    private String tipoCertificado;
    private String nivelSeguridad;
    private String tipoUso;
}
