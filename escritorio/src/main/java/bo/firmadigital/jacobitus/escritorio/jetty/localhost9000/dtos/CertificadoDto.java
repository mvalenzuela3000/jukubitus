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
    private String ci;
    private String nombreSignatario;
    private String cargoSignatario;
    private String organizacionSignatario;
    private String emailSignatario;
    private String nombreECA;
    private String descripcionECA;
    private String inicioValidez;
    private String finValidez;
    private String revocado;
    private String numeroSerie;
    private String unidadOrganizacionalSignatario;
}
