/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Token;
import bo.firmadigital.jacobitus.firmador.Opciones;
import bo.firmadigital.jacobitus.validador.DatosCertificado;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ADSIB
 */
public class Firmante extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final long slot;
    private final int tipo;
    private String label;
    private String pass = null;
    private boolean bloquea;
    private String node;

    public Firmante(Stage parent, long slot, int tipo) {
        this.slot = slot;
        this.tipo = tipo;
        this.label = null;
        setTitle("Seleccione el certificado a utilizar para la firma");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(root.widthProperty());
        root.setTop(progressBar);
        table = new TableView();
        TableColumn tokenCol = new TableColumn("Certificado");
        tokenCol.setCellValueFactory(new PropertyValueFactory("label"));
        TableColumn nombreCol = new TableColumn("Signatario");
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
                        CertInformation pane = new CertInformation(datos, true);
                        Tooltip tooltip = new Tooltip();
                        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        tooltip.setGraphic(pane);
                        setTooltip(tooltip);
                    } else {
                        setTooltip(null);
                    }
                }
            };
            return row;
        });
        root.setCenter(table);
        Scene scene = new Scene(root, 560, 260);
        setScene(scene);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            label = ((DatosCertificado)newSelection).getLabel();
            close();
        });

        setOnShown((WindowEvent t) -> {
            Contrasena contrasena = new Contrasena(Firmante.this, tipo);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                pass = contrasena.getPass();
                bloquea = contrasena.isBloquea();
                node = contrasena.getNode();
                new Thread(listarCertificados(contrasena.getPass())).start();
            }
        });
    }

    public String getLabel() {
        return label;
    }

    public String getPass() {
        return pass;
    }

    public boolean isBloquea() {
        return bloquea;
    }

    public String getNode() {
        return node;
    }

    public Task listarCertificados(String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() {
                Config config = Config.getInstance();
                Opciones opciones = new Opciones();
                opciones.setControlador(config.getDriver());
                opciones.setToken(config.getToken());
                opciones.setSelloTiempoHabilitado(config.isTSEnabled());
                opciones.setApiSelloTiempo(config.getTS());
                opciones.setJwtSelloTiempo(config.getTSJWT());
                opciones.setHsmHabilitado(config.isTSEnabled());
                opciones.setApiHsm(config.getTS());
                opciones.setJwtHsm(config.getTSJWT());

                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot, opciones).getToken();
                    token.iniciar(pass);
                    List<String> labels = token.listarIdentificadorClaves();
                    List<DatosCertificado> certificados = new LinkedList<>();
                    for (String label : labels) {
                        DatosCertificado entry = new DatosCertificado(label, token.obtenerCertificado(label));
                        if (entry.getNombreComunIssuer().equals("Entidad Certificadora Publica ADSIB")) {
                            certificados.add(entry);
                        }
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
            Contrasena contrasena = new Contrasena(Firmante.this, tipo);
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
}
