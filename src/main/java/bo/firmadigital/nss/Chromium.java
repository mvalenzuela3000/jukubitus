/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.nss;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ADSIB
 */
public class Chromium {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean registrarCertificadoLinux() {
        try {
            File chromium = new File(System.getProperty("user.home") + "/.pki/nssdb");
            if (!chromium.exists()) {
                return false;
            }
            File db = searchFile(chromium, "cert9.db");
            if (db == null) {
                return false;
            }
            Process p;
            p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-L", "-d", db.getParent() });
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    if (s.startsWith("adsib.gob.bo")) {
                        return true;
                    }
                }
            }
            String cert = URLDecoder.decode(Chromium.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
            if (cert.endsWith(".jar")) {
                cert = new File(cert).getParentFile().getParent() + "/ca/server.crt";
            } else {
                cert = System.getProperty("user.dir") + "/ca/server.crt";
            }
            p = Runtime.getRuntime().exec(new String[] { "/usr/bin/certutil", "-A", "-n", "adsib.gob.bo", "-i", cert, "-t", "cTC,cTC,cTC", "-d", db.getParent() });
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    if (s.startsWith("adsib.gob.bo")) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Chromium.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static File searchFile(File path, String name) {
        if (path.isDirectory()) {
            File[] arr = path.listFiles();
            for (File f : arr) {
                File found = searchFile(f, name);
                if (found != null) {
                    return found;
                }
            }
        } else {
            if (path.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

    public static boolean registrarCertificatoMacOS() {
        try {
            Process p = Runtime.getRuntime().exec("/opt/jacobitus/check");
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                if (in.readLine() != null) {
                    return true;
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                if (in.readLine() != null) {
                    return false;
                }
            }
        } catch (IOException ex) { }
        return false;
    }

    public static boolean registrarCertificatoMacOS(String pass) {
        try {
            Process p = Runtime.getRuntime().exec(new String[] { "/opt/jacobitus/install", pass });
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                if (in.readLine() != null) {
                    return true;
                }
            }
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
                return in.readLine().equals("Password:");
            }
        } catch (IOException ex) { }
        return false;
    }

    public static boolean registrarCertificado() {
        if (OS.contains("nux")) {
            return registrarCertificadoLinux();
        } else if (OS.contains("mac")) {
            return registrarCertificatoMacOS();
        } else {
            return false;
        }
    }

    public static boolean registrarCertificado(String pass) {
        if (OS.contains("mac")) {
            return registrarCertificatoMacOS(pass);
        } else {
            return false;
        }
    }
}
