package bo.firmadigital.jacobitus4.jetty.localhost9000;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.ObjectMapper;

import bo.firmadigital.jacobitus4.jetty.JettyHelper;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaHashDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaJsonDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaLotePdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPdfDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaPkcs7Dto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.FirmaXmlDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.servicios.FirmadorServicio;

@Path("/token")
public class FirmadorRest {
    @POST
    @Path("/firmar_json")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarJson(String body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaJsonDto objetoDto = om.readValue(body, FirmaJsonDto.class);
            return om.writeValueAsString(servicio.firmarJson(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/firmar_pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarPdf(InputStream body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaPdfDto objetoDto = om.readValue(body, FirmaPdfDto.class);
            return om.writeValueAsString(servicio.firmarPdf(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/firmar_hash")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarHash(String body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaHashDto objetoDto = om.readValue(body, FirmaHashDto.class);
            return om.writeValueAsString(servicio.firmarHash(objetoDto));
        } catch (IOException e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/firmar_pkcs7")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarPKCS7(InputStream body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaPkcs7Dto objetoDto = om.readValue(body, FirmaPkcs7Dto.class);
            return om.writeValueAsString(servicio.firmarPkcs7(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/firmar_xml")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarXml(InputStream body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaXmlDto objetoDto = om.readValue(body, FirmaXmlDto.class);
            return om.writeValueAsString(servicio.firmarXml(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/firmar_lote_pdfs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarLotePdf(String body) {
        FirmadorServicio servicio = new FirmadorServicio();
        ObjectMapper om = new ObjectMapper();
        try {
            FirmaLotePdfDto objetoDto = om.readValue(body, FirmaLotePdfDto.class);
            return om.writeValueAsString(servicio.firmarLotePdfs(objetoDto));
        } catch (Exception e) {
            return JettyHelper.generarRespuesta(e.getMessage());
        }
    }

    @POST
    @Path("/sign")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Deprecated(forRemoval=true)
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

    @POST
    @Path("/firmar_modo_seguro")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String firmarModoSeguro(String body) {
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
