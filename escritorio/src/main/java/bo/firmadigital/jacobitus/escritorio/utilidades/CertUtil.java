package bo.firmadigital.jacobitus.escritorio.utilidades;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;

public class CertUtil {
    public static String obtenerDirectorio() {
        try {
            String ruta = new File(CertUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String rutaBase = new File(ruta).getParentFile().getAbsolutePath();
            String directorio = rutaBase + "/ca";
            if (directorio.contains("build/classes/java") || directorio.contains("build\\classes\\java")) {
                ruta = new File(CertUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getPath();
                rutaBase = new File(ruta).getParentFile().getAbsolutePath();
                directorio = rutaBase + "/libs/ca";
            }
            if (SistemaOperativoHelper.esMacOS()) {
                directorio = directorio.replace(" ", "\\ ");
            }
            return directorio;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String obtenerRutaCertificadoServicioLocal() {
        return CertUtil.obtenerDirectorio() + "/localhost.crt";
    }
    
    public static String obtenerRutaCertificadoECRB() {
        return CertUtil.obtenerDirectorio() + "/ecrb.crt";
    }

    private static String obtenerDistribucion() {
        if (SistemaOperativoHelper.esUnix()) {
            File osRelease = new File("/etc/os-release");
            if (osRelease.exists()) {
                try (Stream<String> lines = Files.lines(osRelease.toPath())) {
                    String content = lines.collect(Collectors.joining("\n")).toLowerCase();
                    if (content.contains("arch")) {
                        return "ARCH";
                    } else if (content.contains("fedora") || content.contains("rhel") || content.contains("centos")) {
                        return "RHEL";
                    } else if (content.contains("ubuntu") || content.contains("debian") || content.contains("mint")) {
                        return "DEBIAN";
                    }
                } catch (IOException e) {
                    // Fallback si falla la lectura del archivo
                }
            }
            return SistemaOperativoHelper.esDebian() ? "DEBIAN" : "RHEL";
        }
        if (SistemaOperativoHelper.esWindows()) {
            return "WINDOWS";
        }
        if (SistemaOperativoHelper.esMacOS()) {
            return "MACOS";
        }
        return null;
    }

    public static boolean verificarCertificadoServicioLocal() throws IOException {
        return CertUtil.verificarCertificadoServicioLocal(null);
    }

    public static boolean verificarCertificadoServicioLocal(String contrasenia) throws IOException {
        Process p;
        String distro = CertUtil.obtenerDistribucion();
        switch (distro) {
            case "DEBIAN":
            case "RHEL":
            case "ARCH":
                boolean respuesta = true;
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                boolean chromiumInstalado = false;
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "certutil", "-L", "-d", "sql:" + chromiumBD.getParent() });
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String s;
                        while ((s = in.readLine()) != null) {
                            if (s.startsWith("adsib.gob.bo")) {
                                chromiumInstalado = true;
                            }
                        }
                    }
                    respuesta = respuesta && chromiumInstalado;
                }

                List<String> rutasFirefox = new ArrayList<>();
                if ("ARCH".equals(distro)) {
                    rutasFirefox.add(System.getProperty("user.home") + "/.config/mozilla/firefox/");
                } else {
                    rutasFirefox.add(System.getProperty("user.home") + "/.mozilla/firefox/");
                    rutasFirefox.add(System.getProperty("user.home") + "/snap/firefox/common/.mozilla/firefox/");
                }
                
                final List<File> encontrados = new ArrayList<>();
                for (String ruta : rutasFirefox) {
                    File mozilla = new File(ruta);
                    if (mozilla.exists()) {
                        try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                            walkStream.filter(x -> x.toFile().isFile())
                                .forEach(f -> {
                                    if (f.toString().endsWith("cert9.db")) {
                                        encontrados.add(f.toFile());
                                    }
                                });
                        }
                    }
                }

                if (!chromiumBD.exists() && encontrados.isEmpty()) {
                    return false;
                }

                if (!encontrados.isEmpty()) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        boolean mozillaInstalado = false;
                        p = Runtime.getRuntime().exec(new String[] { "certutil", "-L", "-d", "sql:" + mozillaBD.getParent() });
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String s;
                            while ((s = in.readLine()) != null) {
                                if (s.startsWith("adsib.gob.bo")) {
                                    mozillaInstalado = true;
                                }
                            }
                        }
                        respuesta = respuesta && mozillaInstalado;
                    }
                    return respuesta;
                }
                return chromiumInstalado;

            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-user", "-store", "root" });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.contains("CN=adsib.gob.bo, OU=UID, O=ADSIB")) {
                            return true;
                        }
                    }
                }
                return false;

            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security verify-cert -c " + CertUtil.obtenerRutaCertificadoServicioLocal() + " -v" });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    Integer c = 0;
                    while ((s = in.readLine()) != null) {
                        if (s.contains("try again")) {
                            throw new IOException("La contraseña ingresada no válida.");
                        }
                        if (s.contains("certificate verification successful")) {
                            return true;
                        }
                        if (s.contains("CSSMERR_TP_NOT_TRUSTED")) {
                            return false;
                        }
                        c++;
                    }
                    if (s == null && c == 0) {
                        throw new IOException("La contraseña ingresada no válida.");
                    }
                }
                return false;
            default:
                return false;
        }
    }
    
    public static String obtenerHashCertificadoServicioLocal() throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
            case "RHEL":
            case "ARCH":
                return null;
            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-dump", CertUtil.obtenerRutaCertificadoServicioLocal() });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.toLowerCase().contains("cert") && s.toLowerCase().contains("(sha1)")) {
                            return s.split(":")[1].trim();
                        }
                    }
                }
                return null;
            case "MACOS":
                return null;
            default:
                return null;
        }
    }

    public static boolean instalarCertificadoServicioLocal() throws IOException, InterruptedException {
        return CertUtil.instalarCertificadoServicioLocal(null);
    }

    public static boolean instalarCertificadoServicioLocal(String contrasenia) throws IOException, InterruptedException {
        Process p;
        String distro = CertUtil.obtenerDistribucion();
        switch (distro) {
            case "DEBIAN":
            case "RHEL":
            case "ARCH":
                boolean respuesta = true;
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                boolean chromiumInstalado = false;
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "certutil", "-L", "-d", "sql:" + chromiumBD.getParent() });
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String s;
                        while ((s = in.readLine()) != null) {
                            if (s.startsWith("adsib.gob.bo")) {
                                chromiumInstalado = true;
                            }
                        }
                    }
                    if (!chromiumInstalado) {
                        p = Runtime.getRuntime().exec(new String[] { "certutil", "-A", "-n", "adsib.gob.bo", "-i", CertUtil.obtenerRutaCertificadoServicioLocal(), "-t", "cTC,cTC,cTC", "-d", "sql:" + chromiumBD.getParent() });
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String s;
                            while ((s = in.readLine()) != null) {
                                if (s.startsWith("adsib.gob.bo")) {
                                    chromiumInstalado = true;
                                }
                            }
                        }
                    }
                    respuesta = respuesta && chromiumInstalado;
                }

                List<String> rutasFirefox = new ArrayList<>();
                if ("ARCH".equals(distro)) {
                    rutasFirefox.add(System.getProperty("user.home") + "/.config/mozilla/firefox/");
                } else {
                    rutasFirefox.add(System.getProperty("user.home") + "/.mozilla/firefox/");
                    rutasFirefox.add(System.getProperty("user.home") + "/snap/firefox/common/.mozilla/firefox/");
                }

                final List<File> encontrados = new ArrayList<>();
                for (String ruta : rutasFirefox) {
                    File mozilla = new File(ruta);
                    if (mozilla.exists()) {
                        try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                            walkStream.filter(x -> x.toFile().isFile())
                                .forEach(f -> {
                                    if (f.toString().endsWith("cert9.db")) {
                                        encontrados.add(f.toFile());
                                    }
                                });
                        }
                    }
                }

                if (!chromiumBD.exists() && encontrados.isEmpty()) {
                    return false;
                }

                if (!encontrados.isEmpty()) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        boolean mozillaInstalado = false;
                        p = Runtime.getRuntime().exec(new String[] { "certutil", "-L", "-d", "sql:" + mozillaBD.getParent() });
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String s;
                            while ((s = in.readLine()) != null) {
                                if (s.startsWith("adsib.gob.bo")) {
                                    mozillaInstalado = true;
                                }
                            }
                        }
                        if (!mozillaInstalado) {
                            p = Runtime.getRuntime().exec(new String[] { "certutil", "-A", "-n", "adsib.gob.bo", "-i", CertUtil.obtenerRutaCertificadoServicioLocal(), "-t", "cTC,cTC,cTC", "-d", "sql:" + mozillaBD.getParent() });
                            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                                String s;
                                while ((s = in.readLine()) != null) {
                                    if (s.startsWith("adsib.gob.bo")) {
                                        mozillaInstalado = true;
                                    }
                                }
                            }
                        }
                        respuesta = respuesta && mozillaInstalado;
                    }
                    return respuesta;
                }
                return chromiumInstalado;

            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-addstore", "-f", "-user", "root", CertUtil.obtenerRutaCertificadoServicioLocal() });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.toLowerCase().startsWith("certutil: -addstore") && (s.toLowerCase().contains("correctamente") || s.toLowerCase().contains("successfully"))) {
                            return true;
                        }
                    }
                }
                return false;

            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain " + CertUtil.obtenerRutaCertificadoServicioLocal() });
                return true;
            default:
                return false;
        }
    }
    
    public static boolean desinstalarCertificadoServicioLocal() throws IOException {
        return CertUtil.desinstalarCertificadoServicioLocal(null);
    }

    public static boolean desinstalarCertificadoServicioLocal(String contrasenia) throws IOException {
        Process p;
        String distro = CertUtil.obtenerDistribucion();
        switch (distro) {
            case "DEBIAN":
            case "RHEL":
            case "ARCH":
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "certutil", "-D", "-n", "adsib.gob.bo", "-d", "sql:" + chromiumBD.getParent() });
                }

                List<String> rutasFirefox = new ArrayList<>();
                if ("ARCH".equals(distro)) {
                    rutasFirefox.add(System.getProperty("user.home") + "/.config/mozilla/firefox/");
                } else {
                    rutasFirefox.add(System.getProperty("user.home") + "/.mozilla/firefox/");
                    rutasFirefox.add(System.getProperty("user.home") + "/snap/firefox/common/.mozilla/firefox/");
                }

                final List<File> encontrados = new ArrayList<>();
                for (String ruta : rutasFirefox) {
                    File mozilla = new File(ruta);
                    if (mozilla.exists()) {
                        try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                            walkStream.filter(x -> x.toFile().isFile())
                                .forEach(f -> {
                                    if (f.toString().endsWith("cert9.db")) {
                                        encontrados.add(f.toFile());
                                    }
                                });
                        }
                    }
                }
                
                if (encontrados.size() > 0) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        p = Runtime.getRuntime().exec(new String[] { "certutil", "-D", "-n", "adsib.gob.bo", "-d", "sql:" + mozillaBD.getParent() });
                    }
                }
                return true;
            case "WINDOWS":
                String hashCertificado = CertUtil.obtenerHashCertificadoServicioLocal();
                if (hashCertificado != null) {
                    p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-delstore", "-f", "-user", "root", hashCertificado });
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String s;
                        while ((s = in.readLine()) != null) {
                            if (s.toLowerCase().startsWith("certutil: -delstore") && (s.toLowerCase().contains("correctamente") || s.toLowerCase().contains("successfully"))) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security remove-trusted-cert -d " + CertUtil.obtenerRutaCertificadoServicioLocal() + " && sudo -S security delete-certificate -c adsib.gob.bo" });
                return true;
            default:
                return false;
        }
    }

    public static boolean verificarCertificadoECRB() throws IOException {
        return CertUtil.verificarCertificadoECRB(null);
    }

    public static boolean verificarCertificadoECRB(String contrasenia) throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                p = Runtime.getRuntime().exec(new String[] { "ls", "/usr/local/share/ca-certificates" });
                return analizarSalidaECRB(p);
            case "RHEL":
                p = Runtime.getRuntime().exec(new String[] { "ls", "/etc/pki/ca-trust/source/anchors" });
                return analizarSalidaECRB(p);
            case "ARCH":
                p = Runtime.getRuntime().exec(new String[] { "ls", "/etc/ca-certificates/trust-source/anchors" });
                return analizarSalidaECRB(p);
            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-user", "-store", "root" });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.contains("C=BO, O=ATT, CN=Entidad Certificadora Raiz de Bolivia")) {
                            return true;
                        }
                    }
                }
                return false;
            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security verify-cert -c " + CertUtil.obtenerRutaCertificadoECRB() + " -v" });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    Integer c = 0;
                    while ((s = in.readLine()) != null) {
                        if (s.contains("try again")) {
                            throw new IOException("La contraseña ingresada no válida.");
                        }
                        if (s.contains("certificate verification successful")) {
                            return true;
                        }
                        if (s.contains("CSSMERR_TP_NOT_TRUSTED")) {
                            return false;
                        }
                        c++;
                    }
                    if (s == null && c == 0) {
                        throw new IOException("La contraseña ingresada no válida.");
                    }
                }
                return false;
            default:
                return false;
        }
    }
    
    private static boolean analizarSalidaECRB(Process p) throws IOException {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.contains("ecrb")) {
                    return true;
                }
            }
        }
        return false;
    }

    public static String obtenerHashCertificadoECRB() throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
            case "RHEL":
            case "ARCH":
                return null;
            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-dump", CertUtil.obtenerRutaCertificadoECRB() });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.toLowerCase().contains("cert") && s.toLowerCase().contains("(sha1)")) {
                            return s.split(":")[1].trim();
                        }
                    }
                }
                return null;
            case "MACOS":
                return null;
            default:
                return null;
        }
    }

    public static boolean instalarCertificadoECRB() throws IOException {
        return CertUtil.instalarCertificadoECRB(null);
    }

    public static boolean instalarCertificadoECRB(String contrasenia) throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "cp", CertUtil.obtenerRutaCertificadoECRB(), "/usr/local/share/ca-certificates" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-certificates" });
                }
                return true;
            case "RHEL":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "cp", CertUtil.obtenerRutaCertificadoECRB(), "/etc/pki/ca-trust/source/anchors" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-trust" });
                }
                return true;
            case "ARCH":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "cp", CertUtil.obtenerRutaCertificadoECRB(), "/etc/ca-certificates/trust-source/anchors" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-trust" });
                }
                return true;
            case "WINDOWS":
                p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-addstore", "-f", "-user", "root", CertUtil.obtenerRutaCertificadoECRB() });
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.toLowerCase().startsWith("certutil: -addstore") && (s.toLowerCase().contains("correctamente") || s.toLowerCase().contains("successfully"))) {
                            return true;
                        }
                    }
                }
                return false;
            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security add-trusted-cert -d -r trustRoot -k /Library/Keychains/System.keychain " + CertUtil.obtenerRutaCertificadoECRB() });
                return true;
            default:
                return false;
        }
    }
    
    public static boolean desinstalarCertificadoECRB() throws IOException {
        return CertUtil.desinstalarCertificadoECRB(null);
    }

    public static boolean desinstalarCertificadoECRB(String contrasenia) throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "rm", "/usr/local/share/ca-certificates/ecrb.crt" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-certificates", "--fresh" });
                }
                return true;

            case "RHEL":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "rm", "/etc/pki/ca-trust/source/anchors/ecrb.crt" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-trust" });
                }
                return true;

            case "ARCH":
                p = Runtime.getRuntime().exec(new String[] { "sudo", "rm", "/etc/ca-certificates/trust-source/anchors/ecrb.crt" });
                if (CertUtil.verificarCertificadoECRB()) {
                    p = Runtime.getRuntime().exec(new String[] { "sudo", "update-ca-trust" });
                }
                return true;

            case "WINDOWS":
                String hashCertificado = CertUtil.obtenerHashCertificadoECRB();
                if (hashCertificado != null) {
                    p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-delstore", "-f", "-user", "root", hashCertificado });
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        return true;
                    }
                }
                return false;

            case "MACOS":
                p = Runtime.getRuntime().exec(new String[] { "/bin/bash", "-c", "echo " + contrasenia + " | sudo -S security remove-trusted-cert -d " + CertUtil.obtenerRutaCertificadoECRB() + " && sudo -S security delete-certificate -c \"Entidad Certificadora Raiz de Bolivia\"" });
                return true;

            default:
                return false;
        }
    }
}