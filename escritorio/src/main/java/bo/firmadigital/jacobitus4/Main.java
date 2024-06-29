/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus.utilidades.OS;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javax.imageio.ImageIO;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.DefaultServlet;
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
        Request req = new Request();
        if (req.estado()) {
            if (args.length == 1) {
                req.show(args[0]);
            } else {
                req.show();
            }
        } else {
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
            ServletHolder staticResource = new ServletHolder("default", DefaultServlet.class);
            staticResource.setInitParameter("resourceBase", jettyServer.getClass().getClassLoader().getResource("web").toExternalForm());
            staticResource.setInitParameter("dirAllowed", "false");
            servletContextHandler.addServlet(staticResource, "/");

            HandlerList handlers = new HandlerList();
            handlers.setHandlers(new Handler[] { servletContextHandler });
            jettyServer.setHandler(handlers);
            ServletHolder servletHolder = servletContextHandler.addServlet(ServletContainer.class, "/api/*");
            servletHolder.setInitOrder(0);
            servletHolder.setInitParameter("jersey.config.server.provider.packages", "bo.firmadigital.jacobitus4.localhost9000");

            Config config = Config.getInstance();
            if (config.isSecondaryPortEnabled()) {
                ServletHolder servletHolderSign = servletContextHandler.addServlet(ServletContainer.class, "/sign/*");
                servletHolderSign.setInitOrder(1);
                servletHolderSign.setInitParameter("jersey.config.server.provider.packages", "bo.firmadigital.jacobitus4.resources4637");
            }

            if (config.isTertiaryPortEnabled()) {
                ServletHolder servletHolderDF = servletContextHandler.addServlet(ServletContainer.class, "/*");
                servletHolderDF.setInitOrder(1);
                servletHolderDF.setInitParameter("jersey.config.server.provider.packages", "bo.firmadigital.jacobitus4.resources3200");
            }
            try {
                createServerConnectorHTTPS();
                jettyServer.start();
                if (java.awt.SystemTray.isSupported()) {
                    java.awt.SystemTray tray = java.awt.SystemTray.getSystemTray();
                    java.awt.Image image = ImageIO.read(jettyServer.getClass().getClassLoader().getResource("sicon.png"));
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
                            jettyServer.stop();
                            jettyServer.destroy();
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
                    jettyServer.stop();
                    jettyServer.destroy();
                } catch (Exception ex2) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
                }
                App.run(false, false, false);
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    private static void createServerConnectorHTTPS() throws Exception {
	// HTTPS configuration
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());
 
        // Configuring SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
 
        // Defining keystore path and passwords
        sslContextFactory.setKeyStorePath(jettyServer.getClass().getClassLoader().getResource("server.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("12345678");
        sslContextFactory.setKeyManagerPassword("12345678");

        ArrayList<ServerConnector> connectors = new ArrayList();
 
        // Configuring the connector
        ServerConnector sslConnector = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
        sslConnector.setHost("127.0.0.1");
        sslConnector.setPort(9000);
        connectors.add(sslConnector);

        // Configuring the connector
        Config config = Config.getInstance();
        if (config.isSecondaryPortEnabled()) {
            ServerConnector sslConnector2 = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector2.setHost("127.0.0.1");
            sslConnector2.setPort(4637);
            connectors.add(sslConnector2);
        }

        // Configuring the connector
        if (config.isTertiaryPortEnabled()) {
            ServerConnector sslConnector3 = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector3.setHost("127.0.0.1");
            sslConnector3.setPort(3200);
            connectors.add(sslConnector3);
        }
 
        // Setting HTTP and HTTPS connectors
        jettyServer.setConnectors(connectors.toArray(new Connector[0]));
    }
}
