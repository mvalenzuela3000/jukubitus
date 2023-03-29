/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Token;
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
public class CambiarContrasena extends Stage {
    public CambiarContrasena(Stage parent, long slot) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(8);
        Label oldLabel = new Label("Introduzca pin actual:");
        root.getChildren().add(oldLabel);
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Pin actual");
        root.getChildren().add(oldPasswordField);
        Label label = new Label("Introduzca nuevo pin:");
        root.getChildren().add(label);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nuevo pin");
        root.getChildren().add(passwordField);
        Label label2 = new Label("Repita nuevo pin:");
        root.getChildren().add(label2);
        PasswordField passwordField2 = new PasswordField();
        passwordField2.setPromptText("Repita nuevo pin");
        root.getChildren().add(passwordField2);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            String pass = passwordField.getText();
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
                    if (slot == -1 && (num < 1 || may < 1 || minu < 1)) {
                        error = "La contraseña debe contener al menos un número, una letra mayúscula y una letra minúscula.";
                    } else {
                        GestorSlot gestorSlot = GestorSlot.getInstance();
                        Token token = gestorSlot.obtenerSlot(slot).getToken();
                        try {
                            String oldPass = oldPasswordField.getText();
                            if (oldPass.startsWith("@unlock:")) {
                                token.unlockPin(oldPass.split("@unlock:")[1], pass);
                            } else if (oldPass.startsWith("@so:")) {
                                token.modificarPinSo(oldPass.split("@so:")[1], pass);
                            } else {
                                token.modificarPin(oldPass, pass);
                            }
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Se cambió la contraseña.", ButtonType.OK);
                            alert.setTitle("Jacobitus");
                            alert.showAndWait();
                            close();
                            return;
                        } catch (RuntimeException ex) {
                            error = ex.getMessage();
                        }
                    }
                }
            } else {
                error = "Las contraseñas no coinciden.";
            }
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
        Scene scene = new Scene(root, 300, 220);
        setScene(scene);
    }
}
