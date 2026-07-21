package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaPdfDto {
    private Long slot;
    private String pin;
    private String alias;
    private String pdf;

    private Boolean bloquear = false;
    private FirmaPosicionDto point = null;
    private String image = null;
}
