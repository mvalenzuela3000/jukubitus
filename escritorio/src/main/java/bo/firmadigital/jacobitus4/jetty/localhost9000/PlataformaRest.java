package bo.firmadigital.jacobitus4.jetty.localhost9000;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenAutenticacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenChangePinDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCreacionCsrDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenCreacionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenPemDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.TokenSolicitudDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.PlataformaServicio;

@Path("/token")
public class PlataformaRest {
    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String create(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenCreacionDto objetoDto = om.readValue(body, TokenCreacionDto.class);
            return om.writeValueAsString(servicio.create(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @GET
    @Path("/generate_keypar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_keypar(@QueryParam("pin") String pin, @QueryParam("slot") Integer slotNumber) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = new TokenAutenticacionDto();
            objetoDto.setPin(pin);
            objetoDto.setSlot(Long.parseLong(slotNumber.toString()));
            return om.writeValueAsString(servicio.generateKeypar(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/generate_csr")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String generate_csr(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenCreacionCsrDto objetoDto = om.readValue(body, TokenCreacionCsrDto.class);
            return om.writeValueAsString(servicio.generateCsr(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cargar_pem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cargar_pem(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenPemDto objetoDto = om.readValue(body, TokenPemDto.class);
            return om.writeValueAsString(servicio.cargarPem(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cambiar_pin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cambiar_pin(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenChangePinDto objetoDto = om.readValue(body, TokenChangePinDto.class);
            return om.writeValueAsString(servicio.cambiarPin(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/cambiar_pin_so")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String cambiar_pin_so(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenChangePinDto objetoDto = om.readValue(body, TokenChangePinDto.class);
            return om.writeValueAsString(servicio.cambiarPinSO(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/test_pin_so")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String test_pin_so(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenAutenticacionDto objetoDto = om.readValue(body, TokenAutenticacionDto.class);
            return om.writeValueAsString(servicio.testPinSO(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
    
    @POST
    @Path("/firmar_solicitudes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarSolicitudes(String body) {
        PlataformaServicio servicio = new PlataformaServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            TokenSolicitudDto objetoDto = om.readValue(body, TokenSolicitudDto.class);
            return om.writeValueAsString(servicio.firmarSolicitudes(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
