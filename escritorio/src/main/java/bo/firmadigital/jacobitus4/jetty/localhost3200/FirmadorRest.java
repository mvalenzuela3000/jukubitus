package bo.firmadigital.jacobitus4.jetty.localhost3200;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost3200.dtos.FirmaPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost3200.servicios.FirmadorServicio;

@Path("/")
public class FirmadorRest {
    @GET
    @Path("/")
    @Produces(MediaType.TEXT_HTML)
    public String index() {
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.inicio();
    }

    @GET
    @Path("/tokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String tokens() throws JsonGenerationException, JsonMappingException, IOException {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.tokens());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String start(@QueryParam("pin") String pin) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.start(pin));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/certs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String certs() {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.certs());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/sign")
    @Produces(MediaType.APPLICATION_JSON)
    public String sign(String body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaPdfDto objetoDto = om.readValue(body, FirmaPdfDto.class);
            return om.writeValueAsString(servicio.sign(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/finish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String finish() {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.finish());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/estadoToken")
    @Produces(MediaType.TEXT_HTML)
    public String estadoToken() {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            return om.writeValueAsString(servicio.estadoToken());
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/matarProceso")
    @Produces(MediaType.TEXT_HTML)
    public String matarProceso() {
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.matarProceso();
    }

    @GET
    @Path("/reset")
    @Produces(MediaType.TEXT_HTML)
    public String reset() {
        FirmadorServicio servicio = new FirmadorServicio();
        return servicio.reset();
    }
}
