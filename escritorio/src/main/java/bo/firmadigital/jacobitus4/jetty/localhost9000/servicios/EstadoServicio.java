package bo.firmadigital.jacobitus4.jetty.localhost9000.servicios;

import java.util.logging.Level;
import java.util.logging.Logger;

import bo.firmadigital.jacobitus4.Informacion;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.VersionDto;
import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.RespuestaDto;

public class EstadoServicio {

    public EstadoServicio() {
    }

    public RespuestaDto<VersionDto> status() {
        RespuestaDto<VersionDto> respuesta = new RespuestaDto<VersionDto>();
        try {
            VersionDto datos = new VersionDto();
            datos.setCompilacion(Informacion.COMPILACION);
            datos.setApi_version(Informacion.VERSION);

            respuesta.setDatos(datos);
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Servicio ejecutandose correctamente");
        } catch (Exception ex) {
            Logger.getLogger(EstadoServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return respuesta;
    }
}
