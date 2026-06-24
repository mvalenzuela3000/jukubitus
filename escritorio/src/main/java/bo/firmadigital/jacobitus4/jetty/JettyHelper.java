package bo.firmadigital.jacobitus.escritorio.jetty;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;

public class JettyHelper {
    public static String generarRespuesta(String mensaje) {
        ObjectMapper om = new ObjectMapper();

        RespuestaDto respuesta = new RespuestaDto();
        respuesta.setFinalizado(false);
        respuesta.setMensaje(mensaje);
        
        try {
            return om.writeValueAsString(respuesta);
        } catch (IOException e) {
            return null;
        }
    }
}
