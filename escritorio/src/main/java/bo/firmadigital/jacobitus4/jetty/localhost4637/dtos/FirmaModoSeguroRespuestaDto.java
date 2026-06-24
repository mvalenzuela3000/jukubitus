
package bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaModoSeguroRespuestaDto {
    private String message;
    private String error;
    private List<FirmaPdfItemRespuestaDto> files;
}
