/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 * @author ADSIB
 */
public class Main {
    public static Server jettyServer = new Server();

    public static void main(String[] args) {
        /*String configName = "/tmp/fido_pkcs11_7305928436468159072.cfg";
        Provider p = Security.getProvider("SunPKCS11");
        p = p.configure(configName);
        Security.addProvider(p);
        PKCS11 p11 = new PKCS11(p);
        long[] slots = p11.C_GetSlotList(true);
        for (long s : slots) {
            System.out.println(s);
        }*/
        /*GestorSlot gestorSlot = GestorSlot.getInstance();
        try {
            gestorSlot.adicionarProveedor(proveedores());
        } catch (IOException | PKCS11Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        ServletContextHandler servletContextHandler = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler.setContextPath("/");
        // AGREGAR FILTER CORS
        FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
        servletContextHandler.addFilter(filterHolder, "/*", null);
        // Aplicar
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[]{ "index.html" });
        resourceHandler.setResourceBase(jettyServer.getClass().getClassLoader().getResource("web").toExternalForm());
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resourceHandler, servletContextHandler });
        jettyServer.setHandler(handlers);
        ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
        servletHolder.setInitOrder(0);
        servletHolder.setInitParameter("jersey.config.server.provider.packages", "bo.firmadigital.jacobitus4.resources");
        try {
            createServerConnectorHTTPS();
            jettyServer.start();
            if (java.awt.SystemTray.isSupported()) {
                java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                java.awt.Image image = ImageIO.read(jettyServer.getClass().getClassLoader().getResource("sicon.png"));
                java.awt.TrayIcon trayIcon = new java.awt.TrayIcon(image);
                trayIcon.addActionListener((ActionEvent e) -> {
                    App.show();
                });
                java.awt.MenuItem exitItem = new java.awt.MenuItem("Salir");
                exitItem.addActionListener(event -> {
                    try {
                        jettyServer.stop();
                        jettyServer.destroy();
                    } catch (Exception ex) {
                        Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Platform.exit();
                    tray.remove(trayIcon);
                });
                final java.awt.PopupMenu popup = new java.awt.PopupMenu();
                popup.add(exitItem);
                trayIcon.setPopupMenu(popup);
                tray.add(trayIcon);
                App.run(true, true);
            } else {
                App.run(true, false);
            }
        } catch (Exception ex) {
            try {
                jettyServer.stop();
                jettyServer.destroy();
            } catch (Exception ex2) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex2);
            }
            App.run(false, false);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * cargo la lista de librerias para que el token trabaje con el sistema.
     *
     * @param conf
     * @return retorna la lista de librerias para que el token trabaje con el
     * sistema.
     * @throws IOException
     */
    /*private static String[] proveedores() throws FileNotFoundException, IOException {
        ArrayList<String> librerias = new ArrayList<>();
        File folder = new File(FileSystemView.getFileSystemView().getDefaultDirectory().getPath() + File.separator + "FidoProfiles");
        File[] files = folder.listFiles((File file) -> file.isFile() && file.getName().contains(".profile"));
        for (File file : files) {
            try (InputStream input = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(input);
                librerias.add(prop.getProperty("driverPath"));
            }
        }
        return librerias.toArray(new String[librerias.size()]);
    }*/

    private static void createServerConnectorHTTPS() throws Exception {
	// HTTP Configuration
        HttpConfiguration http = new HttpConfiguration();
        http.addCustomizer(new SecureRequestCustomizer());
 
        // Configuration for HTTPS redirect
        http.setSecurePort(9000);
        http.setSecureScheme("https");
 
        // HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());
 
        // Configuring SSL
        SslContextFactory sslContextFactory = new SslContextFactory();
 
        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(jettyServer.getClass().getClassLoader().getResource("server.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("12345678");
        sslContextFactory.setKeyManagerPassword("12345678");
 
        // Configuring the connector
        ServerConnector sslConnector = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
        sslConnector.setPort(9000);
 
        // Setting HTTP and HTTPS connectors
        jettyServer.setConnectors(new Connector[]{sslConnector});
    }
}
