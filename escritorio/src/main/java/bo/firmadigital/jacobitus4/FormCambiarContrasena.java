package bo.firmadigital.jacobitus4;

import com.itextpdf.io.exceptions.IOException;

import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus4.util.Config;
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

public class FormCambiarContrasena extends Stage {
    private Stage stage;

    private ConfiguracionFirmador getOpciones() {
        Config config = Config.getInstance();
        ConfiguracionFirmador configFirmador = new ConfiguracionFirmador();
        configFirmador.setControlador(config.getDriver());
        configFirmador.setSoftoken(config.getToken());
        configFirmador.setDirectorioControladores(config.getDirectorioControladores());
        configFirmador.setDispositivosCompatibles(config.getDispositivosCompatibles());
        configFirmador.setSelloTiempoHabilitado(config.isTSEnabled());
        configFirmador.setApiSelloTiempo(config.getTS());
        configFirmador.setJwtSelloTiempo(config.getTSJWT());
        configFirmador.setHsmHabilitado(config.isHsmEnabled());
        configFirmador.setTipoHsm(config.getHsmType());
        configFirmador.setApiHsm(config.getHsmCloud());
        configFirmador.setJwtHsm(config.getHsmJWT());
        return configFirmador;
    }

    public FormCambiarContrasena(Stage parent, long slot) {
        stage = parent;
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
        oldPasswordField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });
        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                passwordField2.requestFocus();
            }
        });
        passwordField2.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                cambiarContrasenia(slot, oldPasswordField.getText(), passwordField.getText(), passwordField2.getText());
            }
        });
        root.getChildren().add(passwordField2);
        Button buttonAceptar = new Button("Aceptar");
        buttonAceptar.setOnAction(t -> {
            cambiarContrasenia(slot, oldPasswordField.getText(), passwordField.getText(), passwordField2.getText());
        });
        Button buttonCancelar = new Button("Cancelar");
        buttonCancelar.setOnAction(t -> {
            close();
        });
        HBox hBox = new HBox();
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.getChildren().add(hBox);
        Scene scene = new Scene(root, 300, 220);
        setScene(scene);
    }

    private void cambiarContrasenia(long slot, String oldPassword, String pass, String pass2) {
        stage.getScene().setCursor(Cursor.WAIT);
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
                    gestorSlot.setConfigFirmador(this.getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    try {
                        String oldPass = oldPassword;
                        if (oldPass.startsWith("@unlock:")) {
                            token.unlockPin(oldPass.split("@unlock:")[1], pass);
                        } else if (oldPass.startsWith("@so:")) {
                            token.modificarPinSo(oldPass.split("@so:")[1], pass);
                        } else {
                            token.modificarPin(oldPass, pass);
                        }
                        stage.getScene().setCursor(Cursor.WAIT);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Se cambió la contraseña.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
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
        stage.getScene().setCursor(Cursor.WAIT);
        Alert alert = new Alert(Alert.AlertType.ERROR, error, ButtonType.OK);
        alert.initOwner(stage);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Jacobitus");
        alert.showAndWait();
    }
}
