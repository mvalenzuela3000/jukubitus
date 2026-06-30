package bo.firmadigital.jacobitus.escritorio.formularios;
import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import bo.firmadigital.jacobitus.escritorio.WebServer;
import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;
import dorkbox.jna.rendering.ProviderType;
import dorkbox.jna.rendering.RenderProvider;
import dorkbox.jna.rendering.Renderer;
import dorkbox.systemTray.SystemTray;
import dorkbox.systemTray.util.SizeAndScalingLinux;
import javafx.application.Platform;

public class TaskBar {
    private TrayIcon trayIcon = null;
    private SystemTray sysTray = null;

    public boolean iniciarBandejaSistema() {
        if (SistemaOperativoHelper.esWindows() || SistemaOperativoHelper.esMacOS()) {
            return iniciarBandejaAwtSeguro();
        }

        sysTray = SystemTray.get("Jacobitus");
        if (SistemaOperativoHelper.esUnix()) {
            return iniciarBandejaDorkbox();
        }

        return iniciarBandejaDorkbox() || iniciarBandejaAwtSeguro();
    }

    private boolean iniciarBandejaDorkbox() {
        try {
            if (SistemaOperativoHelper.esUnix()) {
                RenderProvider.set(new ProveedorRenderJavaFx());
                SystemTray.AUTO_SIZE = false;
                SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.AppIndicator;
                SizeAndScalingLinux.OVERRIDE_TRAY_SIZE = 24;
                SizeAndScalingLinux.OVERRIDE_MENU_SIZE = 16;
            }
            if (sysTray == null) {
                return false;
            }
            File iconFile = prepararIconoBandeja();
            if (iconFile == null) {
                return false;
            }
            sysTray.setTooltip("Jacobitus");
            sysTray.setImage(iconFile);
            sysTray.getMenu().add(new dorkbox.systemTray.MenuItem("Abrir", event -> FormAplicacion.show()));
            sysTray.getMenu().add(new dorkbox.systemTray.MenuItem("Salir", event -> {
                try {
                    WebServer.detener();
                } catch (Exception ex) {
                    Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
                }
                Platform.exit();
                sysTray.shutdown();
            }));
            return true;
        } catch (Exception ex) {
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.WARNING, "No se pudo iniciar Dorkbox SystemTray", ex);
            return false;
        }
    }

    private boolean iniciarBandejaAwtSeguro() {
        try {
            return iniciarBandejaAwt();
        } catch (IOException | AWTException ex) {
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.WARNING, "No se pudo iniciar AWT SystemTray", ex);
            return false;
        }
    }

    private boolean iniciarBandejaAwt() throws IOException, AWTException {
        if (!java.awt.SystemTray.isSupported()) {
            return false;
        }
        File iconFile = prepararIconoBandeja();
        if (iconFile == null) {
            return false;
        }
        BufferedImage iconImage = ImageIO.read(iconFile);
        if (iconImage == null) {
            return false;
        }

        PopupMenu popupMenu = new PopupMenu();
        java.awt.MenuItem abrirItem = new java.awt.MenuItem("Abrir");
        abrirItem.addActionListener(event -> FormAplicacion.show());
        java.awt.MenuItem salirItem = new java.awt.MenuItem("Salir");
        salirItem.addActionListener(event -> {
            try {
                WebServer.detener();
            } catch (Exception ex) {
                Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
            }
            quitarIconoBandejaAwt();
            Platform.exit();
        });
        popupMenu.add(abrirItem);
        popupMenu.add(salirItem);

        TrayIcon trayIcon = new TrayIcon(iconImage, "Jacobitus", popupMenu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                if (event.getButton() == java.awt.event.MouseEvent.BUTTON1) {
                    FormAplicacion.show();
                }
            }
        });
        java.awt.SystemTray.getSystemTray().add(trayIcon);
        return true;
    }

    private void quitarIconoBandejaAwt() {
        if (trayIcon == null) {
            return;
        }
        try {
            if (java.awt.SystemTray.isSupported()) {
                java.awt.SystemTray.getSystemTray().remove(trayIcon);
            }
        } finally {
            trayIcon = null;
        }
    }

    private File prepararIconoBandeja() throws IOException {
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "JacobitusCache_" + System.getProperty("user.name"));
        cacheDir.mkdirs();
        File iconFile = new File(cacheDir, "jacobitus-tray.png");
        try (InputStream iconStream = getClass().getClassLoader().getResourceAsStream("icon.png")) {
            if (iconStream == null) {
                return null;
            }
            BufferedImage source = ImageIO.read(iconStream);
            if (source == null) {
                return null;
            }
            BufferedImage trayIcon = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = trayIcon.createGraphics();
            try {
                graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.drawImage(source, 0, 0, 24, 24, null);
            } finally {
                graphics.dispose();
            }
            ImageIO.write(trayIcon, "png", iconFile);
        }
        return iconFile;
    }

    private static class ProveedorRenderJavaFx implements Renderer {
        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public ProviderType getType() {
            return ProviderType.JAVAFX;
        }

        @Override
        public boolean alreadyRunning() {
            return true;
        }

        @Override
        public boolean isEventThread() {
            return Platform.isFxApplicationThread();
        }

        @Override
        public int getGtkVersion() {
            return 3;
        }

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
