package bo.firmadigital.jacobitus4.util;

import java.util.Set;

import bo.firmadigital.jacobitus.comun.InfoCertificado;

public class ECA {
    private static final Set<String> ECAS_VALIDAS = Set.of(
        "Entidad Certificadora Publica ADSIB",
        "Entidad Certificadora Autorizada Digicert"
    );

    private static final Set<String> ECAS_PUBLICAS = Set.of(
        "Entidad Certificadora Publica ADSIB"
    );

    public static boolean esValida(InfoCertificado infoCertificado) {
        return ECAS_VALIDAS.contains(infoCertificado.getInfoEmisor().getNombreComun());
    }

    public static boolean esPublica(InfoCertificado infoCertificado) {
        return ECAS_PUBLICAS.contains(infoCertificado.getInfoEmisor().getNombreComun());
    }
}
