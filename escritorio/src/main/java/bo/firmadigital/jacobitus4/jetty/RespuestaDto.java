package bo.firmadigital.jacobitus4.jetty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDto {
    private boolean finalizado;
    private String mensaje;
}
