package bo.firmadigital.jacobitus.escritorio.formularios;
import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.PopupMenu;
import java.awt.RenderingHints;
import java.awt.TrayIcon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
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
    private String ultimoDiagnostico = null;

    public boolean iniciarBandejaSistema() {
        ultimoDiagnostico = null;
        if (SistemaOperativoHelper.esWindows() || SistemaOperativoHelper.esMacOS()) {
            return iniciarBandejaAwtSeguro();
        }

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
                if (hayAppIndicatorDisponible()) {
                    SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.AppIndicator;
                    prepararAliasAyatanaAppIndicator();
                } else {
                    SystemTray.FORCE_TRAY_TYPE = SystemTray.TrayType.Gtk;
                }
                SizeAndScalingLinux.OVERRIDE_TRAY_SIZE = 24;
                SizeAndScalingLinux.OVERRIDE_MENU_SIZE = 16;
            }
            sysTray = SystemTray.get("Jacobitus");
            if (sysTray == null) {
                ultimoDiagnostico = diagnosticoBandejaNoDisponible();
                return false;
            }
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.INFO,
                    "Dorkbox SystemTray iniciado. Tipo: {0}. Entorno: {1}",
                    new Object[] { sysTray.getType(), detectarEntornoGrafico() });
            File iconFile = prepararIconoBandeja();
            if (iconFile == null) {
                ultimoDiagnostico = "No se pudo preparar el icono de Jacobitus para la bandeja del sistema.";
                return false;
            }
            sysTray.setTooltip("Jacobitus");
            sysTray.setImage(iconFile);
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.INFO,
                    "Icono de bandeja configurado: {0}", iconFile.getAbsolutePath());
            sysTray.getMenu().add(new dorkbox.systemTray.MenuItem("Abrir", event -> FormAplicacion.show()));
            sysTray.getMenu().add(new dorkbox.systemTray.MenuItem("Salir", event -> FormAplicacion.salir()));
            return true;
        } catch (Exception ex) {
            ultimoDiagnostico = diagnosticoBandejaNoDisponible();
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.WARNING, "No se pudo iniciar Dorkbox SystemTray", ex);
            return false;
        }
    }

    private boolean iniciarBandejaAwtSeguro() {
        try {
            return iniciarBandejaAwt();
        } catch (IOException | AWTException ex) {
            ultimoDiagnostico = diagnosticoBandejaNoDisponible();
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.WARNING, "No se pudo iniciar AWT SystemTray", ex);
            return false;
        }
    }

    private boolean iniciarBandejaAwt() throws IOException, AWTException {
        if (!java.awt.SystemTray.isSupported()) {
            ultimoDiagnostico = diagnosticoBandejaNoDisponible();
            return false;
        }
        File iconFile = prepararIconoBandeja();
        if (iconFile == null) {
            ultimoDiagnostico = "No se pudo preparar el icono de Jacobitus para la bandeja del sistema.";
            return false;
        }
        BufferedImage iconImage = ImageIO.read(iconFile);
        if (iconImage == null) {
            ultimoDiagnostico = "No se pudo leer el icono de Jacobitus para la bandeja del sistema.";
            return false;
        }

        PopupMenu popupMenu = new PopupMenu();
        java.awt.MenuItem abrirItem = new java.awt.MenuItem("Abrir");
        abrirItem.addActionListener(event -> FormAplicacion.show());
        java.awt.MenuItem salirItem = new java.awt.MenuItem("Salir");
        salirItem.addActionListener(event -> FormAplicacion.salir());
        popupMenu.add(abrirItem);
        popupMenu.add(salirItem);

        trayIcon = new TrayIcon(iconImage, "Jacobitus", popupMenu);
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

    private void prepararAliasAyatanaAppIndicator() {
        File ayatana = buscarAyatanaAppIndicator();
        if (ayatana == null) {
            return;
        }
        try {
            File dir = new File(System.getProperty("java.io.tmpdir"), "JacobitusAppIndicator_" + System.getProperty("user.name"));
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            String[] alias = { "libappindicator3.so", "libappindicator3-1.so", "libappindicator-gtk3.so",
                    "libappindicator-gtk3-1.so" };
            for (String nombre : alias) {
                Path link = new File(dir, nombre).toPath();
                if (!Files.exists(link)) {
                    Files.createSymbolicLink(link, ayatana.toPath());
                }
            }
            String jnaLibraryPath = System.getProperty("jna.library.path");
            String dirPath = dir.getAbsolutePath();
            if (jnaLibraryPath == null || jnaLibraryPath.trim().equals("")) {
                System.setProperty("jna.library.path", dirPath);
            } else if (!jnaLibraryPath.contains(dirPath)) {
                System.setProperty("jna.library.path", dirPath + File.pathSeparator + jnaLibraryPath);
            }
        } catch (Exception ex) {
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.WARNING,
                    "No se pudo preparar alias para Ayatana AppIndicator", ex);
        }
    }

    private File buscarAyatanaAppIndicator() {
        File file = buscarLibreria("libayatana-appindicator3.so.1");
        if (file != null) {
            return file;
        }
        String[] rutasFallback = { "/usr/lib/x86_64-linux-gnu", "/lib/x86_64-linux-gnu", "/usr/lib", "/usr/lib64" };
        for (String ruta : rutasFallback) {
            file = new File(ruta, "libayatana-appindicator3.so.1");
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    private boolean hayAppIndicatorDisponible() {
        return buscarAyatanaAppIndicator() != null
                || buscarLibreria("libappindicator3.so") != null
                || buscarLibreria("libappindicator3.so.1") != null
                || buscarLibreria("libappindicator3-1.so") != null
                || buscarLibreria("libappindicator-gtk3.so") != null
                || buscarLibreria("libappindicator-gtk3-1.so") != null;
    }

    private File buscarLibreria(String nombre) {
        try {
            Process process = new ProcessBuilder("ldconfig", "-p").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.contains(nombre) || !line.contains("=>")) {
                        continue;
                    }
                    String ruta = line.substring(line.indexOf("=>") + 2).trim();
                    File file = new File(ruta);
                    if (file.exists()) {
                        return file;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(FormAplicacion.class.getName()).log(Level.FINE,
                    "No se pudo consultar ldconfig para AppIndicator", ex);
        }
        String javaLibraryPath = System.getProperty("java.library.path", "");
        for (String ruta : javaLibraryPath.split(File.pathSeparator)) {
            if (ruta.trim().equals("")) {
                continue;
            }
            File file = new File(ruta, nombre);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    public String getUltimoDiagnostico() {
        return ultimoDiagnostico;
    }

    public static String diagnosticoBandejaNoDisponible() {
        if (GraphicsEnvironment.isHeadless()) {
            return "No se puede iniciar el icono en bandeja porque Java está ejecutándose en modo headless.";
        }
        if (!SistemaOperativoHelper.esUnix()) {
            if (!java.awt.SystemTray.isSupported()) {
                return "No se puede iniciar el icono en bandeja porque Java/AWT reporta que SystemTray no está soportado en este sistema.";
            }
            return "Java/AWT reporta soporte para SystemTray, pero no pudo crear el icono. Revise los registros de la aplicación para ver la causa exacta.";
        }
        String entorno = detectarEntornoGrafico();
        String entornoLower = entorno.toLowerCase();
        if (entornoLower.contains("chrome")) {
            return "No se puede iniciar el icono en bandeja porque Dorkbox SystemTray no soporta ChromeOS.";
        }
        return "Se detectó el entorno " + entorno
                + ". No se pudo crear el icono en bandeja. Verifique que libayatana-appindicator3/libappindicator3 esté instalado o que el panel tenga habilitada el área de notificación.";
    }

    public static String diagnosticoActivacionUnix() {
        String entorno = detectarEntornoGrafico();
        String entornoLower = entorno.toLowerCase();
        if (GraphicsEnvironment.isHeadless()) {
            return "Java está ejecutándose en modo headless; el icono en bandeja no podrá mostrarse.";
        }
        if (entornoLower.contains("chrome")) {
            return "Dorkbox SystemTray no soporta ChromeOS.";
        }
        return "Se detectó el entorno " + entorno
                + ". Jacobitus intentará usar AppIndicator o el área de notificación GTK al reiniciar; verifique que el panel tenga habilitado el soporte para iconos de bandeja.";
    }

    private static String detectarEntornoGrafico() {
        String entorno = System.getenv("XDG_CURRENT_DESKTOP");
        if (entorno == null || entorno.trim().equals("")) {
            entorno = System.getenv("DESKTOP_SESSION");
        }
        if (entorno == null || entorno.trim().equals("")) {
            entorno = System.getenv("GDMSESSION");
        }
        return entorno == null || entorno.trim().equals("") ? "desconocido" : entorno;
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
