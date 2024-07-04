package bo.firmadigital.utiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import bo.firmadigital.jacobitus.utilidades.OS;

public class CertUtil {
    public static String obtenerDirectorio() {
        try {
            String ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
            String rutaBase = new File(ruta).getParentFile().getAbsolutePath();
            String directorio = rutaBase + "/ca";
            if (directorio.contains("build/classes/java") || directorio.contains("build\\classes\\java")) {
                ruta = new File(Controlador.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile().getParentFile().getPath();
                rutaBase = new File(ruta).getParentFile().getAbsolutePath();
                directorio = rutaBase + "/libs/ca";
            }
            if (OS.isMac()) {
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
        if (OS.isUnix()) {
            if (OS.isDebian()) {
                return "DEBIAN";
            } else {
                return "RHEL";
            }
        }
        if (OS.isWindows()) {
            return "WINDOWS";
        }
        if (OS.isMac()) {
            return "MACOS";
        }
        return null;
    }

    public static boolean verificarCertificadoServicioLocal() throws IOException {
        return CertUtil.verificarCertificadoServicioLocal(null);
    }

    public static boolean verificarCertificadoServicioLocal(String contrasenia) throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                boolean respuesta = true;
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                boolean chromiumInstalado = false;
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-L", "-d", chromiumBD.getParent() });
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
                File mozilla = new File(System.getProperty("user.home") + "/.mozilla/firefox/");
                final List<File> encontrados = new ArrayList<>();
                try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                    walkStream.filter(x -> x.toFile().isFile())
                        .forEach(f -> {
                            if (f.toString().endsWith("cert9.db")) {
                                encontrados.add(f.toFile());
                            }
                        });
                }
                if (encontrados.size() > 0) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        boolean mozillaInstalado = false;
                        p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-L", "-d", mozillaBD.getParent() });
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
                }
                return respuesta;
            case "RHEL":
                return false;
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
                return null;
            case "RHEL":
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
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                boolean respuesta = true;
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                boolean chromiumInstalado = false;
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-L", "-d", chromiumBD.getParent() });
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                        String s;
                        while ((s = in.readLine()) != null) {
                            if (s.startsWith("adsib.gob.bo")) {
                                chromiumInstalado = true;
                            }
                        }
                    }
                    if (!chromiumInstalado) {
                        p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-A", "-n", "adsib.gob.bo", "-i", CertUtil.obtenerRutaCertificadoServicioLocal(), "-t", "cTC,cTC,cTC", "-d", chromiumBD.getParent() });
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
                File mozilla = new File(System.getProperty("user.home") + "/.mozilla/firefox/");
                final List<File> encontrados = new ArrayList<>();
                try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                    walkStream.filter(x -> x.toFile().isFile())
                        .forEach(f -> {
                            if (f.toString().endsWith("cert9.db")) {
                                encontrados.add(f.toFile());
                            }
                        });
                }
                if (encontrados.size() > 0) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        boolean mozillaInstalado = false;
                        p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-L", "-d", mozillaBD.getParent() });
                        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                            String s;
                            while ((s = in.readLine()) != null) {
                                if (s.startsWith("adsib.gob.bo")) {
                                    mozillaInstalado = true;
                                }
                            }
                        }
                        if (!mozillaInstalado) {
                            p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-A", "-n", "adsib.gob.bo", "-i", CertUtil.obtenerRutaCertificadoServicioLocal(), "-t", "cTC,cTC,cTC", "-d", mozillaBD.getParent() });
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
                }
                return respuesta;
            case "RHEL":
                return false;
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
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                File chromiumBD = new File(System.getProperty("user.home") + "/.pki/nssdb/cert9.db");
                if (chromiumBD.exists()) {
                    p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-D", "-n", "adsib.gob.bo", "-d", chromiumBD.getParent() });
                }
                File mozilla = new File(System.getProperty("user.home") + "/.mozilla/firefox/");
                final List<File> encontrados = new ArrayList<>();
                try (Stream<Path> walkStream = Files.walk(mozilla.toPath())) {
                    walkStream.filter(x -> x.toFile().isFile())
                        .forEach(f -> {
                            if (f.toString().endsWith("cert9.db")) {
                                encontrados.add(f.toFile());
                            }
                        });
                }
                if (encontrados.size() > 0) {
                    for (int i = 0; i < encontrados.size(); i++) {
                        File mozillaBD = encontrados.get(i);
                        p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-D", "-n", "adsib.gob.bo", "-d", mozillaBD.getParent() });
                    }
                }
                return true;
            case "RHEL":
                return false;
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
                try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String s;
                    while ((s = in.readLine()) != null) {
                        if (s.contains("ecrb")) {
                            return true;
                        }
                    }
                }
                return false;
            case "RHEL":
                return false;
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
    
    public static String obtenerHashCertificadoECRB() throws IOException {
        Process p;
        switch (CertUtil.obtenerDistribucion()) {
            case "DEBIAN":
                return null;
            case "RHEL":
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
                return false;
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
                return false;
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
