package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus.escritorio.jetty.JettyHelper;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.ValidacionArchivoDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.ValidacionPdfDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.servicios.ValidadorServicio;

@Path("/")
public class ValidadorRest {
    @POST
    @Path("/validar_pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarPdf(InputStream body) {
        ValidadorServicio servicio = new ValidadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            ValidacionPdfDto objetoDto = om.readValue(body, ValidacionPdfDto.class);
            return om.writeValueAsString(servicio.validarPdf(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/validar_pkcs7")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarPKCS7(InputStream body) {
        ValidadorServicio servicio = new ValidadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            ValidacionArchivoDto objetoDto = om.readValue(body, ValidacionArchivoDto.class);
            return om.writeValueAsString(servicio.validarPkcs7(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/validar_xml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarXml(InputStream body) {
        ValidadorServicio servicio = new ValidadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            ValidacionArchivoDto objetoDto = om.readValue(body, ValidacionArchivoDto.class);
            return om.writeValueAsString(servicio.validarXml(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/validar_jws")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String validarJws(InputStream body) {
        ValidadorServicio servicio = new ValidadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            ValidacionArchivoDto objetoDto = om.readValue(body, ValidacionArchivoDto.class);
            return om.writeValueAsString(servicio.validarJws(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }
}
