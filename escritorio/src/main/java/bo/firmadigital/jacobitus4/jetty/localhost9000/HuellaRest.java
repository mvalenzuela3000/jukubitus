package bo.firmadigital.jacobitus4.jetty.localhost9000;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.HuellaServicio;

@Path("/huella")
public class HuellaRest {
    @GET
    @Path("/capturar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String capturar() {
        HuellaServicio servicio = new HuellaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.capturar());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
