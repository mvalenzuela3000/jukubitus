package bo.firmadigital.jacobitus4;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.List;

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.firmador.base.Opciones;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus4.util.TableViewHelper;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
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

@SuppressWarnings("rawtypes")
public class FormFirmante extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final long slot;
    private final int tipo;
    private String label;
    private String pass = null;
    private boolean bloquea;
    private boolean forzarEnveloped;
    private boolean usarPrefijo;
    private String node;

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
    public FormFirmante(Stage parent, long slot, int tipo) {
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
        TableColumn tokenCol = new TableColumn("Alias");
        tokenCol.setCellValueFactory(new PropertyValueFactory("alias"));
        TableColumn nombreCol = new TableColumn("Titular");
        nombreCol.setCellValueFactory(new PropertyValueFactory("nombreComunSujeto"));
        TableColumn descCol = new TableColumn("Descripción");
        descCol.setCellValueFactory(new PropertyValueFactory("descripcionSujeto"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().setAll(tokenCol, nombreCol, descCol);
        table.setRowFactory(tv -> {
            return new TableRow<InfoCertificado>() {
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
        });
        root.setCenter(table);
        Scene scene = new Scene(root, 560, 260);
        setScene(scene);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            label = ((InfoCertificado)newSelection).getAlias();
            close();
        });

        setOnShown((WindowEvent t) -> {
            FormContrasena contrasena = new FormContrasena(FormFirmante.this, tipo);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                pass = contrasena.getPass();
                forzarEnveloped = contrasena.getForzarEnveloped();
                usarPrefijo = contrasena.getUsarPrefijo();
                node = contrasena.getNode();
                new Thread(listarCertificados(pass)).start();
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

    public boolean getForzarEnveloped() {
        return forzarEnveloped;
    }

    public boolean getUsarPrefijo() {
        return usarPrefijo;
    }

    public String getNode() {
        return node;
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
                    List<String> listaAlias = token.listarIdentificadorClaves();
                    List<InfoCertificado> listaInfoCertificado = new LinkedList<>();
                    for (String alias : listaAlias) {
                        InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                        listaInfoCertificado.add(infoCertificado);
                    }
                    token.salir();
                    table.setItems(FXCollections.observableList(listaInfoCertificado));
                    TableViewHelper.ajustarColumnas(table);
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
                } finally {
                    updateProgress(100, 100);
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

            this.pass = null;

            FormContrasena contrasena = new FormContrasena(FormFirmante.this, tipo);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                this.pass = contrasena.getPass();
                this.forzarEnveloped = contrasena.getForzarEnveloped();
                this.usarPrefijo = contrasena.getUsarPrefijo();
                this.node = contrasena.getNode();
                new Thread(listarCertificados(pass)).start();
            }
        });
        return task;
    }
}
