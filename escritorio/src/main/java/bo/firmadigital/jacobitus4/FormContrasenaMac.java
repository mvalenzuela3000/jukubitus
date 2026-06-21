/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class FormContrasenaMac extends Stage {
    private String pass;

    public FormContrasenaMac(Stage parent) {
        setTitle("Seguridad MacOS");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(4));
        vBox.setSpacing(4);
        Label label1 = new Label("Estás realizando cambios en la configuración de\nconfianza para los certificados del sistema.\n");
        Label label2 = new Label("Ingresa tu contraseña para permitir esta acción:");
        vBox.getChildren().add(label1);
        vBox.getChildren().add(label2);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                pass = passwordField.getText();
                close();
            }
        });
        vBox.getChildren().add(passwordField);
        vBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        root.setCenter(vBox);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.setOnAction(t -> {
            pass = passwordField.getText();
            close();
        });
        Button buttonCancelar = new Button("Cancelar");
        buttonCancelar.setOnAction(t -> {
            close();
        });
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(4));
        hBox.setSpacing(4);
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.setBottom(hBox);
        Scene scene = new Scene(root, 300, 145);
        setScene(scene);
    }

    public String getPass() {
        return pass;
    }
}
