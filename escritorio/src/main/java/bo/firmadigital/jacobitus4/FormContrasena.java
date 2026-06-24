package bo.firmadigital.jacobitus.escritorio;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormContrasena extends Stage {
    private String pass;
    private boolean bloquea = false;
    private boolean forzarEnveloped = false;
    private boolean usarPrefijo = false;
    private int height = 120;
    private TextField  nodeField;

    public FormContrasena(Stage parent, int tipo) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(4));
        VBox vBoxTop = new VBox();
        vBoxTop.setPadding(new Insets(4));
        vBoxTop.setSpacing(4);
        Label label = new Label("Introduzca su pin:");
        vBoxTop.getChildren().add(label);
        root.setTop(vBoxTop);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(4));
        vBox.setSpacing(4);
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
        switch (tipo) {
            case 0:
                vBox.getChildren().add(new Label("Información"));
                break;
            case 1:
                CheckBox checkBox = new CheckBox("Bloquear documento.");
                vBox.getChildren().add(checkBox);
                checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    bloquea = newValue;
                });
                break;
            case 2:
                vBox.getChildren().add(new Label("PKCS#7"));
                break;
            case 3:
                nodeField = new TextField();
                vBox.getChildren().add(nodeField);
                nodeField.setPromptText("Nodo");
                CheckBox checkBoxEnveloped = new CheckBox("Forzar enveloped.");
                vBox.getChildren().add(checkBoxEnveloped);
                checkBoxEnveloped.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    forzarEnveloped = newValue;
                });
                CheckBox checkBoxPrefix = new CheckBox("Usar prefijo.");
                vBox.getChildren().add(checkBoxPrefix);
                checkBoxPrefix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    usarPrefijo = newValue;
                });
                height = 180;
                break;
        }
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
        Scene scene = new Scene(root, 300, height);
        setScene(scene);

        javafx.application.Platform.runLater(() -> {
            this.sizeToScene();
            this.setWidth(300);
            this.setHeight(height + 60.0);
        });
    }

    public String getPass() {
        return pass;
    }

    public boolean isBloquea() {
        return bloquea;
    }

    public boolean getForzarEnveloped() {
        return forzarEnveloped;
    }

    public boolean getUsarPrefijo() {
        return usarPrefijo;
    }

    public String getNode() {
        if (nodeField == null){
            return null;
        }
        if (nodeField.getText().trim().equals("")) {
            return null;
        }
        return nodeField.getText();
    }
}
