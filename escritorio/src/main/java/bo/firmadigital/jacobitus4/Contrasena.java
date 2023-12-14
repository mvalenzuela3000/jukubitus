/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class Contrasena extends Stage {
    private String pass;
    private boolean bloquea = false;
    private int height = 120;
    private TextField  nodeField;

    public Contrasena(Stage parent, int tipo) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        Label label = new Label("Introduzca su pin:");
        root.setTop(label);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(4));
        vBox.setSpacing(4);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
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
                    bloquea = newValue;
                });
                height = 150;
                break;
        }
        vBox.getChildren().add(new Separator(Orientation.HORIZONTAL));
        root.setCenter(vBox);
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
        hBox.setPadding(new Insets(4));
        hBox.setSpacing(4);
        hBox.getChildren().addAll(buttonAceptar, buttonCancelar);
        root.setBottom(hBox);
        Scene scene = new Scene(root, 300, height);
        setScene(scene);
    }

    public String getPass() {
        return pass;
    }

    public boolean isBloquea() {
        return bloquea;
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
