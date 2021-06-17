/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.token.Slot;
import bo.firmadigital.token.TokenPKCS12;
import java.security.GeneralSecurityException;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class Configuracion extends Stage {
    private Config config;
    private CheckBox checkBox;
    private TextField textFieldIP;
    private TextField textFieldPort;
    private TextField textFieldToken;

    public Configuracion(Stage parent) {
        setTitle("Panel de configuración");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        config = new Config();
        HBox root = new HBox();
        VBox vbox1 = new VBox();
        vbox1.setPadding(new Insets(10));
        vbox1.setSpacing(8);
        Label titleP = new Label("Opciones de proxy");
        titleP.setStyle("-fx-font-weight: bold");
        vbox1.getChildren().add(titleP);
        checkBox = new CheckBox("Utilizar proxy");
        checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            textFieldIP.setDisable(!newValue);
            textFieldPort.setDisable(!newValue);
            if (newValue == false && config.isProxyEnabled() == false) {
                textFieldIP.setText(config.getProxyIP());
                textFieldPort.setText(config.getProxyPort());
            }
        });
        vbox1.getChildren().add(checkBox);
        Label labelIP = new Label("Introduzca la IP del proxy:");
        vbox1.getChildren().add(labelIP);
        textFieldIP = new TextField();
        vbox1.getChildren().add(textFieldIP);
        Label labelPort = new Label("Introduzca el puerto del proxy:");
        vbox1.getChildren().add(labelPort);
        textFieldPort = new TextField("3128");
        vbox1.getChildren().add(textFieldPort);
        Button buttonGuardar = new Button("Guardar Proxy");
        vbox1.getChildren().add(buttonGuardar);
        root.getChildren().add(vbox1);
        Separator separator = new Separator(Orientation.VERTICAL);
        root.getChildren().add(separator);
        VBox vbox2 = new VBox();
        vbox2.setPadding(new Insets(10));
        vbox2.setSpacing(8);
        Label titleT = new Label("Opciones softoken");
        titleT.setStyle("-fx-font-weight: bold");
        vbox2.getChildren().add(titleT);
        Label labelToken = new Label("Archivo para token/software:");
        vbox2.getChildren().add(labelToken);
        textFieldToken = new TextField();
        textFieldToken.setDisable(true);
        vbox2.getChildren().add(textFieldToken);
        Button buttonCrear = new Button("Crear Token");
        vbox2.getChildren().add(buttonCrear);
        root.getChildren().add(vbox2);
        Scene scene = new Scene(root, 440, 215);
        setScene(scene);
        checkBox.setSelected(true);
        checkBox.setSelected(config.isProxyEnabled());
        textFieldIP.setText(config.getProxyIP());
        textFieldPort.setText(config.getProxyPort());
        buttonGuardar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            config.setProxyEnabled(checkBox.isSelected());
            config.setProxyIP(textFieldIP.getText());
            config.setProxyPort(textFieldPort.getText());
            config.save();
            close();
        });
        if (config.getToken() == null) {
            textFieldToken.setText("Ninguno");
        } else {
            textFieldToken.setText(config.getToken().getName());
            buttonCrear.setDisable(true);
        }
        buttonCrear.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            ContrasenaNueva contrasena = new ContrasenaNueva(parent);
            contrasena.showAndWait();
            if (contrasena.getPass() != null) {
                Slot slot = new Slot(config.getTokenToCreate());
                TokenPKCS12 token = new TokenPKCS12(slot);
                try {
                    token.crear(contrasena.getPass());
                    textFieldToken.setText(config.getToken().getName());
                } catch (GeneralSecurityException ex) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                }
            }
        });
    }
}
