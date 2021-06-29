/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Token;
import bo.firmadigital.validar.Certificate;
import bo.firmadigital.validar.DatosCertificado;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ADSIB
 */
public class TokenInfo extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final ContextMenu contextMenu;
    private final Button buttonClave;
    private final long slot;
    private String pass;
    private String label;

    public TokenInfo(Stage parent, long slot) {
        this.slot = slot;
        setTitle("Claves contenidas en el Token - " + slot);
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(root.widthProperty());
        root.setTop(progressBar);

        contextMenu = new ContextMenu();
        MenuItem certItem = new MenuItem("Cargar Certificado");
        certItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Certificado");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Certificados", "*.pem", "*.crt");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(parent);
            if (file != null) {
                new Thread(cargarCertificado(file)).start();
            }
        });
        MenuItem deleteItem = new MenuItem("Borrar clave");
        deleteItem.setOnAction((ActionEvent e) -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Esta acción eliminará la clave y el certificado de forma permanente.");
            alert.setContentText("¿Desea continuar?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                new Thread(borrarClave()).start();
            }
        });
        contextMenu.getItems().addAll(certItem, deleteItem);

        table = new TableView();
        TableColumn tokenCol = new TableColumn("Etiqueta clave");
        tokenCol.setCellValueFactory(new PropertyValueFactory("label"));
        TableColumn nombreCol = new TableColumn("Certificado");
        nombreCol.setCellValueFactory(new PropertyValueFactory("nombreComunSubject"));
        TableColumn descCol = new TableColumn("Descripcion");
        descCol.setCellValueFactory(new PropertyValueFactory("descripcionSubject"));
        table.getColumns().setAll(tokenCol, nombreCol, descCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setRowFactory(tv -> {
            TableRow<DatosCertificado> row = new TableRow<DatosCertificado>() {
                @Override
                public void updateItem(DatosCertificado datos, boolean empty) {
                    super.updateItem(datos, empty);
                    if (datos != null && datos.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB")) {
                        CertInformation pane = new CertInformation(datos);
                        Tooltip tooltip = new Tooltip();
                        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        tooltip.setGraphic(pane);
                        setTooltip(tooltip);
                        setTooltip(tooltip);
                    } else {
                        setTooltip(null);
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    label = row.getItem().getLabel();
                    contextMenu.show(table, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
        root.setCenter(table);
        buttonClave = new Button("Agregar Clave");
        buttonClave.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
            buttonClave.setDisable(true);
            new Thread(crearClave()).start();
        });
        root.setBottom(buttonClave);
        Scene scene = new Scene(root, 560, 260);
        setScene(scene);

        setOnShown((WindowEvent t) -> {
            Contrasena contrasena = new Contrasena(TokenInfo.this, false);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                pass = contrasena.getPass();
                new Thread(listarCertificados(pass)).start();
            }
        });
    }

    public Task listarCertificados(String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    List<String> labels = token.listarIdentificadorClaves();
                    List<DatosCertificado> certificados = new LinkedList<>();
                    for (String label : labels) {
                        DatosCertificado entry = new DatosCertificado(label, token.obtenerCertificado(label));
                        certificados.add(entry);
                    }
                    token.salir();
                    table.setItems(FXCollections.observableList(certificados));
                    updateProgress(100, 100);
                    return true;
                } catch (GeneralSecurityException ex) {
                    if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                        if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                            throw new RuntimeException("Por favor verifique el pin.");
                        }
                    }
                    if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                        if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                            throw new RuntimeException("El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.");
                        }
                    }
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.showAndWait();
            Contrasena contrasena = new Contrasena(TokenInfo.this, false);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                this.pass = contrasena.getPass();
                new Thread(listarCertificados(this.pass)).start();
            }
        });
        return task;
    }

    public Task crearClave() {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    BigInteger max = new BigInteger("1000000000000");
                    BigInteger id = new BigInteger(max.bitLength(), new SecureRandom()).mod(max);
                    token.generarClaves(id.toString(), pass, (int)slot);
                    token.salir();
                    return true;
                } catch (GeneralSecurityException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded((Event evt) -> {
            buttonClave.setDisable(false);
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((Event evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.showAndWait();
            buttonClave.setDisable(false);
        });
        return task;
    }

    public Task cargarCertificado(File file) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    String pem;
                    try (FileInputStream is = new FileInputStream(file)) {
                        byte[] cert = is.readAllBytes();
                        pem = Certificate.getPem(cert);
                    }
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    try {
                        token.cargarCertificado(pem, label);
                    } catch (GeneralSecurityException ex) {
                        token.salir();
                        throw new RuntimeException(ex.getMessage());
                    }
                    token.salir();
                    return true;
                } catch (IOException | GeneralSecurityException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded((Event evt) -> {
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((Event evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.showAndWait();
        });
        return task;
    }

    public Task borrarClave() {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    token.eliminarClaves(label);
                    token.salir();
                    return true;
                } catch (GeneralSecurityException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded((Event evt) -> {
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((Event evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.showAndWait();
        });
        return task;
    }
}
