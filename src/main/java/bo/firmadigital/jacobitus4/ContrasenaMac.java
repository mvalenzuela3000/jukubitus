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
public class ContrasenaMac extends Stage {
    private String pass;

    public ContrasenaMac(Stage parent) {
        setTitle("Contraseña MacOS");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        Label label = new Label("El certificado ssl no se encuentra registrado.\nPara poder registrarlo se requiere la contraseña de\nsu usuario MacOS.\nPor favor introduzca su contraseña:");
        root.setTop(label);
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(4));
        vBox.setSpacing(4);
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
        vBox.getChildren().add(passwordField);
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
        Scene scene = new Scene(root, 300, 145);
        setScene(scene);
    }

    public String getPass() {
        return pass;
    }
}
