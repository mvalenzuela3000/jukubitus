package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenSolicitudDto {
    private Long slot;
    private String alias;
    private String pin;
    private List<TokenSolicitudDetalleDto> data;
}
