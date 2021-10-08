/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.components;

import bo.firmadigital.validar.CertDate;
import bo.firmadigital.validar.DatosCertificado;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javafx.geometry.HPos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/**
 *
 * @author ADSIB
 */
public class CertInformation extends GridPane {
    public CertInformation(CertDate certDate) {
        DatosCertificado datos = certDate.getDatos();
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
        if (datos.getComplementoSubject() != null && !datos.getComplementoSubject().equals("")) {
            data0 = new Label(datos.getNumeroDocumentoSubject() + "-" + datos.getComplementoSubject());
        } else {
            data0 = new Label(datos.getNumeroDocumentoSubject());
        }
        this.add(data0, 1, 2, 1, 1);

        Label label1 = new Label("Nombre");
        this.add(label1, 0, 3, 1, 1);
        Label data1 = new Label(datos.getNombreComunSubject());
        this.add(data1, 1, 3, 1, 1);

        Label label2 = new Label("Organización ");
        this.add(label2, 0, 4, 1, 1);
        Label data2 = new Label(datos.getOrganizacionSubject());
        this.add(data2, 1, 4, 1, 1);

        Label label3 = new Label("Unidad");
        this.add(label3, 0, 5, 1, 1);
        Label data3 = new Label(datos.getUnidadOrganizacionalSubject());
        this.add(data3, 1, 5, 1, 1);

        Label label4 = new Label("Cargo");
        this.add(label4, 0, 6, 1, 1);
        Label data4 = new Label(datos.getCargoSubject());
        this.add(data4, 1, 6, 1, 1);

        Label label5 = new Label("Correo");
        this.add(label5, 0, 7, 1, 1);
        Label data5 = new Label(datos.getCorreoSubject());
        this.add(data5, 1, 7, 1, 1);

        Label subTitle2 = new Label("Emisor");
        GridPane.setHalignment(subTitle2, HPos.CENTER);
        this.add(subTitle2, 0, 8, 2, 1);

        Label label6 = new Label("Nombre");
        this.add(label6, 0, 9, 1, 1);
        Label data6 = new Label(datos.getNombreComunIssuer());
        this.add(data6, 1, 9, 1, 1);

        Label label7 = new Label("Organización ");
        this.add(label7, 0, 10, 1, 1);
        Label data7 = new Label(datos.getOrganizacionIssuer());
        this.add(data7, 1, 10, 1, 1);

        Label subTitle3 = new Label("Periodo de validez");
        GridPane.setHalignment(subTitle3, HPos.CENTER);
        this.add(subTitle3, 0, 11, 2, 1);

        Label label8 = new Label("Inicio");
        this.add(label8, 0, 12, 1, 1);
        Label data8 = new Label(df.format(datos.getInicioValidez()));
        this.add(data8, 1, 12, 1, 1);

        Label label9 = new Label("Fin");
        this.add(label9, 0, 13, 1, 1);
        Label data9 = new Label(df.format(datos.getFinValidez()));
        this.add(data9, 1, 13, 1, 1);

        Label subTitle4 = new Label("Revocado");
        GridPane.setHalignment(subTitle4, HPos.CENTER);
        this.add(subTitle4, 0, 14, 2, 1);

        Label label10 = new Label("Detalle");
        this.add(label10, 0, 15, 1, 1);
        Label data10 = new Label(certDate.getOCSP().getDate() != null ? "Revocado el " + df.format(certDate.getOCSP().getDate()) : certDate.isOCSP() ? "No revocado" : "No se pudo consultar");
        this.add(data10, 1, 15, 1, 1);

        Label subTitle5 = new Label("Usos");
        GridPane.setHalignment(subTitle5, HPos.CENTER);
        this.add(subTitle5, 0, 16, 2, 1);

        this.add(new Label(datos.getPersona()), 0, 17, 2, 1);
        this.add(new Label(datos.getAlmacenamiento()), 0, 18, 2, 1);
        this.add(new Label(datos.getTipoFirma()), 0, 19, 2, 1);
    }

    public CertInformation(DatosCertificado datos) {
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
        if (datos.getComplementoSubject() != null && !datos.getComplementoSubject().equals("")) {
            data0 = new Label(datos.getNumeroDocumentoSubject() + "-" + datos.getComplementoSubject());
        } else {
            data0 = new Label(datos.getNumeroDocumentoSubject());
        }
        this.add(data0, 1, 2, 1, 1);

        Label label1 = new Label("Nombre");
        this.add(label1, 0, 3, 1, 1);
        Label data1 = new Label(datos.getNombreComunSubject());
        this.add(data1, 1, 3, 1, 1);

        Label label2 = new Label("Organización ");
        this.add(label2, 0, 4, 1, 1);
        Label data2 = new Label(datos.getOrganizacionSubject());
        this.add(data2, 1, 4, 1, 1);

        Label label3 = new Label("Unidad");
        this.add(label3, 0, 5, 1, 1);
        Label data3 = new Label(datos.getUnidadOrganizacionalSubject());
        this.add(data3, 1, 5, 1, 1);

        Label label4 = new Label("Cargo");
        this.add(label4, 0, 6, 1, 1);
        Label data4 = new Label(datos.getCargoSubject());
        this.add(data4, 1, 6, 1, 1);

        Label label5 = new Label("Correo");
        this.add(label5, 0, 7, 1, 1);
        Label data5 = new Label(datos.getCorreoSubject());
        this.add(data5, 1, 7, 1, 1);

        Label subTitle2 = new Label("Emisor");
        GridPane.setHalignment(subTitle2, HPos.CENTER);
        this.add(subTitle2, 0, 8, 2, 1);

        Label label6 = new Label("Nombre");
        this.add(label6, 0, 9, 1, 1);
        Label data6 = new Label(datos.getNombreComunIssuer());
        this.add(data6, 1, 9, 1, 1);

        Label label7 = new Label("Organización ");
        this.add(label7, 0, 10, 1, 1);
        Label data7 = new Label(datos.getOrganizacionIssuer());
        this.add(data7, 1, 10, 1, 1);

        Label subTitle3 = new Label("Periodo de validez");
        GridPane.setHalignment(subTitle3, HPos.CENTER);
        this.add(subTitle3, 0, 11, 2, 1);

        Label label8 = new Label("Inicio");
        this.add(label8, 0, 12, 1, 1);
        Label data8 = new Label(df.format(datos.getInicioValidez()));
        this.add(data8, 1, 12, 1, 1);

        Label label9 = new Label("Fin");
        this.add(label9, 0, 13, 1, 1);
        Label data9 = new Label(df.format(datos.getFinValidez()));
        this.add(data9, 1, 13, 1, 1);

        Label subTitle5 = new Label("Usos");
        GridPane.setHalignment(subTitle5, HPos.CENTER);
        this.add(subTitle5, 0, 16, 2, 1);

        this.add(new Label(datos.getPersona()), 0, 17, 2, 1);
        this.add(new Label(datos.getAlmacenamiento()), 0, 18, 2, 1);
        this.add(new Label(datos.getTipoFirma()), 0, 19, 2, 1);
    }
}
