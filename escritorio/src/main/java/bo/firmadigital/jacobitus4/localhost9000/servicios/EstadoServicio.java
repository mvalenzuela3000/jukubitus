package bo.firmadigital.jacobitus4.localhost9000.servicios;

import java.util.logging.Level;
import java.util.logging.Logger;

import bo.firmadigital.jacobitus4.Constantes;
import bo.firmadigital.jacobitus4.localhost9000.dtos.VersionDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.comun.RespuestaDto;

public class EstadoServicio {

    public EstadoServicio() {
    }

    public RespuestaDto<VersionDto> status() {
        RespuestaDto<VersionDto> respuesta = new RespuestaDto<VersionDto>();
        try {
            VersionDto datos = new VersionDto();
            datos.setCompilacion(3100);
            datos.setApi_version(Constantes.VERSION);

            respuesta.setDatos(datos);
            respuesta.setFinalizado(true);
            respuesta.setMensaje("Servicio ejecutandose correctamente");
        } catch (Exception ex) {
            Logger.getLogger(EstadoServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return respuesta;
    }
}
