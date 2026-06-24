package bo.firmadigital.jacobitus.escritorio;

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

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.utilidades.CertificadoHelper;
import bo.firmadigital.jacobitus.escritorio.components.CertInformation;
import bo.firmadigital.jacobitus.escritorio.comun.Constants;
import bo.firmadigital.jacobitus.escritorio.util.Config;
import bo.firmadigital.jacobitus.escritorio.util.TableViewHelper;
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

@SuppressWarnings("rawtypes")
public class FormTokenInfo extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final ContextMenu contextMenu;
    private final Button buttonClave;
    private final long slot;
    private String pass = null;
    private String label;

    private ConfiguracionFirmador getConfigFirmador() {
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
    
    @SuppressWarnings("unchecked")
    public FormTokenInfo(Stage parent, long slot) {
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
        TableColumn tokenCol = new TableColumn("Alias");
        tokenCol.setCellValueFactory(new PropertyValueFactory("alias"));
        TableColumn nombreCol = new TableColumn("Titular");
        nombreCol.setCellValueFactory(new PropertyValueFactory("nombreComunSujeto"));
        TableColumn inicioValidezCol = new TableColumn("Inicio validez");
        inicioValidezCol.setCellValueFactory(new PropertyValueFactory("inicioValidezDMA"));
        TableColumn finValidezCol = new TableColumn("Fin validez");
        finValidezCol.setCellValueFactory(new PropertyValueFactory("finValidezDMA"));
        TableColumn descCol = new TableColumn("Descripción");
        descCol.setCellValueFactory(new PropertyValueFactory("descripcionSujeto"));
        table.getColumns().setAll(tokenCol, nombreCol, inicioValidezCol, finValidezCol, descCol);
        table.setRowFactory(tv -> {
            TableRow<InfoCertificado> row = new TableRow<InfoCertificado>() {
                @Override
                public void updateItem(InfoCertificado infoCertificado, boolean empty) {
                    super.updateItem(infoCertificado, empty);
                    if (infoCertificado != null) {
                        CertInformation pane = new CertInformation(infoCertificado, true);
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
                    label = row.getItem().getAlias();
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
            FormContrasena contrasena = new FormContrasena(FormTokenInfo.this, Constants.INFO);
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
                    gestorSlot.setConfigFirmador(getConfigFirmador());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    List<String> listaAlias = token.listarIdentificadorClaves();
                    List<InfoCertificado> listaInfoCertificado = new LinkedList<>();
                    for (String alias : listaAlias) {
                        InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                        listaInfoCertificado.add(infoCertificado);
                    }
                    token.salir();
                    table.setItems(FXCollections.observableList(listaInfoCertificado));
                    TableViewHelper.ajustarColumnas(table);
                    updateProgress(100, 100);
                    return true;
                } catch (GeneralSecurityException | IOException ex) {
                    if (ex.getCause() instanceof IOException) {
                        if (ex.getCause().getMessage().equals("PKCS12 key store mac invalid - wrong password or corrupted file.")) {
                            throw new JacobitusException("Pin incorrecto, intente nuevamente.");
                        }
                    }
                    if (ex instanceof java.security.cert.CertificateExpiredException) {
                        throw new JacobitusException("El certificado se encuentra expirado.");
                    }
                    if (ex instanceof java.security.cert.CertificateNotYetValidException) {
                        throw new JacobitusException("El certificado aún no está vigente.");
                    }
                    if (ex.getCause() instanceof java.security.UnrecoverableKeyException) {
                        if (ex.getCause().getCause() instanceof javax.security.auth.login.FailedLoginException) {
                            throw new JacobitusException("Por favor verifique el pin.");
                        }
                    }
                    if (ex.getCause() instanceof javax.security.auth.login.LoginException) {
                        if (ex.getCause().getCause().getMessage().equals("CKR_PIN_LOCKED")) {
                            throw new JacobitusException("El token criptográfico se encuentra bloqueado por demasiados intentos fallidos al ingresar el PIN.");
                        }
                    }
                    throw new JacobitusException(ex.getMessage());
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
            FormContrasena contrasena = new FormContrasena(FormTokenInfo.this, Constants.INFO);
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
                    gestorSlot.setConfigFirmador(getConfigFirmador());
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
                    throw new JacobitusException(ex.getMessage());
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
                        pem = CertificadoHelper.obtenerPem(cert);
                    }
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setConfigFirmador(getConfigFirmador());
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
                        throw new JacobitusException(ex.getMessage());
                    }
                    token.salir();
                    return true;
                } catch (IOException | GeneralSecurityException ex) {
                    throw new JacobitusException(ex.getMessage());
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
                    gestorSlot.setConfigFirmador(getConfigFirmador());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    try {
                        InfoCertificado infoCertificado = new InfoCertificado(token.obtenerCertificado(label));
                        String pem = CertificadoHelper.obtenerPem(infoCertificado.getX509certificado().getEncoded());
                        File file = new File(destino, "certificado_" + infoCertificado.getInfoSujeto().getNombreComun().replace(" ", "_") + ".pem");
                        try (FileOutputStream os = new FileOutputStream(file)) {
                            os.write(pem.getBytes());
                            os.flush();
                        }
                    } catch (GeneralSecurityException ex) {
                        token.salir();
                        throw new JacobitusException(ex.getMessage());
                    }
                    token.salir();
                    return true;
                } catch (IOException | GeneralSecurityException ex) {
                    throw new JacobitusException(ex.getMessage());
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
                    gestorSlot.setConfigFirmador(getConfigFirmador());
                    Slot oSlot = gestorSlot.obtenerSlot(slot);
                    if (oSlot == null) {
                        throw new IOException("El slot " + slot + " no se encuentra disponible.");
                    }
                    IToken token = oSlot.getToken();
                    token.iniciar(pass);
                    try {
                        InfoCertificado infoCertificado = new InfoCertificado(token.obtenerCertificado(label));
                        PrivateKey pk = token.obtenerClavePrivada(label);
                        File file = new File(destino, "clave_" + infoCertificado.getInfoSujeto().getNombreComun().replace(" ", "_") + ".pem");
                        try (PemWriter pemWriter = new PemWriter(new OutputStreamWriter(new FileOutputStream(file)))) {
                            pemWriter.writeObject(new PemObject("RSA PRIVATE KEY", pk.getEncoded()));
                        }
                    } catch (GeneralSecurityException ex) {
                        token.salir();
                        throw new JacobitusException(ex.getMessage());
                    }
                    token.salir();
                    return true;
                } catch (IOException | GeneralSecurityException ex) {
                    throw new JacobitusException(ex.getMessage());
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
                    gestorSlot.setConfigFirmador(getConfigFirmador());
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
                    throw new JacobitusException(ex.getMessage());
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
