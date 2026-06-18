/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.codehaus.jettison.json.JSONObject;

import bo.firmadigital.jacobitus.utilidades.OS;
import dorkbox.jna.rendering.ProviderType;
import dorkbox.jna.rendering.RenderProvider;
import dorkbox.jna.rendering.Renderer;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.util.SizeAndScalingLinux;
import javafx.application.Platform;

/**
 *
 * @author ADSIB
 */
public class Main {
    // public static Server jettyServer = new Server();

    public static void main(String[] args) {
        Request req = new Request();
        if (req.estado()) {
            if (args.length == 1) {
                req.show(args[0]);
            } else {
                req.show();
            }
        } else {
            try {
                WebServer.iniciar();
                if (OS.isUnix()) {
                    if (args.length == 1) {
                        String[] parts = args[0].split("\\?");
                        if (parts.length == 2) {
                            JSONObject body = Request.splitQuery(parts[1]);
                            App.run(true, true, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                        } else {
                            App.run(true, true, false, args[0]);
                        }
                    } else {
                        App.run(true, true, false);
                    }
                } else if (java.awt.SystemTray.isSupported()) {
                    java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                    java.awt.Image image = ImageIO.read(Main.class.getClassLoader().getResource("icon.png"));
                    java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);
                    trayIcon.setImageAutoSize(true);
                    trayIcon.addActionListener((ActionEvent e) -> {
                        App.show();
                    });
                    java.awt.MenuItem showItem = new java.awt.MenuItem("Abrir");
                    showItem.addActionListener(event -> {
                        App.show();
                    });
                    java.awt.MenuItem exitItem = new java.awt.MenuItem("Salir");
                    exitItem.addActionListener(event -> {
                        try {
                            WebServer.detener();
                        } catch (Exception ex) {
                            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        Platform.exit();
                        tray.remove(trayIcon);
                    });
                    final java.awt.PopupMenu popup = new java.awt.PopupMenu();
                    popup.add(showItem);
                    popup.add(exitItem);
                    trayIcon.setPopupMenu(popup);
                    tray.add(trayIcon);
                    if (args.length == 1) {
                        String[] parts = args[0].split("\\?");
                        if (parts.length == 2) {
                            JSONObject body = Request.splitQuery(parts[1]);
                            App.run(true, true, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                        } else {
                            App.run(true, true, false, args[0]);
                        }
                    } else {
                        App.run(true, true, false);
                    }
                } else {
                    if (args.length == 1) {
                        String[] parts = args[0].split("\\?");
                        if (parts.length == 2) {
                            JSONObject body = Request.splitQuery(parts[1]);
                            App.run(true, false, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                        } else {
                            App.run(true, false, false, args[0]);
                        }
                    } else {
                        App.run(true, OS.isDebian(), OS.isDebian());
                    }
                }
            } catch (Exception ex) {
                try {
                    WebServer.detener();
                } catch (Exception ex2) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
                App.run(false, false, false);
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    static void iniciarTrayDorkbox() {
        try {
            RenderProvider.set(new JavaFxRenderProvider());
            SystemTray.AUTO_SIZE = false;
            SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.AppIndicator;
            SizeAndScalingLinux.OVERRIDE_TRAY_SIZE = 24;
            SizeAndScalingLinux.OVERRIDE_MENU_SIZE = 16;
            SystemTray systemTray = SystemTray.get("Jacobitus");
            File trayIcon = prepararIconoTrayDorkbox();
            if (systemTray == null || trayIcon == null) {
                return;
            }
            systemTray.setTooltip("Jacobitus");
            systemTray.setImage(trayIcon);
            systemTray.getMenu().add(new MenuItem("Abrir", event -> App.show()));
            systemTray.getMenu().add(new MenuItem("Salir", event -> {
                try {
                    WebServer.detener();
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
                systemTray.shutdown();
                Platform.exit();
            }));
        } catch (Throwable ex) {
            Logger.getLogger(Main.class.getName()).log(Level.WARNING, "No se pudo iniciar Dorkbox SystemTray", ex);
        }
    }

    private static File prepararIconoTrayDorkbox() throws Exception {
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "JacobitusCache_" + System.getProperty("user.name"));
        cacheDir.mkdirs();
        File iconFile = new File(cacheDir, "jacobitus-tray.png");
        try (InputStream icon = Main.class.getClassLoader().getResourceAsStream("tray-icon.png")) {
            if (icon == null) {
                return null;
            }
            Files.copy(icon, iconFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return iconFile;
    }

    private static class JavaFxRenderProvider implements Renderer {
        @Override
        public boolean isSupported() { return true; }

        @Override
        public ProviderType getType() { return ProviderType.JAVAFX; }

        @Override
        public boolean alreadyRunning() { return true; }

        @Override
        public boolean isEventThread() { return Platform.isFxApplicationThread(); }

        @Override
        public int getGtkVersion() { return 3; }

        @Override
        public boolean dispatch(Runnable runnable) {
            if (Platform.isFxApplicationThread()) {
                runnable.run();
            } else {
                Platform.runLater(runnable);
            }
            return true;
        }
    }

}
