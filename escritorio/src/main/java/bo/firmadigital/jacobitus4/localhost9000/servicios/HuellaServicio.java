package bo.firmadigital.jacobitus4.localhost9000.servicios;

import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus4.localhost9000.dtos.HuellaDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.comun.RespuestaDto;
import bo.firmadigital.utiles.fingerprint.Capturar;

public class HuellaServicio {

    public HuellaServicio() {
    }

    public RespuestaDto<HuellaDto> capturar() {
        RespuestaDto<HuellaDto> respuesta = new RespuestaDto<HuellaDto>();
        try {
            if (OS.isWindows()) {
                Capturar.capturar((byte[] image) -> {
                    HuellaDto datos = new HuellaDto();
                    datos.setImage(Base64.getEncoder().encodeToString(image));
                    datos.setWsq(Base64.getEncoder().encodeToString(Capturar.toWSQ(image)));

                    respuesta.setDatos(datos);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Huella capturada");
                });
            } else {
                Capturar.capturarLinux((byte[] image) -> {
                    HuellaDto datos = new HuellaDto();
                    datos.setImage(Base64.getEncoder().encodeToString(image));
                    datos.setWsq(Base64.getEncoder().encodeToString(Capturar.toWSQ(image)));

                    respuesta.setDatos(datos);
                    respuesta.setFinalizado(true);
                    respuesta.setMensaje("Huella capturada");
                });
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(HuellaServicio.class.getName()).log(Level.SEVERE, null, ex);
            respuesta.setFinalizado(false);
            respuesta.setMensaje(ex.getMessage());
        }
        return respuesta;
    }
}
