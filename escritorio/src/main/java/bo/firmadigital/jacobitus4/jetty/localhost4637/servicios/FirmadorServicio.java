package bo.firmadigital.jacobitus4.jetty.localhost4637.servicios;

import java.util.Arrays;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import com.fasterxml.jackson.databind.ObjectMapper;

import bo.firmadigital.jacobitus.firmador.Opciones;
import bo.firmadigital.jacobitus.firmador.TokenSelected;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus4.App;
import bo.firmadigital.jacobitus4.jetty.localhost4637.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus4.jetty.localhost4637.dtos.FirmaModoSeguroRespuestaDto;
import bo.firmadigital.jacobitus4.jetty.localhost4637.dtos.FirmaPdfItemRespuestaDto;
import bo.firmadigital.jacobitus4.util.Config;

public class FirmadorServicio {

    public FirmadorServicio() {
    }

    private Opciones getOpciones() {
        Config config = Config.getInstance();
        Opciones opciones = new Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setDirectorioControladores(config.getDirectorioControladores());
        opciones.setDispositivosCompatibles(config.getDispositivosCompatibles());
        // opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        // opciones.setApiSelloTiempo(config.getTS());
        // opciones.setJwtSelloTiempo(config.getTSJWT());
        // opciones.setHsmHabilitado(config.isHsmEnabled());
        // opciones.setTipoHsm(config.getHsmType());
        // opciones.setApiHsm(config.getHsmCloud());
        // opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }

    public FirmaModoSeguroRespuestaDto firmarModoSeguro(FirmaModoSeguroDto objetoDto) throws Exception {
        FirmaModoSeguroRespuestaDto respuesta = new FirmaModoSeguroRespuestaDto();
        try {
            ObjectMapper om = new ObjectMapper();
            String ci = objetoDto.getCi();
            String format = objetoDto.getFormat();
            JSONArray archivo = new JSONArray(om.writeValueAsString(objetoDto.getArchivo()));

            boolean software = false;
            if (objetoDto.getSoftware() != null) {
                software = objetoDto.getSoftware();
            }
            GestorSlot gestorSlot = GestorSlot.getInstance();
            gestorSlot.setOpciones(this.getOpciones());
            Slot[] slots = gestorSlot.listarSlots(software);
            if (slots.length != 1) {
                throw new RuntimeException("Por favor conecte solo un token.");
            }
            TokenSelected dt;
            if (ci != null) {
                if (format.equals("jws")) {
                    dt = App.serviceJWS(slots[0], ci, archivo);
                } else {
                    dt = App.service(slots[0], ci, archivo);
                }
            } else {
                if (format.equals("jws")) {
                    dt = App.serviceJWS(slots[0], null, archivo);
                } else {
                    dt = App.service(slots[0], null, archivo);
                }
            }
            if (dt.getAlias() != null && dt.getPin() != null) {
                List<FirmaPdfItemRespuestaDto> lista = Arrays.asList(om.readValue(dt.getFiles().toString(), FirmaPdfItemRespuestaDto[].class));
                respuesta.setMessage("Se firmó correctamente");
                respuesta.setFiles(lista);
                return respuesta;
            } else {
                respuesta.setMessage("Se canceló la firma del documento");
                return respuesta;
            }
        } catch (JSONException | RuntimeException ex) {
            respuesta.setMessage(ex.getMessage());
            return respuesta;
        }
    }
}
