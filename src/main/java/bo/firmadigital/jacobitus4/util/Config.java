/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author ADSIB
 */
public class Config {
    protected Properties options;
    protected File user;
    protected File fileOptions;
    protected File token;

    public Config() {
        try {
            options = new Properties();
            user = new File(FileSystemView.getFileSystemView().getDefaultDirectory(), "Jacobitus");
            fileOptions = new File(user, "jacobitus.properties");
            if (user.exists()) {
                if (fileOptions.exists()) {
                    options.load(new FileInputStream(fileOptions));
                }
                token = new File(user, "softoken.p12");
                if (!token.exists()) {
                    token = null;
                }
            } else {
                token = null;
            }
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo obtener las opciones.");
        }
    }

    public boolean isProxyEnabled() {
        String proxy = options.getProperty("proxy");
        return proxy != null && proxy.equals("true");
    }

    public void setProxyEnabled(boolean proxy) {
        if (proxy) {
            options.setProperty("proxy", "true");
        } else {
            options.setProperty("proxy", "false");
        }
    }

    public String getProxyIP() {
        if (isProxyEnabled()) {
            return options.getProperty("proxyIP");
        } else {
            return "Ninguna";
        }
    }

    public void setProxyIP(String ip) {
        options.setProperty("proxyIP", ip);
    }

    public String getProxyPort() {
        if (isProxyEnabled()) {
            return options.getProperty("proxyPort");
        } else {
            return "3128";
        }
    }

    public void setProxyPort(String port) {
        options.setProperty("proxyPort", port);
    }

    public File getToken() {
        return token;
    }

    public String getTokenToCreate() {
        if (!user.exists()) {
            if (!user.mkdir()) {
                throw new RuntimeException("No se pudo crear el directorio " + user);
            }
        }
        token = new File(user, "softoken.p12");
        return token.getPath();
    }

    public void save() {
        try {
            if (!user.exists()) {
                if (!user.mkdir()) {
                    throw new RuntimeException("No se pudo crear el directorio " + user);
                }
            }
            options.store(new FileWriter(fileOptions), "ADSIB - Jacobitus options");
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public File getConversorFile() {
        return new File(user, "ConversorPdf.jar");
    }

    public URLClassLoader getConversor() {
        File jar = new File(user, "ConversorPdf.jar");
        if (jar.exists()) {
            try {
                URLClassLoader child = new URLClassLoader(
                        new URL[] {jar.toURI().toURL()},
                        this.getClass().getClassLoader()
                );
                return child;
            } catch (MalformedURLException ex) {
                return null;
            }
        }
        return null;
    }
}
