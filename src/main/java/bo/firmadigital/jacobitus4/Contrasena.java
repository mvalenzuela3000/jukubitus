/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class Contrasena extends Stage {
    private String pass;

    public Contrasena(Stage parent) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        Label label = new Label("Introduzca su pin:");
        root.setTop(label);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
        root.setCenter(passwordField);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            pass = passwordField.getText();
            close();
        });
        Button buttonCancelar = new Button("Cancelar");
        buttonCancelar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            close();
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.setBottom(hBox);
        Scene scene = new Scene(root, 300, 80);
        setScene(scene);
    }

    public String getPass() {
        return pass;
    }
}
