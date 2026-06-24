package bo.firmadigital.jacobitus.escritorio.util.actualizacion;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ActualizacionInfo {
    private final boolean actualizacionDisponible;
    
    @Getter(AccessLevel.NONE)
    private final boolean esNecesaria;
    
    private final String versionInstalada;
    
    private final String ultimaVersion;
    
    private final String enlaceDescarga;

    public boolean esNecesaria() {
        return esNecesaria;
    }
}
