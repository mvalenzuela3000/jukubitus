package bo.firmadigital.jacobitus.escritorio;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.glassfish.jersey.servlet.ServletContainer;

import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.escritorio.comun.Config;

public class WebServer {
    public static Server jettyServer = new Server();
    public static String mensaje = "";

    public static void iniciar() {
        // Configuracion de CORS
        FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,
                "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");

        // Sitio web
        ServletHolder staticResource = new ServletHolder("default", DefaultServlet.class);
        staticResource.setInitParameter("resourceBase", jettyServer.getClass().getClassLoader().getResource("web").toExternalForm());
        staticResource.setInitParameter("dirAllowed", "false");

        // https://localhost:9000
        ServletContextHandler servletContextHandler9000 = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler9000.setContextPath("/");
        servletContextHandler9000.setVirtualHosts(new String[]{"@localhost9000"});
        servletContextHandler9000.addFilter(filterHolder, "/*", null);
        servletContextHandler9000.addServlet(staticResource, "/");
        ServletHolder servletHolder9000 = servletContextHandler9000.addServlet(ServletContainer.class, "/api/*");
        servletHolder9000.setInitOrder(0);
        servletHolder9000.setInitParameter("jersey.config.server.provider.packages",
                "bo.firmadigital.jacobitus.escritorio.jetty.localhost9000");

        Config config = Config.getInstance();

        // https://localhost:4637
        ServletContextHandler servletContextHandler4637 = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler4637.setContextPath("/");
        servletContextHandler4637.setVirtualHosts(new String[]{"@localhost4637"});
        servletContextHandler4637.addFilter(filterHolder, "/*", null);
        servletContextHandler4637.addServlet(staticResource, "/");
        if (config.isSecondaryPortEnabled()) {
            ServletHolder servletHolder4637 = servletContextHandler4637.addServlet(ServletContainer.class, "/sign");
            servletHolder4637.setInitOrder(1);
            servletHolder4637.setInitParameter("jersey.config.server.provider.classnames",
                    "bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.FirmadorRest");
        }

        // https://localhost:3200
        ServletContextHandler servletContextHandler3200 = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler3200.setContextPath("/");
        servletContextHandler3200.setVirtualHosts(new String[]{"@localhost3200"});
        servletContextHandler3200.addFilter(filterHolder, "/*", null);
        if (config.isTertiaryPortEnabled()) {
            ServletHolder servletHolder3200 = servletContextHandler3200.addServlet(ServletContainer.class, "/*");
            servletHolder3200.setInitOrder(1);
            servletHolder3200.setInitParameter("jersey.config.server.provider.packages",
                    "bo.firmadigital.jacobitus.escritorio.jetty.localhost3200");
        }

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { servletContextHandler9000, servletContextHandler4637, servletContextHandler3200 });
        jettyServer.setHandler(handlers);

        try {
            configurarHttps();
            jettyServer.start();
        } catch (Exception ex) {
            try {
                switch (ex.getMessage()) {
                    case "Failed to bind to /127.0.0.1:9000":
                        WebServer.mensaje = "Hubo un problema con el puerto 9000.";
                        break;
                    case "Failed to bind to /127.0.0.1:4637":
                        WebServer.mensaje = "Hubo un problema con el puerto 4637.";
                        break;
                    case "Failed to bind to /127.0.0.1:3200":
                        WebServer.mensaje = "Hubo un problema con el puerto 3200.";
                        break;
                    default:
                        WebServer.mensaje = "Servicio detenido, no podrá interactuar con aplicaciones web.";
                        break;
                }
                jettyServer.stop();
                jettyServer.destroy();
            } catch (Exception ex2) {
                Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, ex2.getMessage(), ex2);
            }
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new JacobitusException("Error al iniciar el servidor local.");
        }
    }

    public static void detener() {
        try {
            jettyServer.stop();
            jettyServer.destroy();
        } catch (Exception ex) {
            Logger.getLogger(WebServer.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    private static void configurarHttps() throws Exception {
        HttpConfiguration https = new HttpConfiguration();
        https.addCustomizer(new SecureRequestCustomizer());

        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(jettyServer.getClass().getClassLoader().getResource("server.jks").toExternalForm());
        sslContextFactory.setKeyStorePassword("12345678");
        sslContextFactory.setKeyManagerPassword("12345678");

        List<ServerConnector> connectors = new ArrayList<ServerConnector>();
 
        // Configuracion para el puerto 9000
        ServerConnector sslConnector9000 = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
        sslConnector9000.setHost("127.0.0.1");
        sslConnector9000.setPort(9000);
        sslConnector9000.setName("localhost9000");
        connectors.add(sslConnector9000);

        Config config = Config.getInstance();

        // Configuracion para el puerto 4637
        if (config.isSecondaryPortEnabled()) {
            ServerConnector sslConnector4637 = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector4637.setHost("127.0.0.1");
            sslConnector4637.setPort(4637);
            sslConnector4637.setName("localhost4637");
            connectors.add(sslConnector4637);
        }

        // Configuracion para el puerto 3200
        if (config.isTertiaryPortEnabled()) {
            ServerConnector sslConnector3200 = new ServerConnector(jettyServer, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
            sslConnector3200.setHost("127.0.0.1");
            sslConnector3200.setPort(3200);
            sslConnector3200.setName("localhost3200");
            connectors.add(sslConnector3200);
        }
 
        jettyServer.setConnectors(connectors.toArray(new Connector[0]));
    }
}
