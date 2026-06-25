package bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.servicios;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.escritorio.comun.Config;
import bo.firmadigital.jacobitus.escritorio.comun.TokenSelected;
import bo.firmadigital.jacobitus.escritorio.formularios.FormAplicacion;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.dtos.FirmaModoSeguroDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.dtos.FirmaModoSeguroRespuestaDto;
import bo.firmadigital.jacobitus.escritorio.jetty.localhost4637.dtos.FirmaPdfItemRespuestaDto;

public class FirmadorServicio {

    public FirmadorServicio() {
    }

    private ConfiguracionFirmador getOpciones() {
        Config config = Config.getInstance();
        ConfiguracionFirmador configFirmador = new ConfiguracionFirmador();
        configFirmador.setControlador(config.getDriver());
        configFirmador.setSoftoken(config.getToken());
        configFirmador.setDirectorioControladores(config.getDirectorioControladores());
        configFirmador.setDispositivosCompatibles(config.getDispositivosCompatibles());
        // configFirmador.setSelloTiempoHabilitado(config.isTSEnabled());
        // configFirmador.setApiSelloTiempo(config.getTS());
        // configFirmador.setJwtSelloTiempo(config.getTSJWT());
        // configFirmador.setHsmHabilitado(config.isHsmEnabled());
        // configFirmador.setTipoHsm(config.getHsmType());
        // configFirmador.setApiHsm(config.getHsmCloud());
        // configFirmador.setJwtHsm(config.getHsmJWT());
        return configFirmador;
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
            gestorSlot.setConfigFirmador(this.getOpciones());
            Slot[] slots = gestorSlot.listarSlots(software);
            if (slots.length != 1) {
                throw new JacobitusException("Por favor conecte solo un token.");
            }
            TokenSelected dt;
            if (ci != null) {
                if (format.equals("jws")) {
                    dt = FormAplicacion.serviceJWS(slots[0], ci, archivo);
                } else {
                    dt = FormAplicacion.service(slots[0], ci, archivo);
                }
            } else {
                if (format.equals("jws")) {
                    dt = FormAplicacion.serviceJWS(slots[0], null, archivo);
                } else {
                    dt = FormAplicacion.service(slots[0], null, archivo);
                }
            }
            if (dt.getAlias() != null && dt.getPin() != null) {
                List<FirmaPdfItemRespuestaDto> lista = Arrays.asList(om.readValue(dt.getFiles().toString(), FirmaPdfItemRespuestaDto[].class));
                respuesta.setMessage("Se firmó correctamente");
                respuesta.setFiles(lista);
                return respuesta;
            } else {
                respuesta.setMessage("Se canceló la firma del documento");
                respuesta.setFiles(Collections.emptyList());
                return respuesta;
            }
        } catch (JSONException | RuntimeException ex) {
            respuesta.setMessage("No se pudo firmar el documento");
            respuesta.setError(ex.getMessage());
            return respuesta;
        }
    }
}
