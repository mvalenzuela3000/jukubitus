/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class ContrasenaNueva extends Stage {
    private String pass;

    public ContrasenaNueva(Stage parent) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(8);
        Label label = new Label("Introduzca nuevo pin:");
        root.getChildren().add(label);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
        root.getChildren().add(passwordField);
        Label label2 = new Label("Repita pin:");
        root.getChildren().add(label2);
        PasswordField passwordField2 = new PasswordField();
        passwordField.setPromptText("Repita su contraseña");
        root.getChildren().add(passwordField2);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            pass = passwordField.getText();
            String pass2 = passwordField2.getText();
            String error;
            if (pass.equals(pass2)) {
                if (pass.length() < 8) {
                    error = "La contraseña es muy corta.";
                } else {
                    int num = 0, may = 0, minu = 0;
                    char[] password = pass.toCharArray();
                    for (int i = 0; i < pass.length(); i++) {
                        if (password[i] >= '0' && password[i] <= '9') {
                            num++;
                        } else if (password[i] >= 'A' && password[i] <= 'Z') {
                            may++;
                        } else if (password[i] >= 'a' && password[i] <= 'z') {
                            minu++;
                        }
                    }
                    if (num < 1 || may < 1 || minu < 1) {
                        error = "La contraseña debe contener al menos un número, una letra mayúscula y una letra minúscula.";
                    } else {
                        close();
                        return;
                    }
                }
            } else {
                error = "Las contraseñas no coinciden.";
            }
            pass = null;
            Alert alert = new Alert(Alert.AlertType.ERROR, error, ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        Button buttonCancelar = new Button("Cancelar");
        buttonCancelar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            close();
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.getChildren().add(hBox);
        Scene scene = new Scene(root, 300, 165);
        setScene(scene);
    }

    public String getPass() {
        return pass;
    }
}
