/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.utilidades;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author ADSIB
 */
public class OS {
    private static final String OS = System.getProperty("os.name").toLowerCase();
    private static final String VERSION = System.getProperty("os.version");

    public static boolean isWindows() {
        return OS.contains("win");
    }

    public static boolean isMac() {
        return OS.contains("mac") || OS.contains("darwin");
    }

    public static boolean isUnix() {
        return (OS.contains("nix") || OS.contains("nux") || OS.contains("aix"));
    }

    public static String version() {
        return VERSION;
    }

    public static boolean isDebian() {
        if (!isUnix()) {
            return false;
        }
        String[] cmd = { "/bin/sh", "-c", "cat /etc/*-release" };
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = bri.readLine()) != null) {
                if (line.contains("Debian")) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
}
