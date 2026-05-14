package bo.firmadigital.jacobitus4.util.actualizacion;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActualizacionInfo {
    private final boolean actualizacionDisponible;
    private final boolean esNecesaria;
    private final String versionInstalada;
    private final String ultimaVersion;
    private final String enlaceDescarga;
}
