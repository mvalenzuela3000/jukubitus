package bo.firmadigital.jacobitus.escritorio.util;

import java.util.Set;

import bo.firmadigital.jacobitus.comun.InfoCertificado;

public class ECA {
    private static final Set<String> ECAS_VALIDAS = Set.of(
        "Entidad Certificadora Publica ADSIB",
        "Entidad Certificadora Autorizada Digicert",
        "Entidad Certificadora Publica AGETIC"
    );

    private static final Set<String> ECAS_PUBLICAS = Set.of(
        "Entidad Certificadora Publica ADSIB",
        "Entidad Certificadora Publica AGETIC"
    );

    public static boolean esValida(InfoCertificado infoCertificado) {
        return ECAS_VALIDAS.contains(infoCertificado.getInfoEmisor().getNombreComun());
    }

    public static boolean esPublica(InfoCertificado infoCertificado) {
        return ECAS_PUBLICAS.contains(infoCertificado.getInfoEmisor().getNombreComun());
    }
}
