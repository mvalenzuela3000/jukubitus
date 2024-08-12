package bo.firmadigital.jacobitus4.util;

import bo.firmadigital.jacobitus.validador.DatosCertificado;

public class ECA {
    public static boolean esValida(DatosCertificado datos) {
        if (datos.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB") || datos.getNombreComunIssuer().equals("Entidad Certificadora Autorizada Digicert")) {
            return true;
        }
        return false;
    }

    public static boolean esPublica(DatosCertificado datos) {
        if (datos.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB")) {
            return true;
        }
        return false;
    }
}
