package bo.firmadigital.utiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CertUtil {
    public static boolean verificarCertificadoServicioLocal() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-user", "-store", "root" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.contains("CN=adsib.gob.bo, OU=UID, O=ADSIB")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String obtenerHashCertificadoServicioLocal() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-dump", "./ca/localhost.crt" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.toLowerCase().contains("cert") && s.toLowerCase().contains("(sha1)")) {
                    return s.split(":")[1].trim();
                }
            }
        }
        return null;
    }

    public static boolean instalarCertificadoServicioLocal() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-addstore", "-f", "-user", "root", "./ca/localhost.crt" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.toLowerCase().startsWith("certutil: -addstore") && (s.toLowerCase().contains("correctamente") || s.toLowerCase().contains("successfully"))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean desinstalarCertificadoServicioLocal() throws IOException {
        String hashCertificado = CertUtil.obtenerHashCertificadoServicioLocal();
        if (hashCertificado != null) {
            Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-delstore", "-f", "-user", "root", hashCertificado });
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
    }

    public static boolean verificarCertificadoECRB() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-user", "-store", "root" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.contains("C=BO, O=ATT, CN=Entidad Certificadora Raiz de Bolivia")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static String obtenerHashCertificadoECRB() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-dump", "./ca/ecrb.pem" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.toLowerCase().contains("cert") && s.toLowerCase().contains("(sha1)")) {
                    return s.split(":")[1].trim();
                }
            }
        }
        return null;
    }

    public static boolean instalarCertificadoECRB() throws IOException {
        Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-addstore", "-f", "-user", "root", "./ca/ecrb.pem" });
        try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String s;
            while ((s = in.readLine()) != null) {
                if (s.toLowerCase().startsWith("certutil: -addstore") && (s.toLowerCase().contains("correctamente") || s.toLowerCase().contains("successfully"))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean desinstalarCertificadoECRB() throws IOException {
        String hashCertificado = CertUtil.obtenerHashCertificadoECRB();
        if (hashCertificado != null) {
            Process p = Runtime.getRuntime().exec(new String[] { "certutil.exe", "-delstore", "-f", "-user", "root", hashCertificado });
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
    }
}
