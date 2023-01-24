/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.firmar.FirmarJws;
import bo.firmadigital.firmar.FirmarPdf;
import bo.firmadigital.firmar.TokenSelected;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Token;
import bo.firmadigital.validar.DatosCertificado;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
public class Service extends Stage {
    private final ObservableList<DatosCertificado> certificates;
    private final TokenSelected tokenSelected;
    private final String format;
    private final PasswordField passwordField;
    private final Button button;
    private final ChoiceBox aliasChoiceBox;
    private final Label estado;
    private final ProgressBar progressBar;
    private final Button buttonFirmar;
    private final Label message;

    public Service(Stage parent, TokenSelected tokenSelected, String format) {
        setTitle("Pin del token");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        tokenSelected.setAlias(null);
        tokenSelected.setPin(null);
        this.tokenSelected = tokenSelected;
        this.format = format;
        GridPane root = new GridPane();
        root.setHgap(5);
        root.setVgap(5);
        root.setPadding(new Insets(5, 5, 5, 5));
        Scene scene = new Scene(root, 300, 175);
        setScene(scene);
        passwordField = new PasswordField();
        passwordField.setPromptText("Su contraseña");
        root.add(passwordField, 0, 0, 1, 1);
        button = new Button("Actualizar");
        AnchorPane anchorPane = new AnchorPane();
        AnchorPane.setTopAnchor(button, 0d);
        AnchorPane.setLeftAnchor(button, 0d);
        AnchorPane.setBottomAnchor(button, 0d);
        AnchorPane.setRightAnchor(button, 0d);
        anchorPane.getChildren().add(button);
        root.add(anchorPane, 1, 0, 1, 1);
        certificates = FXCollections.observableArrayList();
        aliasChoiceBox = new ChoiceBox(certificates);
        aliasChoiceBox.prefWidthProperty().bind(root.widthProperty());
        aliasChoiceBox.setPrefHeight(55);
        root.add(aliasChoiceBox, 0, 1, 2, 1);
        estado = new Label("Archivos: 0 de " + tokenSelected.getFiles().length());
        root.add(estado, 0, 2, 2, 1);
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(root.widthProperty());
        root.add(progressBar, 0, 3, 2, 1);
        buttonFirmar = new Button("Firmar");
        buttonFirmar.setDisable(true);
        buttonFirmar.prefWidthProperty().bind(root.widthProperty());
        root.add(buttonFirmar, 0, 4, 2, 1);
        message = new Label("");
        root.add(message, 0, 5, 2, 1);

        passwordField.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                this.mostrarCertificados();
            }
        });
        button.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            this.mostrarCertificados();
        });
        buttonFirmar.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                this.aplicarFirma();
            }
        });
        buttonFirmar.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            this.aplicarFirma();
        });
        Service window = this;
        this.setOnShown((WindowEvent t) -> {
            window.setAlwaysOnTop(true);
            passwordField.requestFocus();
        });
    }

    public TokenSelected getDatos() {
        return tokenSelected;
    }

    public Task firmar() {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                final JSONArray files = tokenSelected.getFiles();
                if (files.length() == 0) {
                    updateProgress(100, 100);
                } else {
                    Token token = GestorSlot.getInstance().obtenerSlot(tokenSelected.getSlot().getSlotID()).getToken();
                    token.iniciar(tokenSelected.getPin());
                    JSONArray arr = new JSONArray();
                    for (int i = 0; i < files.length(); i++) {
                        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                            String[] base64 = files.getJSONObject(i).getString("base64").split("base64,");
                            byte[] file = Base64.getDecoder().decode(base64.length == 2 ? base64[1] : base64[0]);
                            switch (format) {
                                case "pades":
                                    FirmarPdf.firmar(new ByteArrayInputStream(file), out, false, token, tokenSelected.getAlias());
                                    break;
                                case "jws":
                                    FirmarJws.firmar(new ByteArrayInputStream(file), out, false, token, tokenSelected.getAlias());
                                    break;
                                default:
                                    throw new Exception(String.format("Formato %s no admitido.", format));
                            }
                            JSONObject obj = new JSONObject();
                            obj.put("name", files.getJSONObject(i).getString("name"));
                            obj.put("base64", Base64.getEncoder().encodeToString(out.toByteArray()));
                            arr.put(obj);
                            final int a = i + 1;
                            Platform.runLater(() -> {
                                estado.setText("Archivos: " + a + " de " + files.length());
                            });
                            updateProgress(a, files.length());
                        } catch (RuntimeException ex) {
                            token.salir();
                            throw new CustomException(ex.getMessage());
                        }
                    }
                    tokenSelected.setFiles(arr);
                    token.salir();
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            tokenSelected.setAlias(null);
            tokenSelected.setPin(null);
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            if (task.getException() instanceof CustomException) {
                close();
            }
        });
        task.setOnSucceeded((Event evt) -> {
            close();
        });
        return task;
    }
    
    private void mostrarCertificados() {
        message.setText("");
        Token token = tokenSelected.getSlot().getToken();
        try {
            token.iniciar(passwordField.getText());
            List<String> list = token.listarIdentificadorClaves();
            certificates.clear();
            for (String clave : list) {
                DatosCertificado cert = new DatosCertificado(clave, token.obtenerCertificado(clave));
                String doc = cert.getNumeroDocumentoSubject();
                if (!cert.getComplementoSubject().equals("")) {
                    doc += "-" + cert.getComplementoSubject();
                }
                if (tokenSelected.getCI() == null || tokenSelected.getCI().equals(doc)) {
                    certificates.add(cert);
                }
            }
            token.salir();
            if (certificates.size() > 0) {
                aliasChoiceBox.getSelectionModel().selectFirst();
                buttonFirmar.setDisable(false);
                buttonFirmar.requestFocus();
            } else {
                message.setText("No se encontró ningún certificado para el ci: " + tokenSelected.getCI());
                buttonFirmar.setDisable(true);
            }
        } catch (GeneralSecurityException ex) {
            message.setText(ex.getMessage());
        }
    }
    
    private void aplicarFirma() {
        if (aliasChoiceBox.getValue() instanceof DatosCertificado) {
            tokenSelected.setPin(passwordField.getText());
            tokenSelected.setAlias(((DatosCertificado)aliasChoiceBox.getValue()).getLabel());
            new Thread(firmar()).start();
        }
    }

    private class CustomException extends RuntimeException {
        public CustomException(String message) {
            super(message);
        }
    }
}
