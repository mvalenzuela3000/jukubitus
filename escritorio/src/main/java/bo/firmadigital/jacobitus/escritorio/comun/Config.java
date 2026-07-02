package bo.firmadigital.jacobitus.escritorio.comun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import javax.swing.filechooser.FileSystemView;

import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.escritorio.utilidades.Controlador;

public class Config {
    protected Properties options;
    protected File user;
    protected File fileOptions;
    protected File token;

    private static Config ourInstance;

    protected Config() {
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
            throw new JacobitusException("No se pudo obtener las opciones.");
        }
    }

    public static Config getInstance() {
        if (ourInstance == null) {
            ourInstance = new Config();
        }
        return ourInstance;
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

    public boolean isSecondaryPortEnabled() {
        String proxy = options.getProperty("secondaryPort");
        return proxy != null && proxy.equals("true");
    }

    public void setSecondaryPortEnabled(boolean secondaryPort) {
        if (secondaryPort) {
            options.setProperty("secondaryPort", "true");
        } else {
            options.setProperty("secondaryPort", "false");
        }
    }

    public boolean isTertiaryPortEnabled() {
        String proxy = options.getProperty("tertiaryPort");
        return proxy != null && proxy.equals("true");
    }

    public void setTertiaryPortEnabled(boolean tertiaryPort) {
        if (tertiaryPort) {
            options.setProperty("tertiaryPort", "true");
        } else {
            options.setProperty("tertiaryPort", "false");
        }
    }

    public boolean isConfirmarSalidaEnabled() {
        String confirmarSalida = options.getProperty("confirmarSalida");
        return confirmarSalida == null || confirmarSalida.equals("true");
    }

    public void setConfirmarSalidaEnabled(boolean confirmarSalida) {
        if (confirmarSalida) {
            options.setProperty("confirmarSalida", "true");
        } else {
            options.setProperty("confirmarSalida", "false");
        }
    }

    public File getToken() {
        token = new File(user, "softoken.p12");
        if (!token.exists()) {
            token = null;
        }
        return token;
    }

    public String getTokenToCreate() {
        if (!user.exists()) {
            if (!user.mkdir()) {
                throw new JacobitusException("No se pudo crear el directorio " + user);
            }
        }
        token = new File(user, "softoken.p12");
        return token.getPath();
    }

    public File getDriver() {
        String driver = options.getProperty("driver");
        if (driver != null) {
            File file = new File(driver);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public void setDriver(File file) {
        if (file == null) {
            options.remove("driver");
        } else {
            options.setProperty("driver", file.getPath());
        }
    }

    public boolean isHsmEnabled() {
        String hsm = options.getProperty("hsm");
        return hsm != null && hsm.equals("true");
    }

    public void setHsmEnabled(boolean hsm) {
        if (hsm) {
            options.setProperty("hsm", "true");
        } else {
            options.setProperty("hsm", "false");
        }
    }

    public String getHsmCloud() {
        if (options.containsKey("hsmCloud")) {
            return options.getProperty("hsmCloud");
        } else {
            return "https://firmadigital.bo/cloud_hsm/services/api/v1/hsm";
        }
    }

    public void setHsmCloud(String url) {
        if (url == null) {
            options.remove("hsmCloud");
        } else {
            options.setProperty("hsmCloud", url);
        }
    }

    public String getHsmJWT() {
        if (options.containsKey("hsmJWT")) {
            return options.getProperty("hsmJWT");
        } else {
            return null;
        }
    }

    public void setHsmJWT(String jwt) {
        if (jwt == null || jwt.trim().equals("")) {
            options.remove("hsmJWT");
        } else {
            options.setProperty("hsmJWT", jwt);
        }
    }

    public boolean isTSEnabled() {
        String ts = options.getProperty("ts");
        return ts != null && ts.equals("true");
    }

    public void setTSEnabled(boolean ts) {
        if (ts) {
            options.setProperty("ts", "true");
        } else {
            options.setProperty("ts", "false");
        }
    }

    public String getTS() {
        if (options.containsKey("TS")) {
            return options.getProperty("TS");
        } else {
            return "https://firmadigital.bo/sellado_tiempo/timestamp/api/v1/sellado";
        }
    }

    public void setTS(String url) {
        if (url == null || url.trim().equals("")) {
            options.remove("TS");
        } else {
            options.setProperty("TS", url);
        }
    }

    public String getTSJWT() {
        if (options.containsKey("TSJWT")) {
            return options.getProperty("TSJWT");
        } else {
            return "";
        }
    }

    public void setTSJWT(String user) {
        if (user == null || user.trim().equals("")) {
            options.remove("TSJWT");
        } else {
            options.setProperty("TSJWT", user);
        }
    }

    public String getHsmType() {
        if (options.containsKey("hsmType")) {
            return options.getProperty("hsmType");
        } else {
            return "HSM";
        }
    }

    public void setHsmType(String type) {
        if (type == null) {
            options.remove("hsmType");
        } else {
            options.setProperty("hsmType", type);
        }
    }

    public String getDirectorioControladores() {
        return Controlador.obtenerDirectorio();
    }

    public String getDispositivosCompatibles() {
        return Controlador.obtenerDispositivosCompatibles();
    }

    public String getEnlaceInstaladores() {
        return "https://firmadigital.bo/herramientas/jacobitus-escritorio/instaladores";
    }

    public String getEnlaceVersion() {
        return "https://firmadigital.bo/herramientas/jacobitus-escritorio/version.json";
    }

    public void save() {
        try {
            if (!user.exists()) {
                if (!user.mkdir()) {
                    throw new JacobitusException("No se pudo crear el directorio " + user);
                }
            }
            options.store(new FileWriter(fileOptions), "AGETIC - Jacobitus options");
        } catch (IOException ex) {
            throw new JacobitusException(ex.getMessage());
        }
    }
}
