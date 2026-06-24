package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaJsonItemRespuestaDto {
    private String id = null;
    private String json_firmado;
}
