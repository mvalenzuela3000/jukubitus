package bo.firmadigital.jacobitus4.localhost9000.servicios;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus4.localhost9000.dtos.ListaUsbDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.UsbDto;
import bo.firmadigital.jacobitus4.localhost9000.dtos.comun.RespuestaDto;

public class UsbServicio {

    public UsbServicio() {
    }

    public RespuestaDto<ListaUsbDto> serial() {
        RespuestaDto<ListaUsbDto> respuesta = new RespuestaDto<ListaUsbDto>();
        try {
            if (OS.isUnix()) {
                Process p = Runtime.getRuntime().exec("lsblk --nodeps -o name,serial,type,tran");
                List<UsbDto> listaUsb = new ArrayList<UsbDto>();
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String linea = null;
                while ((linea = in.readLine()) != null) {
                    StringTokenizer stoken = new StringTokenizer(linea);
                    if (stoken.countTokens() >= 4 && !linea.replace(" ", "").equals("NAMESERIALTYPETRAN")) {
                        UsbDto usb = new UsbDto();
                        int ind = 0;
                        while (stoken.hasMoreTokens()) {
                            String tk = stoken.nextToken();
                            switch (ind) {
                                case 0:
                                    usb.setName(tk);
                                    break;
                                case 1:
                                    usb.setSerial(tk);
                                    break;
                                case 2:
                                    usb.setType(tk);
                                    break;
                                case 3:
                                    usb.setTran(tk);
                                    break;
                            }
                            ind++;
                        }
                        if (usb.getType().equals("disk") && usb.getTran().equals("usb")) {
                            listaUsb.add(usb);
                        }
                    }
                }
                in.close();

                ListaUsbDto datos = new ListaUsbDto();
                datos.setUsbs(listaUsb);

                respuesta.setDatos(datos);
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Operación finalizada correctamente...");
            } else {
                respuesta.setFinalizado(true);
                respuesta.setMensaje("Sistema operativo no soportado...");
            }
        } catch (Exception ex) {
            Logger.getLogger(EstadoServicio.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return respuesta;
    }
}
