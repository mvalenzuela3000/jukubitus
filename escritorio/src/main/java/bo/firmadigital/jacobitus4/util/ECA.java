package bo.firmadigital.jacobitus4.util;

import java.security.GeneralSecurityException;

import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.validador.DatosCertificado;

public class ECA {
    public static boolean esValida(Slot slot, String pin, String label) {
        try {
            IToken token = slot.getToken();
            token.iniciar(pin);
            DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
            token.salir();
            if (datos.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB") || datos.getNombreComunIssuer().equals("Entidad Certificadora Autorizada Digicert")) {
                return true;
            }
            return false;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }

    public static boolean esPublica(Slot slot, String pin, String label) {
        try {
            IToken token = slot.getToken();
            token.iniciar(pin);
            DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
            token.salir();
            if (datos.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB")) {
                return true;
            }
            return false;
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
