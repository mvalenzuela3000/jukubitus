/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import bo.firmadigital.jacobitus.firmador.Constants;
import bo.firmadigital.jacobitus.firmador.base.Opciones;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.utilidades.Certificate;
import bo.firmadigital.jacobitus.validador.comun.DatosCertificado;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.util.Config;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
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
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ADSIB
 */
@SuppressWarnings("rawtypes")
public class TokenInfo extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final ContextMenu contextMenu;
    private final Button buttonClave;
    private final long slot;
    private String pass = null;
    private String label;

    private Opciones getOpciones() {
        Config config = Config.getInstance();
        Opciones opciones = new Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setDirectorioControladores(config.getDirectorioControladores());
        opciones.setDispositivosCompatibles(config.getDispositivosCompatibles());
        opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        opciones.setApiSelloTiempo(config.getTS());
        opciones.setJwtSelloTiempo(config.getTSJWT());
        opciones.setHsmHabilitado(config.isHsmEnabled());
        opciones.setTipoHsm(config.getHsmType());
        opciones.setApiHsm(config.getHsmCloud());
        opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }
    
    @SuppressWarnings("unchecked")
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
        MenuItem exportarCertItem = new MenuItem("Exportar Certificado");
        exportarCertItem.setOnAction((ActionEvent e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccione directorio de destino");
            File destino = directoryChooser.showDialog(parent);
            if (destino != null) {
                new Thread(exportarCertificado(destino)).start();
            }
        });
        MenuItem exportarClaveItem = new MenuItem("Exportar Clave");
        exportarClaveItem.setOnAction((ActionEvent e) -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Esta acción dejará vulnerable su clave privada.\nHágalo solo si conoce los riesgos.");
            alert.setContentText("¿Desea continuar?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Seleccione directorio de destino");
                File destino = directoryChooser.showDialog(parent);
                if (destino != null) {
                    new Thread(exportarClave(destino)).start();
                }
            }
        });
        MenuItem deleteItem = new MenuItem("Borrar Clave");
        deleteItem.setOnAction((ActionEvent e) -> {
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Advertencia");
            alert.setHeaderText("Esta acción eliminará la clave y el certificado de forma permanente.");
            alert.setContentText("¿Desea continuar?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            if (alert.showAndWait().get() == ButtonType.YES) {
                new Thread(borrarClave()).start();
            }
        });
        if (slot == -1l) {
            contextMenu.getItems().addAll(certItem, exportarCertItem, exportarClaveItem, deleteItem);
        } else {
            contextMenu.getItems().addAll(certItem, exportarCertItem, deleteItem);
        }

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
                    if (datos != null) {
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
        buttonClave.setOnAction(t -> {
            buttonClave.setDisable(true);
            new Thread(crearClave()).start();
        });
        root.setBottom(buttonClave);
        Scene scene = new Scene(root, 560, 260);
        setScene(scene);

        setOnShown((WindowEvent t) -> {
            Contrasena contrasena = new Contrasena(TokenInfo.this, Constants.INFO);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                pass = contrasena.getPass();
                new Thread(listarCertificados(pass)).start();
            }
        });
    }

    public Task<Boolean> listarCertificados(String pass) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
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
                } catch (GeneralSecurityException | IOException ex) {
                    if (ex.getCause() instanceof IOException) {
                        if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                            throw new RuntimeException("Pin incorrecto, intente nuevamente.");
                        }
                    }
                    if (ex instanceof java.security.cert.CertificateExpiredException) {
                        throw new RuntimeException("El certificado se encuentra expirado.");
                    }
                    if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                        throw new RuntimeException("El certificado aún no está vigente.");
                    }
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
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            Contrasena contrasena = new Contrasena(TokenInfo.this, Constants.INFO);
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

    public Task<Boolean> crearClave() {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    BigInteger max = new BigInteger("1000000000000");
                    BigInteger id = new BigInteger(max.bitLength(), new SecureRandom()).mod(max);
                    token.generarClaves(id.toString(), pass, (int)slot);
                    token.salir();
                    return true;
                } catch (GeneralSecurityException | IOException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded((WorkerStateEvent evt) -> {
            buttonClave.setDisable(false);
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            buttonClave.setDisable(false);
        });
        return task;
    }

    public Task<Boolean> cargarCertificado(File file) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    String pem;
                    try (FileInputStream is = new FileInputStream(file)) {
                        byte[] cert = is.readAllBytes();
                        pem = Certificate.getPem(cert);
                    }
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
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
        task.setOnSucceeded((WorkerStateEvent evt) -> {
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        return task;
    }

    public Task<Boolean> exportarCertificado(File destino) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    try {
                        DatosCertificado cert = new DatosCertificado(token.obtenerCertificado(label));
                        String pem = Certificate.getPem(cert.getCert().getEncoded());
                        File file = new File(destino, "certificado_" + cert.getNombreComunSubject().replace(" ", "_") + ".pem");
                        try (FileOutputStream os = new FileOutputStream(file)) {
                            os.write(pem.getBytes());
                            os.flush();
                        }
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
        task.setOnSucceeded((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.INFORMATION, "El certificado se exportó correctamente.");
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        return task;
    }

    public Task<Boolean> exportarClave(File destino) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    try {
                        DatosCertificado cert = new DatosCertificado(token.obtenerCertificado(label));
                        PrivateKey pk = token.obtenerClavePrivada(label);
                        File file = new File(destino, "clave_" + cert.getNombreComunSubject().replace(" ", "_") + ".pem");
                        try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                            pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", pk.getEncoded()));
                        }
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
        task.setOnSucceeded((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.INFORMATION, "La clave se exportó correctamente.");
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        return task;
    }

    public Task<Boolean> borrarClave() {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpciones());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    token.eliminarClaves(label);
                    token.salir();
                    return true;
                } catch (GeneralSecurityException | IOException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded((WorkerStateEvent evt) -> {
            new Thread(listarCertificados(pass)).start();
        });
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING, err);
            alert.initOwner(this);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        });
        return task;
    }
}
