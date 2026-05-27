package bo.firmadigital.jacobitus4.jetty.localhost9000;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.TokenServicio;

@Path("/token")
public class TokenRest {
    @GET
    @Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String status() {
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.status());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/connected")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String connected() {
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.connected());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/data")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String data(String body) {
        TokenServicio servicio = new TokenServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = om.readValue(body, TokenAutenticacionDto.class);
            return om.writeValueAsString(servicio.data(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
