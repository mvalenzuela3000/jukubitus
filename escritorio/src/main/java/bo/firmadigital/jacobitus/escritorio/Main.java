package bo.firmadigital.jacobitus.escritorio;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONObject;

import bo.firmadigital.jacobitus.escritorio.formularios.FormAplicacion;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        /*
         * ============================================================
         * MODO SERVIDOR / HEADLESS
         * ============================================================
         *
         * Este modo está pensado para Debian/Linux Server.
         *
         * No inicia JavaFX.
         * No ejecuta FormAplicacion.
         * Solamente inicia Jetty/WebServer y mantiene vivo el proceso.
         *
         * Se puede activar mediante:
         *
         *   export JACOBITUS_HEADLESS=true
         *
         * o:
         *
         *   java -Djacobitus.headless=true ...
         */
        if (esModoServidor()) {
            ejecutarModoServidor();
            return;
        }

        /*
         * ============================================================
         * MODO ESCRITORIO ORIGINAL
         * ============================================================
         *
         * Conservamos el funcionamiento actual para Windows/Linux
         * con interfaz gráfica.
         */
        ejecutarModoEscritorio(args);
    }

    /**
     * Determina si Jukubitus debe ejecutarse sin interfaz gráfica.
     *
     * Se puede activar mediante:
     *
     * Variable de entorno:
     * JACOBITUS_HEADLESS=true
     *
     * o propiedad Java:
     * -Djacobitus.headless=true
     */
    private static boolean esModoServidor() {

        String variableEntorno = System.getenv("JACOBITUS_HEADLESS");

        if (variableEntorno != null && "true".equalsIgnoreCase(variableEntorno.trim())) {
            return true;
        }

        String propiedad = System.getProperty("jacobitus.headless","false");

        return Boolean.parseBoolean(propiedad);
    }

    /**
     * Ejecuta Jukubitus únicamente como servidor REST.
     *
     * No inicia JavaFX.
     */
    private static void ejecutarModoServidor() {
        try {
            LOGGER.info("==============================================");
            LOGGER.info("Iniciando Jukubitus en modo servidor/headless");
            LOGGER.info("Interfaz gráfica JavaFX deshabilitada");
            LOGGER.info("==============================================");
            /*
             * Iniciar Jetty / Jersey
             */
            WebServer.iniciar();
            /*
             * Registrar cierre ordenado.
             *
             * Esto será especialmente útil cuando posteriormente
             * ejecutemos Jukubitus mediante systemd:
             *
             * systemctl stop jukubitus
             */
            Runtime.getRuntime().addShutdownHook(
                    new Thread(() -> {
                        LOGGER.info("Deteniendo servidor Jukubitus...");
                        try {
                            WebServer.detener();
                            LOGGER.info("Servidor Jacobitus detenido correctamente.");
                        } catch (Exception ex) {
                            LOGGER.log(Level.SEVERE,"Error al detener Jukubitus: " + ex.getMessage(),ex);
                        }
                    }, "jacobitus-shutdown")
            );
            LOGGER.info("Servidor Jukubitus iniciado correctamente.");
            LOGGER.info("Modo servidor activo.");

            /*
             * Mantener vivo el proceso principal.
             *
             * No dependemos de JavaFX para mantener abierta la aplicación.
             */
            CountDownLatch mantenerVivo = new CountDownLatch(1);
            mantenerVivo.await();

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING,"Proceso Jukubitus interrumpido.",ex);
            detenerServidorSilenciosamente();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE,"No se pudo iniciar el servidor Jukubitus: " + ex.getMessage(), ex);
            detenerServidorSilenciosamente();
            /*
             * Muy importante para systemd.
             *
             * Si Jetty no pudo arrancar, el proceso debe devolver un código de error real.
             */
            System.exit(1);
        }
    }

    /**
     * Ejecuta Jukubitus con su comportamiento original de escritorio.
     */
    private static void ejecutarModoEscritorio(String[] args) {

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
                if (args.length == 1) {
                    String[] parts = args[0].split("\\?");
                    if (parts.length == 2) {
                        JSONObject body = Request.splitQuery(parts[1]);
                        FormAplicacion.run(
                                true,
                                true,
                                body.getString("url"),
                                body.getString("token"),
                                body.getString("urlpost")
                        );
                    } else {
                        FormAplicacion.run(
                                true,
                                true,
                                args[0]
                        );
                    }
                } else {
                    FormAplicacion.run(
                            true,
                            true
                    );
                }

            } catch (Exception ex) {
                try {
                    WebServer.detener();
                } catch (Exception ex2) {
                    LOGGER.log(Level.SEVERE, ex2.getMessage(), ex2);
                }

                /*
                 * Aquí mantenemos el comportamiento original
                 * porque solamente se ejecutará en modo escritorio.
                 */
                FormAplicacion.run(
                        false,
                        false
                );

                LOGGER.log(Level.SEVERE,ex.getMessage(),ex);
            }
        }
    }

    /**
     * Intenta detener WebServer sin generar una segunda excepción.
     */
    private static void detenerServidorSilenciosamente() {
        try {
            WebServer.detener();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING,"No fue posible detener completamente WebServer: "+ ex.getMessage(),ex);
        }
    }
}