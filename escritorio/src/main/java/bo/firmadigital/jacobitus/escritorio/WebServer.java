package bo.firmadigital.jacobitus.escritorio;

import static org.eclipse.jetty.servlet.ServletContextHandler.NO_SESSIONS;

import java.net.BindException;
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

/*public class WebServer {
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
*/
public class WebServer {

    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());

    /*
     * Se crea una sola instancia de Jetty.
     */
    public static Server jettyServer;
    public static String mensaje = "";

    /*
     * ================================================================
     * CONFIGURACION POR DEFECTO
     * ================================================================
     *
     * En Windows / escritorio:
     *
     * JACOBITUS_HOST no definido
     *
     *    -> 127.0.0.1
     *
     *
     * En Debian:
     *
     * export JACOBITUS_HOST=0.0.0.0
     *
     *    -> disponible desde la red
     */
    private static final String HOST_DEFAULT = "127.0.0.1";
    private static final int PUERTO_9000_DEFAULT = 9000;

    public static void iniciar() {
        jettyServer = new Server();
        /*
         * ============================================================
         * CORS
         * ============================================================
         */
        FilterHolder filterHolder = new FilterHolder(CrossOriginFilter.class);

        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM,"*");
        filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER,"*");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM,"Content-Type,Authorization,X-Requested-With,"+ "Content-Length,Accept,Origin");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM,"GET,PUT,POST,DELETE,OPTIONS");
        filterHolder.setInitParameter(CrossOriginFilter.ALLOW_CREDENTIALS_PARAM,"false");
        /*
         * ============================================================
         * RECURSOS ESTATICOS
         * ============================================================
         */
        ServletHolder staticResource = new ServletHolder("default",DefaultServlet.class);

        staticResource.setInitParameter("resourceBase",jettyServer.getClass().getClassLoader().getResource("web").toExternalForm());
        staticResource.setInitParameter("dirAllowed","false");
        /*
         * ============================================================
         * PUERTO 9000
         *
         * API REST principal
         * ============================================================
         */
        ServletContextHandler servletContextHandler9000 = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler9000.setContextPath("/");
        servletContextHandler9000.setVirtualHosts(new String[]{"@localhost9000"});
        servletContextHandler9000.addFilter(filterHolder,"/*",null);
        servletContextHandler9000.addServlet(staticResource,"/");
        ServletHolder servletHolder9000 = servletContextHandler9000.addServlet(ServletContainer.class,"/api/*");
        servletHolder9000.setInitOrder(0);
        servletHolder9000.setInitParameter("jersey.config.server.provider.packages","bo.firmadigital.jacobitus.escritorio."+ "jetty.localhost9000");
        Config config = Config.getInstance();
        /*
         * ============================================================
         * PUERTO 4637
         *
         * Se mantiene exclusivamente en localhost.
         * ============================================================
         */
        ServletContextHandler servletContextHandler4637 = new ServletContextHandler(NO_SESSIONS);
        servletContextHandler4637.setContextPath("/");
        servletContextHandler4637.setVirtualHosts(
                new String[]{"@localhost4637"}
        );
        servletContextHandler4637.addFilter(
                filterHolder,
                "/*",
                null
        );
        servletContextHandler4637.addServlet(
                staticResource,
                "/"
        );
        if (config.isSecondaryPortEnabled()) {
            ServletHolder servletHolder4637 =
                    servletContextHandler4637.addServlet(
                            ServletContainer.class,
                            "/sign"
                    );
            servletHolder4637.setInitOrder(1);
            servletHolder4637.setInitParameter(
                    "jersey.config.server.provider.classnames",
                    "bo.firmadigital.jacobitus.escritorio."
                            + "jetty.localhost4637.FirmadorRest"
            );
        }
        /*
         * ============================================================
         * PUERTO 3200
         *
         * Se mantiene exclusivamente en localhost.
         * ============================================================
         */
        ServletContextHandler servletContextHandler3200 =
                new ServletContextHandler(NO_SESSIONS);
        servletContextHandler3200.setContextPath("/");
        servletContextHandler3200.setVirtualHosts(
                new String[]{"@localhost3200"}
        );
        servletContextHandler3200.addFilter(
                filterHolder,
                "/*",
                null
        );
        if (config.isTertiaryPortEnabled()) {
            ServletHolder servletHolder3200 =
                    servletContextHandler3200.addServlet(
                            ServletContainer.class,
                            "/*"
                    );
            servletHolder3200.setInitOrder(1);
            servletHolder3200.setInitParameter("jersey.config.server.provider.packages","bo.firmadigital.jacobitus.escritorio."+ "jetty.localhost3200");
        }
        /*
         * ============================================================
         * HANDLERS
         * ============================================================
         */
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(
                new Handler[]{
                        servletContextHandler9000,
                        servletContextHandler4637,
                        servletContextHandler3200
                }
        );
        jettyServer.setHandler(handlers);
        /*
         * ============================================================
         * INICIAR SERVIDOR
         * ============================================================
         */
        try {
            configurarHttps();
            jettyServer.start();
            imprimirInformacionServidor();
        } catch (Exception ex) {
            procesarErrorInicio(ex);
            throw new JacobitusException(
                    "Error al iniciar el servidor Jacobitus."
            );
        }
    }
    /*
     * ================================================================
     * DETENER SERVIDOR
     * ================================================================
     */
    public static void detener() {
        if (jettyServer == null) {
            return;
        }
        /*
        * ==========================================
        * DETENER SERVIDOR
        * ==========================================
        */
        try {
            if (jettyServer.isRunning() || jettyServer.isStarted() || jettyServer.isStarting()) {
                LOGGER.info("Deteniendo servidor Jetty...");
                jettyServer.stop();
                LOGGER.info("Servidor Jetty detenido.");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,"Error al detener Jetty: "+ ex.getMessage(),ex);
        }
        /*
        * ==========================================
        * LIBERAR RECURSOS
        * ==========================================
        */
        try {
            jettyServer.destroy();
            LOGGER.info("Recursos de Jetty liberados.");
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,"Error al liberar recursos de Jetty: "+ ex.getMessage(),ex);
        }
        jettyServer = null;
    }
    /*
     * ================================================================
     * CONFIGURACION HTTPS
     * ================================================================
     */
   
private static void configurarHttps() throws Exception {
        /*
        * ============================================================
        * CONFIGURACION HTTPS
        * ============================================================
        */
        HttpConfiguration https = new HttpConfiguration();
        /*
        * Determinar si estamos ejecutando Jacobitus como
        * servidor Linux/Debian headless.
        */
        boolean esServidorLinux = esServidorLinux();
        /*
        * ============================================================
        * SECURE REQUEST CUSTOMIZER / SNI
        * ============================================================
        */
        SecureRequestCustomizer secureRequestCustomizer = new SecureRequestCustomizer();

        if (esServidorLinux) {
            /*
            * En Debian inicialmente permitimos acceso mediante IP
            * o nombre distinto de localhost.
            *
            * Cuando el certificado institucional tenga configurado
            * correctamente el DNS/SAN definitivo, estos valores
            * pueden volver a true.
            */
            secureRequestCustomizer.setSniHostCheck(false);
            secureRequestCustomizer.setSniRequired(false);

        } else {
            /*
            * Windows:
            * conservar comportamiento normal/seguro de Jacobitus.
            */
            secureRequestCustomizer.setSniHostCheck(true);
            secureRequestCustomizer.setSniRequired(false);
        }

        /*
        * IMPORTANTE:
        * agregar ESTA instancia y no crear otra nueva.
        */
        https.addCustomizer(secureRequestCustomizer);
        /*
        * ============================================================
        * SSL CONTEXT
        * ============================================================
        */
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();

        if (esServidorLinux) {
            /*
            * Mientras Debian pueda ser accedido por IP.
            */
            sslContextFactory.setSniRequired(false);

        } else {

            /*
            * Comportamiento original Windows.
            */
            sslContextFactory.setSniRequired(false);
        }
        /*
        * ============================================================
        * CERTIFICADO SSL
        * ============================================================
        *
        * Windows:
        *      server.jks original de Jacobitus
        *
        * Debian headless:
        *      certificado institucional PKCS12
        */
        configurarCertificadoSsl(
                sslContextFactory
        );

        List<ServerConnector> connectors = new ArrayList<ServerConnector>();
        /*
        * ============================================================
        * PUERTO 9000
        * ============================================================
        *
        * Windows:
        *
        *      JACOBITUS_HOST no definido
        *      => 127.0.0.1
        *
        * Debian:
        *
        *      JACOBITUS_HOST=0.0.0.0
        *      => servicio disponible desde la red
        */
        String host9000 =
                obtenerVariableEntorno(
                        "JACOBITUS_HOST",
                        HOST_DEFAULT
                );

        int puerto9000 =
                obtenerPuerto(
                        "JACOBITUS_PORT",
                        PUERTO_9000_DEFAULT
                );


        ServerConnector sslConnector9000 =
                new ServerConnector(
                        jettyServer,
                        new SslConnectionFactory(
                                sslContextFactory,
                                "http/1.1"
                        ),
                        new HttpConnectionFactory(
                                https
                        )
                );
        sslConnector9000.setHost(
                host9000
        );

        sslConnector9000.setPort(
                puerto9000
        );

        sslConnector9000.setName(
                "localhost9000"
        );

        connectors.add(
                sslConnector9000
        );


        Config config =
                Config.getInstance();


        /*
        * ============================================================
        * PUERTO 4637
        * ============================================================
        *
        * Firma local.
        *
        * Se mantiene SIEMPRE en localhost para no exponerlo
        * externamente.
        */
        if (config.isSecondaryPortEnabled()) {

            ServerConnector sslConnector4637 =
                    new ServerConnector(
                            jettyServer,
                            new SslConnectionFactory(
                                    sslContextFactory,
                                    "http/1.1"
                            ),
                            new HttpConnectionFactory(
                                    https
                            )
                    );
            sslConnector4637.setHost(
                    "127.0.0.1"
            );
            sslConnector4637.setPort(
                    4637
            );
            sslConnector4637.setName(
                    "localhost4637"
            );
            connectors.add(
                    sslConnector4637
            );
        }
       /*
        * ============================================================
        * PUERTO 3200
        * ============================================================
        *
        * También permanece SIEMPRE ligado a localhost.
        */
        if (config.isTertiaryPortEnabled()) {

            ServerConnector sslConnector3200 =
                    new ServerConnector(
                            jettyServer,
                            new SslConnectionFactory(
                                    sslContextFactory,
                                    "http/1.1"
                            ),
                            new HttpConnectionFactory(
                                    https
                            )
                    );


            sslConnector3200.setHost(
                    "127.0.0.1"
            );
            sslConnector3200.setPort(
                    3200
            );
            sslConnector3200.setName(
                    "localhost3200"
            );
            connectors.add(
                    sslConnector3200
            );
        }

        /*
        * ============================================================
        * REGISTRAR CONNECTORS
        * ============================================================
        */
        jettyServer.setConnectors(
                connectors.toArray(
                        new Connector[0]
                )
        );
    }
    private static boolean esServidorLinux() {
        String osName =
                System.getProperty(
                        "os.name",
                        ""
                ).toLowerCase();
        boolean esLinux =
                osName.contains("linux");
        String headlessEnv =
                System.getenv(
                        "JACOBITUS_HEADLESS"
                );
        boolean esHeadless =
                headlessEnv != null
                        && "true".equalsIgnoreCase(
                                headlessEnv.trim()
                        );
        return esLinux && esHeadless;
    }   
    /*
     * ================================================================
     * VARIABLE DE ENTORNO
     * ================================================================
     */
    private static String obtenerVariableEntorno(String nombre,String valorDefault) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.trim().isEmpty()) {
            return valorDefault;
        }
        return valor.trim();
    }
    /*
     * ================================================================
     * PUERTO CONFIGURABLE
     * ================================================================
     */
    private static int obtenerPuerto(String variable, int puertoDefault) {
        String valor = System.getenv(variable);
        if (valor == null || valor.trim().isEmpty()) {
            return puertoDefault;
        }
        try {
            int puerto = Integer.parseInt(valor.trim());
            if (puerto < 1 || puerto > 65535) {
                throw new IllegalArgumentException("Puerto fuera de rango: " + puerto);
            }
            return puerto;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor inválido para " + variable + ": " + valor, ex);
        }
    }
    /*
     * ================================================================
     * INFORMACION DEL SERVIDOR
     * ================================================================
     */
    private static void imprimirInformacionServidor() {
        LOGGER.info("==============================================");
        LOGGER.info("Servidor Jukubitus iniciado correctamente");
        for (Connector connector: jettyServer.getConnectors()) {
            if (connector instanceof ServerConnector) {
                ServerConnector sc =(ServerConnector) connector;
                LOGGER.info(
                        "Connector: "
                                + sc.getName()
                                + " | Host: "
                                + sc.getHost()
                                + " | Puerto: "
                                + sc.getLocalPort()
                );
            }
        }

        LOGGER.info("==============================================");
    }
    /*
     * ================================================================
     * MANEJO DE ERRORES
     * ================================================================
     */
    private static void procesarErrorInicio(Exception ex) {
        try {
            if (esErrorBind(ex)) {
                WebServer.mensaje = "No se pudo iniciar Jukubitus: " + "uno de los puertos configurados " + "ya se encuentra en uso.";
            } else {
                WebServer.mensaje = "Servicio detenido. " + "No podrá interactuar " + "con aplicaciones web.";
            }
            if (jettyServer != null) {
                try {
                    jettyServer.stop();
                } catch (Exception stopException) {
                    LOGGER.log(Level.WARNING,"Error al detener Jetty.", stopException);
                }
                try {
                    jettyServer.destroy();
                } catch (Exception destroyException) {
                    LOGGER.log(Level.WARNING,"Error al destruir Jetty.",destroyException);
                }
            }
        } finally {
            LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
        }
    }
    /*
     * ================================================================
     * BUSCAR BindException EN LA CADENA DE EXCEPCIONES
     * ================================================================
     */
    private static boolean esErrorBind(Throwable throwable) {
        Throwable actual = throwable;
        while (actual != null) {
            if (actual instanceof BindException) {
                return true;
            }
            actual = actual.getCause();
        }
        return false;
    }
    private static void configurarCertificadoSsl(
        SslContextFactory.Server sslContextFactory) {
        boolean esServidorLinux = esServidorLinux();
        /*
        * ============================================================
        * DEBIAN / LINUX HEADLESS
        * ============================================================
        */
        if (esServidorLinux) {
            String keyStorePath =
                    System.getenv(
                            "JACOBITUS_KEYSTORE_PATH"
                    );
            String keyStorePassword =
                    System.getenv(
                            "JACOBITUS_KEYSTORE_PASSWORD"
                    );
            if (keyStorePath == null
                    || keyStorePath.trim().isEmpty()) {

                throw new IllegalStateException(
                        "La variable JACOBITUS_KEYSTORE_PATH "
                                + "no está configurada."
                );
            }
            if (keyStorePassword == null
                    || keyStorePassword.trim().isEmpty()) {

                throw new IllegalStateException(
                        "La variable JACOBITUS_KEYSTORE_PASSWORD "
                                + "no está configurada."
                );
            }
            sslContextFactory.setKeyStorePath(
                    keyStorePath.trim()
            );
            sslContextFactory.setKeyStoreType(
                    "PKCS12"
            );
            sslContextFactory.setKeyStorePassword(
                    keyStorePassword
            );
            sslContextFactory.setKeyManagerPassword(
                    keyStorePassword
            );
            LOGGER.info(
                    "HTTPS configurado con certificado "
                            + "institucional para servidor Linux."
            );
            LOGGER.info(
                    "KeyStore: "
                            + keyStorePath
            );
        } else {

            /*
            * ============================================================
            * WINDOWS / JACOBITUS ORIGINAL
            * ============================================================
            */
            java.net.URL serverJks =
                    jettyServer
                            .getClass()
                            .getClassLoader()
                            .getResource(
                                    "server.jks"
                            );


            if (serverJks == null) {

                throw new IllegalStateException(
                        "No se encontró el certificado "
                                + "server.jks de Jacobitus."
                );
            }
            sslContextFactory.setKeyStorePath(
                    serverJks.toExternalForm()
            );
            sslContextFactory.setKeyStorePassword(
                    "12345678"
            );
            sslContextFactory.setKeyManagerPassword(
                    "12345678"
            );
            LOGGER.info(
                    "HTTPS configurado con server.jks "
                            + "original de Jacobitus."
            );
        }
    }
}