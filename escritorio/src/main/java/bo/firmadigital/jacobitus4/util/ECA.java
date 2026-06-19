package bo.firmadigital.jacobitus4.util;

import java.util.Set;

import bo.firmadigital.jacobitus.validador.comun.DatosCertificado;

public class ECA {
    private static final Set<String> ECAS_VALIDAS = Set.of(
        "Entidad Certificadora Publica ADSIB",
        "Entidad Certificadora Autorizada Digicert"
    );

    private static final Set<String> ECAS_PUBLICAS = Set.of(
        "Entidad Certificadora Publica ADSIB"
    );

    public static boolean esValida(DatosCertificado datos) {
        return ECAS_VALIDAS.contains(datos.getNombreComunIssuer());
    }

    public static boolean esPublica(DatosCertificado datos) {
        return ECAS_PUBLICAS.contains(datos.getNombreComunIssuer());
    }
}
