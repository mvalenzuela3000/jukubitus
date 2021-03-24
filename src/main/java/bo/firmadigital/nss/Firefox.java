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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ADSIB
 */
public class Firefox {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean registrarCertificadoWindows() {
        try {
            File mozilla = new File(System.getenv("APPDATA") + "\\Mozilla\\Firefox\\Profiles");
            if (!mozilla.exists()) {
                return false;
            }
            File db = searchFile(mozilla, "cert9.db");
            if (db == null) {
                return false;
            }
            String path = System.getProperty("user.dir");
            if (path.endsWith("runtime\\bin")) {
                path = new File(path).getParentFile().getParent();
            }
            String certutil = path + "\\ca\\nss\\install.bat";
            Process p;
            p = Runtime.getRuntime().exec(new String[] { certutil });
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    if (s.startsWith("adsib.gob.bo")) {
                        return true;
                    }
                }
            }
            String cert = path + "\\ca\\server.crt";
            p = Runtime.getRuntime().exec(new String[] { certutil, cert });
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String s;
                while ((s = in.readLine()) != null) {
                    if (s.startsWith("adsib.gob.bo")) {
                        return true;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Firefox.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public static boolean registrarCertificadoLinux() {
        try {
            File mozilla = new File(System.getProperty("user.home") + "/.mozilla/firefox");
            if (!mozilla.exists()) {
                return false;
            }
            File db = searchFile(mozilla, "cert9.db");
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
            String cert = Firefox.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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
            Logger.getLogger(Firefox.class.getName()).log(Level.SEVERE, null, ex);
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

    public static boolean registrarCertificado() {
        if (OS.contains("win")) {
            return registrarCertificadoWindows();
        } else {
            return registrarCertificadoLinux();
        }
    }
}
