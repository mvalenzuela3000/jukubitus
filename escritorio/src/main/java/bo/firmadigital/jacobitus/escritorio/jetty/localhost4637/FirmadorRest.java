package bo.firmadigital.jacobitus.escritorio.jetty.localhost4637;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus.escritorio.jetty.JettyHelper;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.servicios.FirmadorServicio;

@Path("/")
public class FirmadorRest {
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String sign(String body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaModoSeguroDto objetoDto = om.readValue(body, FirmaModoSeguroDto.class);
            return om.writeValueAsString(servicio.firmarModoSeguro(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
