package bo.firmadigital.jacobitus4;

import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.codehaus.jettison.json.JSONObject;

import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;
import javafx.application.Platform;

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
                if (java.awt.SystemTray.isSupported()) {
                    java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                    java.awt.Image image = ImageIO.read(Main.class.getClassLoader().getResource("icon.png"));
                    java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);
                    trayIcon.setImageAutoSize(true);
                    trayIcon.addActionListener((ActionEvent e) -> {
                        FormAplicacion.show();
                    });
                    java.awt.MenuItem showItem = new java.awt.MenuItem("Abrir");
                    showItem.addActionListener(event -> {
                        FormAplicacion.show();
                    });
                    java.awt.MenuItem exitItem = new java.awt.MenuItem("Salir");
                    exitItem.addActionListener(event -> {
                        try {
                            WebServer.detener();
                        } catch (Exception ex) {
                            Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
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
                            FormAplicacion.run(true, true, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                        } else {
                            FormAplicacion.run(true, true, false, args[0]);
                        }
                    } else {
                        FormAplicacion.run(true, true, false);
                    }
                } else {
                    if (args.length == 1) {
                        String[] parts = args[0].split("\\?");
                        if (parts.length == 2) {
                            JSONObject body = Request.splitQuery(parts[1]);
                            FormAplicacion.run(true, false, false, body.getString("url"), body.getString("token"), body.getString("urlpost"));
                        } else {
                            FormAplicacion.run(true, false, false, args[0]);
                        }
                    } else {
                        FormAplicacion.run(true, SistemaOperativoHelper.esDebian(), SistemaOperativoHelper.esDebian());
                    }
                }
            } catch (Exception ex) {
                try {
                    WebServer.detener();
                } catch (Exception ex2) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
                FormAplicacion.run(false, false, false);
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}
