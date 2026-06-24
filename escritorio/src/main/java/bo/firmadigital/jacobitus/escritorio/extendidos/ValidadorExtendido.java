package bo.firmadigital.jacobitus.escritorio.extendidos;

import java.io.File;
import java.io.InputStream;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import bo.firmadigital.jacobitus.revocacion.EstadoRevocacion.Estado;
import bo.firmadigital.jacobitus.validador.base.Validador;
import bo.firmadigital.jacobitus.validador.comun.Firma;

public abstract class ValidadorExtendido extends Validador {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public boolean isRemoto() {
        return false;
    }

    public String getUrlRespuesta() {
        throw new UnsupportedOperationException("Not supported in " + this.getClass() + ".");
    }

    public String getTokenAutorizacion() {
        throw new UnsupportedOperationException("No implementado..");
    }

    public String getPath() {
        return file.getPath();
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public String getExtension() {
        return file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf(".") + 1) : null;
    }

    public String getRevisionPath(String revision) {
        throw new UnsupportedOperationException("No implementado.");
    }

    public void export(File f) {
        throw new UnsupportedOperationException("No implementado.");
    }

    public String exportB64(InputStream is) {
        throw new UnsupportedOperationException("No implementado.");
    }

    public String getPathValidated() {
        StringBuilder res = new StringBuilder(file.getPath());
        for (Firma firma : firmas) {
            if (firma.getIntegridad() && firma.getCadenaConfianza() && (firma.getCertVigente() || (getExtension().equals("jws") || getExtension().equals("xml"))) && firma.getCertNoRevocado()) {
                if (firma.getTieneObservaciones() || (getExtension().equals("jws") || getExtension().equals("xml"))) {
                    res.append("\n\t✔ ⚠ ");
                    // res.append("\n\t\u2714 \u26A0");
                } else {
                    res.append("\n\t✔ ");
                    // res.append("\n\t\u2714");
                }
            } else {
                if (firma.getRevocacion() == null) {
                    res.append("\n\t✘? ");
                    // res.append("\n\t\u2718");
                } else if (firma.getRevocacion().getEstado() == Estado.ERROR_CONEXION) {
                    res.append("\n\t✘? ");
                    // res.append("\n\t\u2718");
                } else {
                    res.append("\n\t✘ ");
                    // res.append("\n\t\u2718");
                }
            }
            res.append(firma.getInfoCertificado().getInfoSujeto().getNombreComun());
        }
        return res.toString();
    }
}
