package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus.escritorio.jetty.JettyHelper;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.servicios.EstadoServicio;

@Path("/status")
public class EstadoRest {
    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
        EstadoServicio servicio = new EstadoServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.status());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
