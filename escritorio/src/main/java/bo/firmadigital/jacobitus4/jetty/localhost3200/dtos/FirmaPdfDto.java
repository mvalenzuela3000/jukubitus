package bo.firmadigital.jacobitus.escritorio.jetty.localhost3200.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaPdfDto {
    private String nombre_archivo;
    private String alias;
    private String pdf_base64;
}
