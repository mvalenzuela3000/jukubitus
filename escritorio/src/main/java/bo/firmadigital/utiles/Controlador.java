package bo.firmadigital.utiles;

import java.io.File;
import java.net.URISyntaxException;

import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;

public class Controlador {
    public static String obtenerDirectorio() {
        try {
            String ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String rutaBase = new File(ruta).getParentFile().getAbsolutePath();
            String directorio = rutaBase + "/controladores";
            if (directorio.contains("build/classes/java") || directorio.contains("build\\classes\\java")) {
                ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getPath();
                rutaBase = new File(ruta).getParentFile().getAbsolutePath();
                directorio = rutaBase + "/libs/controladores";
            }
            if (SistemaOperativoHelper.es32Bits()) {
                directorio = directorio + "/x86";
            } else {
                directorio = directorio + "/x64";
            }
            return directorio;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String obtenerDispositivosCompatibles() {
        try {
            String ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String rutaBase = new File(ruta).getParentFile().getAbsolutePath();
            String directorio = rutaBase + "/controladores";
            if (directorio.contains("build/classes/java") || directorio.contains("build\\classes\\java")) {
                ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getPath();
                rutaBase = new File(ruta).getParentFile().getAbsolutePath();
                directorio = rutaBase + "/libs/controladores";
            }
            return directorio + "/tokens";
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
