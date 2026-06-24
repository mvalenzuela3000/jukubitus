package bo.firmadigital.jacobitus.escritorio.comun.actualizaciones;

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
