package bo.firmadigital.jacobitus.escritorio.formularios;

import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormContrasenaNueva extends Stage {
    private Stage stage;
    private String pass;

    public String getPass() {
        return pass;
    }
    
    public FormContrasenaNueva(Stage parent) {
        stage = parent;
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
        passwordField2.setPromptText("Repita su contraseña");
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                passwordField2.requestFocus();
            }
        });
        passwordField2.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                establecerContrasenia(passwordField.getText(), passwordField2.getText());
                close();
            }
        });
        root.getChildren().add(passwordField2);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.setOnAction(t -> {
            establecerContrasenia(passwordField.getText(), passwordField2.getText());
            close();
        });
        Button buttonCancelar = new Button("Cancelar");
        buttonCancelar.setOnAction(t -> {
            close();
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.getChildren().add(hBox);
        Scene scene = new Scene(root, 300, 165);
        setScene(scene);
    }

    private void establecerContrasenia(String contrasenia1, String contrasenia2) {
        stage.getScene().setCursor(Cursor.WAIT);
        String error;
        if (contrasenia1.equals(contrasenia2)) {
            if (contrasenia1.length() < 8) {
                error = "La contraseña es muy corta.";
            } else {
                int num = 0, may = 0, minu = 0;
                char[] password = contrasenia1.toCharArray();
                for (int i = 0; i < contrasenia1.length(); i++) {
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
                    stage.getScene().setCursor(Cursor.DEFAULT);
                    pass = contrasenia1;
                    return;
                }
            }
        } else {
            error = "Las contraseñas no coinciden.";
        }
        contrasenia1 = null;
        stage.getScene().setCursor(Cursor.DEFAULT);
        Alert alert = new Alert(Alert.AlertType.ERROR, error, ButtonType.OK);
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Jukubitus");
        alert.showAndWait();
    }
}
