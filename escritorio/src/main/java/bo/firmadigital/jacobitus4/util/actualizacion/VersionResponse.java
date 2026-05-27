package bo.firmadigital.jacobitus4.util.actualizacion;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VersionResponse {
    private String aplicacion;
    private String version;
    private String tipoActualizacion;
}
