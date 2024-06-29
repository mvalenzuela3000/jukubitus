package bo.firmadigital.jacobitus4.jetty.localhost3200.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaPdfRespuestaDto {
    private String nombre_archivo;
    private String pdf_base64;
    private CertificadoDto certificado;
}
