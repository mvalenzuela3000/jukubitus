package bo.firmadigital.jacobitus4.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.revocacion.EstadoRevocacion;
import bo.firmadigital.jacobitus.revocacion.RevocacionHelper;
import bo.firmadigital.jacobitus.validador.comun.Firma;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class CertInformation extends GridPane {
    public CertInformation(Firma firma) {
        InfoCertificado infoCertificado = firma.getInfoCertificado();
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Label title = new Label("INFORMACIÓN DEL CERTIFICADO");
        GridPane.setHalignment(title, HPos.CENTER);
        this.add(title, 0, 0, 2, 1);

        Label subTitle1 = new Label("Titular");
        GridPane.setHalignment(subTitle1, HPos.CENTER);
        this.add(subTitle1, 0, 1, 2, 1);

        Label label0 = new Label("CI");
        this.add(label0, 0, 2, 1, 1);
        Label data0;
        if (infoCertificado.getInfoSujeto().getComplemento() != null && !infoCertificado.getInfoSujeto().getComplemento().equals("")) {
            data0 = new Label(infoCertificado.getInfoSujeto().getNumeroDocumento() + "-" + infoCertificado.getInfoSujeto().getComplemento());
        } else {
            data0 = new Label(infoCertificado.getInfoSujeto().getNumeroDocumento());
        }
        this.add(data0, 1, 2, 1, 1);

        Label label1 = new Label("Nombre");
        this.add(label1, 0, 3, 1, 1);
        Label data1 = new Label(infoCertificado.getInfoSujeto().getNombreComun());
        this.add(data1, 1, 3, 1, 1);

        Label label2 = new Label("Organización ");
        this.add(label2, 0, 4, 1, 1);
        Label data2 = new Label(infoCertificado.getInfoSujeto().getOrganizacion());
        this.add(data2, 1, 4, 1, 1);

        Label label3 = new Label("Unidad");
        this.add(label3, 0, 5, 1, 1);
        Label data3 = new Label(infoCertificado.getInfoSujeto().getUnidadOrganizacional());
        this.add(data3, 1, 5, 1, 1);

        Label label4 = new Label("Cargo");
        this.add(label4, 0, 6, 1, 1);
        Label data4 = new Label(infoCertificado.getInfoSujeto().getCargo());
        this.add(data4, 1, 6, 1, 1);

        Label label5 = new Label("Correo");
        this.add(label5, 0, 7, 1, 1);
        Label data5 = new Label(infoCertificado.getInfoSujeto().getCorreoElectronico());
        this.add(data5, 1, 7, 1, 1);

        Label label6 = new Label("Nit");
        this.add(label6, 0, 8, 1, 1);
        Label data6 = new Label(infoCertificado.getInfoSujeto().getNit());
        this.add(data6, 1, 8, 1, 1);

        Label subTitle2 = new Label("Emisor");
        GridPane.setHalignment(subTitle2, HPos.CENTER);
        this.add(subTitle2, 0, 9, 2, 1);

        Label label7 = new Label("Nombre");
        this.add(label7, 0, 10, 1, 1);
        Label data7 = new Label(infoCertificado.getInfoEmisor().getNombreComun());
        this.add(data7, 1, 10, 1, 1);

        Label label8 = new Label("Organización ");
        this.add(label8, 0, 11, 1, 1);
        Label data8 = new Label(infoCertificado.getInfoEmisor().getOrganizacion());
        this.add(data8, 1, 11, 1, 1);

        Label subTitle3 = new Label("Periodo de validez");
        GridPane.setHalignment(subTitle3, HPos.CENTER);
        this.add(subTitle3, 0, 12, 2, 1);

        Label label9 = new Label("Inicio");
        this.add(label9, 0, 13, 1, 1);
        Label data9 = new Label(df.format(infoCertificado.getInicioValidez()));
        this.add(data9, 1, 13, 1, 1);

        Label label10 = new Label("Fin");
        this.add(label10, 0, 14, 1, 1);
        Label data10 = new Label(df.format(infoCertificado.getFinValidez()));
        this.add(data10, 1, 14, 1, 1);

        Label subTitle4 = new Label("Revocado");
        GridPane.setHalignment(subTitle4, HPos.CENTER);
        this.add(subTitle4, 0, 15, 2, 1);

        Label label11 = new Label("Detalle");
        this.add(label11, 0, 16, 1, 1);
        Label data11 = new Label(firma.getRevocacion() != null && firma.getRevocacion().getFecha() != null ? "Revocado el " + df.format(firma.getRevocacion().getFecha()) : firma.getCertNoRevocado() ? "No revocado" : "No se pudo consultar");
        this.add(data11, 1, 16, 1, 1);

        Label subTitle5 = new Label("Usos");
        GridPane.setHalignment(subTitle5, HPos.CENTER);
        this.add(subTitle5, 0, 17, 2, 1);

        this.add(new Label(infoCertificado.getPersona()), 0, 18, 2, 1);
        this.add(new Label(infoCertificado.getAlmacenamiento()), 0, 19, 2, 1);
        this.add(new Label(infoCertificado.getTipoFirma()), 0, 20, 2, 1);
    }

    public CertInformation(InfoCertificado infoCertificado, boolean ocsp) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Label title = new Label("INFORMACIÓN DEL CERTIFICADO");
        GridPane.setHalignment(title, HPos.CENTER);
        this.add(title, 0, 0, 2, 1);

        Label subTitle1 = new Label("Titular");
        GridPane.setHalignment(subTitle1, HPos.CENTER);
        this.add(subTitle1, 0, 1, 2, 1);

        Label label0 = new Label("CI");
        this.add(label0, 0, 2, 1, 1);
        Label data0;
        if (infoCertificado.getInfoSujeto().getComplemento() != null && !infoCertificado.getInfoSujeto().getComplemento().equals("")) {
            data0 = new Label(infoCertificado.getInfoSujeto().getNumeroDocumento() + "-" + infoCertificado.getInfoSujeto().getComplemento());
        } else {
            data0 = new Label(infoCertificado.getInfoSujeto().getNumeroDocumento());
        }
        this.add(data0, 1, 2, 1, 1);

        Label label1 = new Label("Nombre");
        this.add(label1, 0, 3, 1, 1);
        Label data1 = new Label(infoCertificado.getInfoSujeto().getNombreComun());
        this.add(data1, 1, 3, 1, 1);

        Label label2 = new Label("Organización ");
        this.add(label2, 0, 4, 1, 1);
        Label data2 = new Label(infoCertificado.getInfoSujeto().getOrganizacion());
        this.add(data2, 1, 4, 1, 1);

        Label label3 = new Label("Unidad");
        this.add(label3, 0, 5, 1, 1);
        Label data3 = new Label(infoCertificado.getInfoSujeto().getUnidadOrganizacional());
        this.add(data3, 1, 5, 1, 1);

        Label label4 = new Label("Cargo");
        this.add(label4, 0, 6, 1, 1);
        Label data4 = new Label(infoCertificado.getInfoSujeto().getCargo());
        this.add(data4, 1, 6, 1, 1);

        Label label5 = new Label("Correo");
        this.add(label5, 0, 7, 1, 1);
        Label data5 = new Label(infoCertificado.getInfoSujeto().getCorreoElectronico());
        this.add(data5, 1, 7, 1, 1);

        Label label6 = new Label("Nit");
        this.add(label6, 0, 8, 1, 1);
        Label data6 = new Label(infoCertificado.getInfoSujeto().getNit());
        this.add(data6, 1, 8, 1, 1);

        Label subTitle2 = new Label("Emisor");
        GridPane.setHalignment(subTitle2, HPos.CENTER);
        this.add(subTitle2, 0, 9, 2, 1);

        Label label7 = new Label("Nombre");
        this.add(label7, 0, 10, 1, 1);
        Label data7 = new Label(infoCertificado.getInfoEmisor().getNombreComun());
        this.add(data7, 1, 10, 1, 1);

        Label label8 = new Label("Organización ");
        this.add(label8, 0, 11, 1, 1);
        Label data8 = new Label(infoCertificado.getInfoEmisor().getOrganizacion());
        this.add(data8, 1, 11, 1, 1);

        Label subTitle3 = new Label("Periodo de validez");
        GridPane.setHalignment(subTitle3, HPos.CENTER);
        this.add(subTitle3, 0, 12, 2, 1);

        Label label9 = new Label("Inicio");
        this.add(label9, 0, 13, 1, 1);
        Label data9 = new Label(df.format(infoCertificado.getInicioValidez()));
        this.add(data9, 1, 13, 1, 1);

        Label label10 = new Label("Fin");
        this.add(label10, 0, 14, 1, 1);
        Label data10 = new Label(df.format(infoCertificado.getFinValidez()));
        this.add(data10, 1, 14, 1, 1);

        if (ocsp) {
            Label subTitle4 = new Label("Estado revocación");
            GridPane.setHalignment(subTitle4, HPos.CENTER);
            this.add(subTitle4, 0, 15, 2, 1);

            Label label11 = new Label("Detalle");
            this.add(label11, 0, 16, 1, 1);
            // TODO: Recibir configuracion de validacion para obtener configuracion de proxy
            EstadoRevocacion revocacion = RevocacionHelper.verificar(infoCertificado.getX509certificado(), null, new Date());
            Label data11 = new Label(revocacion.getDescripcion());
            this.add(data11, 1, 16, 1, 1);
        }

        Label subTitle5 = new Label("Usos");
        GridPane.setHalignment(subTitle5, HPos.CENTER);
        this.add(subTitle5, 0, 17, 2, 1);

        this.add(new Label(infoCertificado.getPersona()), 0, 18, 2, 1);
        this.add(new Label(infoCertificado.getAlmacenamiento()), 0, 19, 2, 1);
        this.add(new Label(infoCertificado.getTipoFirma()), 0, 20, 2, 1);
    }
}
