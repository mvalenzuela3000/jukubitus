package bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.comun;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDto<T> {
    private boolean finalizado;
    private String mensaje;
    private T datos;
}
