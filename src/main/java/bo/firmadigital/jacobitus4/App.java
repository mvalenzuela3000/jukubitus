/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus.firmador.Constants;
import bo.firmadigital.jacobitus.firmador.TokenSelected;
import bo.firmadigital.jacobitus.firmador.FirmadorJws;
import bo.firmadigital.jacobitus.firmador.FirmadorPKCS7;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.FirmadorXml;
import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus4.util.Converter;
import bo.firmadigital.jacobitus4.util.UrlFileName;
import bo.firmadigital.utiles.nss.Chromium;
import bo.firmadigital.utiles.nss.Firefox;
import bo.firmadigital.jacobitus.comun.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.comun.token.GestorSlot;
import bo.firmadigital.jacobitus.comun.token.Slot;
import bo.firmadigital.jacobitus.comun.token.SmartCard;
import bo.firmadigital.jacobitus.validador.Certificate;
import bo.firmadigital.jacobitus.validador.DatosCertificado;
import bo.firmadigital.jacobitus.validador.MagicBytes;
import bo.firmadigital.jacobitus.validador.Opciones;
import bo.firmadigital.jacobitus.validador.Validador;
import bo.firmadigital.jacobitus.validador.ValidadorJws;
import bo.firmadigital.jacobitus.validador.ValidadorPdf;
import bo.firmadigital.jacobitus.validador.ValidadorPKCS7;
import bo.firmadigital.jacobitus.validador.ValidadorXml;
import com.itextpdf.kernel.PdfException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.codehaus.jettison.json.JSONArray;
import bo.firmadigital.jacobitus.firmador.IFirmador;

/**
 *
 * @author ADSIB
 */
public class App extends Application {
    private ProgressBar progressBar;
    private ContextMenu contextMenuToken;
    private ContextMenu contextMenu;
    private MenuItem exportarItem;
    private TableView table;
    private TableView tableFile;
    private File destino;
    private Validador validar;
    private CK_TOKEN_INFO tokenInfo;
    private static boolean servicio;
    private static boolean taskBar;
    private static boolean taskBarEmulated;
    private static String url = null, token, urlPost;
    private static String param = null;
    private static Stage stage;
    private static App app;
    private static final TokenSelected tokenSelected = new TokenSelected();
    public static final String VERSION = "1.1.0";

    private bo.firmadigital.jacobitus.firmador.Opciones getOpcionesFirmador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.firmador.Opciones opciones = new bo.firmadigital.jacobitus.firmador.Opciones();
        opciones.setControlador(config.getDriver());
        opciones.setToken(config.getToken());
        opciones.setSelloTiempoHabilitado(config.isTSEnabled());
        opciones.setApiSelloTiempo(config.getTS());
        opciones.setJwtSelloTiempo(config.getTSJWT());
        opciones.setHsmHabilitado(config.isHsmEnabled());
        opciones.setTipoHsm(config.getHsmType());
        opciones.setApiHsm(config.getHsmCloud());
        opciones.setJwtHsm(config.getHsmJWT());
        return opciones;
    }

    private bo.firmadigital.jacobitus.validador.Opciones getOpcionesValidador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.validador.Opciones opciones = new bo.firmadigital.jacobitus.validador.Opciones();
        opciones.setProxyHabilitado(config.isProxyEnabled());
        opciones.setServidorProxy(config.getProxyIP());
        opciones.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
        return opciones;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("ADSIB - Jacobitus Total");
        if (!servicio) {
            Alert alert = new Alert(AlertType.ERROR, "Servicio detenido, no podrá interactuar con páginas web", ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        }
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar();
        
        Menu mainMenu = new Menu("Archivo");
        MenuItem actualizarItem = new MenuItem("Actualizar Tokens");
        actualizarItem.setOnAction((ActionEvent e) -> {
            new Thread(listarTokens()).start();
        });
        MenuItem abrirItem = new MenuItem("Abrir PDF");
        abrirItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir PDF");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            FileChooser.ExtensionFilter extFilterDocs = new FileChooser.ExtensionFilter("Documentos", "*.odt", "*.docx");
            fileChooser.getExtensionFilters().add(extFilterDocs);
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                new Thread(validar(files)).start();
            }
        });
        MenuItem abrirOtroItem = new MenuItem("Abrir Otro");
        abrirOtroItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir Otro");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos P7S (*.p7s)", "*.p7s");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Archivos XML (*.xml)", "*.xml");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Archivos JSON (*.json)", "*.json");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Archivos JWS (*.jws)", "*.jws");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Todos los archivos (*)", "*");
            fileChooser.getExtensionFilters().add(extFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                new Thread(validarPKCS7(files)).start();
            }
        });
        MenuItem limpiarItem = new MenuItem("Limpiar Lista");
        limpiarItem.setOnAction((ActionEvent e) -> {
            tableFile.getItems().clear();
        });
        MenuItem opcionesItem = new MenuItem("Opciones");
        opcionesItem.setOnAction((ActionEvent e) -> {
            Configuracion configuracion = new Configuracion(stage);
            configuracion.showAndWait();
        });
        MenuItem abrirCrt = new MenuItem("Ver Certificado");
        abrirCrt.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ver Certificado");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Certificados", "*.crt", "*.pem");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    X509Certificate certificate;
                    try (FileInputStream is = new FileInputStream(file)) {
                        byte[] cert = is.readAllBytes();
                        certificate = Certificate.getCert(cert);
                    }
                    DatosCertificado datosCertificado = new DatosCertificado(file.getName(), certificate);
                    CertInformation information = new CertInformation(datosCertificado, true);
                    Stage info = new Stage();
                    info.setTitle("Certificado");
                    info.initOwner(stage);
                    info.initModality(Modality.APPLICATION_MODAL);
                    Scene scene = new Scene(information);
                    info.setScene(scene);
                    info.showAndWait();
                } catch (IOException | CertificateEncodingException ex) {
                    throw new RuntimeException(ex.getMessage());
                }
            }
        });
        MenuItem closeItem = new MenuItem("Cerrar");
        closeItem.setOnAction((ActionEvent e) -> {
            if (servicio && (!taskBar || taskBarEmulated)) {
                Platform.setImplicitExit(taskBarEmulated);
                try {
                    Main.jettyServer.stop();
                    Main.jettyServer.destroy();
                    stage.close();
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                stage.close();
            }
        });
        mainMenu.getItems().addAll(actualizarItem, abrirItem, abrirOtroItem, limpiarItem, opcionesItem, abrirCrt, closeItem);
        menuBar.getMenus().add(mainMenu);

        Menu firmaMenu = new Menu("Firma");
        MenuItem firmarItem = new MenuItem("Firmar");
        firmarItem.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    if (((Validador)tableFile.getItems().get(0)).isRemoto()) {
                        destino = new File(System.getProperty("java.io.tmpdir"));
                    } else {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setTitle("Seleccione directorio de destino");
                        destino = directoryChooser.showDialog(stage);
                    }
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.PDF);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmar(firmante.isBloquea(), item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarPKCS7Item = new MenuItem("Firmar PKCS#7");
        firmarPKCS7Item.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    if (((Validador)tableFile.getItems().get(0)).isRemoto()) {
                        destino = new File(System.getProperty("java.io.tmpdir"));
                    } else {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setTitle("Seleccione directorio de destino");
                        destino = directoryChooser.showDialog(stage);
                    }
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.PKCS7);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarPKCS7(item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarXmlItem = new MenuItem("Firmar XML");
        firmarXmlItem.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.DSIG);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarXml(item.getSlot(), firmante.getLabel(), firmante.getPass(), firmante.getNode(), firmante.isBloquea())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarJwsItem = new MenuItem("Firmar JSON");
        firmarJwsItem.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.JWS);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarJws(item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
                        }
                    }
                }
            }
        });
        firmaMenu.getItems().addAll(firmarItem, firmarPKCS7Item, firmarXmlItem, firmarJwsItem);
        menuBar.getMenus().add(firmaMenu);
        
        Menu pdfMenu = new Menu("PDF");
        MenuItem nuevoItem = new MenuItem("Nuevo");
        nuevoItem.setOnAction((ActionEvent e) -> {
            Pdf pdf = new Pdf(stage);
            pdf.showAndWait();
            if (pdf.getPath() != null) {
                tableFile.getItems().add(new ValidadorPdf(new File(pdf.getPath()), this.getOpcionesValidador()));
            }
        });
        pdfMenu.getItems().addAll(nuevoItem);
        menuBar.getMenus().add(pdfMenu);
        
        Menu helpMenu = new Menu("Ayuda");
        MenuItem servicioItem = new MenuItem("Verificar servicio");
        servicioItem.setOnAction((ActionEvent e) -> {
            Firefox.registrarCertificado();
            if (!Chromium.registrarCertificado() && OS.isMac()) {
                ContrasenaMac contrasena = new ContrasenaMac(stage);
                contrasena.showAndWait();
                if (contrasena.getPass() == null) {
                    return;
                } else {
                    if (!Chromium.registrarCertificado(contrasena.getPass())) {
                        return;
                    }
                }
            }
            HostServices hostServices = getHostServices();
            hostServices.showDocument("https://localhost:9000");
        });
        MenuItem aboutItem = new MenuItem("Acerca de ...");
        aboutItem.setOnAction((ActionEvent e) -> {
            ImageView adsib = new ImageView(new Image(this.getClass().getClassLoader().getResource("adsib.png").toExternalForm()));
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Acerca de ...");
            alert.setHeaderText("Jacobitus Total " + VERSION + "\nJavaFX " + javafxVersion + "\nJava " + javaVersion);
            alert.setContentText("Agencia para el Desarrollo de la Sociedad de la Información en Bolivia");
            alert.setGraphic(adsib);
            alert.showAndWait();
        });
        helpMenu.getItems().addAll(servicioItem, aboutItem);
        menuBar.getMenus().add(helpMenu);
        root.setTop(menuBar);

        contextMenuToken = new ContextMenu();
        MenuItem contenidoItem = new MenuItem("Información");
        contenidoItem.setOnAction((ActionEvent e) -> {
            TokenInfo info = new TokenInfo(stage, tokenInfo.getSlot());
            info.showAndWait();
        });
        MenuItem pinItem = new MenuItem("Cambiar pin");
        pinItem.setOnAction((ActionEvent e) -> {
            CambiarContrasena cambiarContrasena = new CambiarContrasena(stage, tokenInfo.getSlot());
            cambiarContrasena.showAndWait();
        });
        MenuItem exportarSoftokenItem = new MenuItem("Exportar Softoken");
        exportarSoftokenItem.setOnAction((ActionEvent e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccione directorio de destino");
            File destino = directoryChooser.showDialog(stage);
            if (destino != null) {
                Config config = Config.getInstance();
                try {
                    Files.copy(config.getToken().toPath(), new File(destino, "softoken.p12").toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                    Alert alert = new Alert(AlertType.INFORMATION, "El softoken se exportó correctamente.");
                    alert.showAndWait();
                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR, ex.getMessage());
                    alert.showAndWait();
                }
            }
        });
        contextMenuToken.getItems().addAll(contenidoItem, pinItem, exportarSoftokenItem);

        contextMenu = new ContextMenu();
        MenuItem detalleItem = new MenuItem("Detalle Validación");
        detalleItem.setOnAction((ActionEvent e) -> {
            Detalle detalle = new Detalle(stage, validar, getHostServices());
            detalle.showAndWait();
        });
        exportarItem = new MenuItem("Exportar contenido");
        exportarItem.setVisible(false);
        exportarItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar archivo");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                validar.export(file);
            }
        });
        contextMenu.getItems().addAll(detalleItem, exportarItem);

        table = new TableView();
        TableColumn tokenCol = new TableColumn("Token");
        tokenCol.setCellValueFactory(new PropertyValueFactory("label"));
        table.getColumns().setAll(tokenCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxHeight(76);
        table.setRowFactory(tv -> {
            TableRow<CK_TOKEN_INFO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    tokenInfo = row.getItem();
                    contextMenuToken.getItems().get(2).setVisible(tokenInfo.getSlot() == -1);
                    contextMenuToken.show(table, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        tableFile = new TableView();
        TableColumn fileCol = new TableColumn("Archivo");
        fileCol.setCellValueFactory(new PropertyValueFactory("path"));
        tableFile.getColumns().setAll(fileCol);
        tableFile.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableFile.setRowFactory(tv -> {
            TableRow<Validador> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    HostServices hostServices = getHostServices();
                    hostServices.showDocument(row.getItem().getAbsolutePath());
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    validar = row.getItem();
                    try {
                        exportarItem.setVisible(MagicBytes.P7S.is(validar.getFile()));
                    } catch (IOException ignore) {
                        exportarItem.setVisible(false);
                    }
                    contextMenu.show(tableFile, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
        tableFile.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });
        tableFile.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                new Thread(validar(db.getFiles())).start();
            }
            event.setDropCompleted(success);
            event.consume();
        });

        BorderPane tables = new BorderPane(tableFile);
        BorderPane middle = new BorderPane(tables);
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(middle.widthProperty());

        tables.setTop(table);
        
        root.setCenter(middle);
        middle.setTop(progressBar);
        Label adsib = new Label("ADSIB - firmadigital.bo");
        root.setBottom(new StackPane(adsib));
        ((StackPane)root.getBottom()).setAlignment(Pos.BOTTOM_RIGHT);
        Scene scene = new Scene(root, 640, 480);
        stage.setScene(scene);
        stage.show();
        if (taskBar) {
            Platform.setImplicitExit(false);
            if (url == null && param == null) {
                stage.hide();
            }
        }
        stage.setOnCloseRequest((WindowEvent e) -> {
            if (servicio && (!taskBar || taskBarEmulated)) {
                Platform.setImplicitExit(taskBarEmulated);
                try {
                    Main.jettyServer.stop();
                    Main.jettyServer.destroy();
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        new Thread(registrarCertificado()).start();
        if (url == null) {
            if (param == null) {
                if (taskBar) {
                    SmartCard.cards(this.getOpcionesFirmador());
                } else {
                    new Thread(listarTokens()).start();
                }
            } else {
                File file = new File(param);
                new Thread(validar(Arrays.asList(file))).start();
            }
        } else {
            new Thread(download(url, token, urlPost)).start();
        }
        stage.setOnShown((WindowEvent e) -> {
            if (taskBar) {
                new Thread(listarTokens()).start();
            }
        });
        App.stage = stage;
        App.app = this;
    }

    public Task registrarCertificado() {
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                Firefox.registrarCertificado();
                Chromium.registrarCertificado();
                return true;
            }
        };
        return task;
    }
    
    public Task listarTokens() {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
               
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Slot[] slots = gestorSlot.listarSlots(getOpcionesFirmador());
                    List<CK_TOKEN_INFO> list = new LinkedList();
                    for (Slot s : slots) {
                        list.add(s.detalleToken());
                    }
                    table.setItems(FXCollections.observableList(list));
                    updateProgress(100, 100);
                    return true;
                } catch (RuntimeException ex) {
                    updateProgress(100, 100);
                    table.getItems().clear();
                    throw ex;
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Jacobitus");
            BorderPane pane = new BorderPane();
            Label label = new Label(err);
            pane.setTop(label);
            alert.getDialogPane().setContent(pane);
            if (err.startsWith("http")) {
                Hyperlink link = new Hyperlink(err);
                link.setOnAction((ActionEvent t) -> {
                    getHostServices().showDocument(err);
                });
                pane.setCenter(link);
                label.setText("No se encontro el controlador del token, por favor descargue e instale del siguiente link.");
            }
            alert.showAndWait();
        });
        return task;
    }

    public Task validar(List<File> files) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validador> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getName().endsWith(".odt")) {
                        certs.add(new ValidadorPdf(Converter.odtToPdf(files.get(i)), getOpcionesValidador()));
                    } else if (files.get(i).getName().endsWith(".docx")) {
                        certs.add(new ValidadorPdf(Converter.docxToPdf(files.get(i)), getOpcionesValidador()));
                    } else if (files.get(i).getName().endsWith(".pdf")) {
                        certs.add(new ValidadorPdf(files.get(i), getOpcionesValidador()));
                    } else {
                        certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                    }
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task firmar(boolean bloquear, long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                Config config = Config.getInstance();
                bo.firmadigital.jacobitus.validador.Opciones opcionesValidador = new bo.firmadigital.jacobitus.validador.Opciones();
                opcionesValidador.setProxyHabilitado(config.isProxyEnabled());
                opcionesValidador.setServidorProxy(config.getProxyIP());
                opcionesValidador.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));

                StringBuilder errores = new StringBuilder();
                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    for (int i = 0; i < files.size(); i++) {
                        try {
                            IFirmador firmar = FirmadorPdf.getInstance(slot, label, pass, getOpcionesFirmador());
                            String name = new File(files.get(i).getAbsolutePath()).getName();
                            if (!name.endsWith(".pdf")) {
                                name += ".firmado.pdf";
                            } else {
                                name = name.replace(".pdf", ".firmado.pdf");
                            }
                            File out = new File(destino, name);
                            try (InputStream is = new FileInputStream(files.get(i).getAbsolutePath()); OutputStream os = new FileOutputStream(out)) {
                                firmar.firmar(is, os, bloquear);
                            }
                            updateProgress(i + 1, files.size());
                            tableFile.getItems().set(i, new ValidadorPdf(out, opcionesValidador));
                        } catch (IOException ex) {
                            updateProgress(i + 1, files.size());
                            errores.append(files.get(i).getAbsolutePath()).append(":").append(ex.getMessage()).append("\n");
                        } catch (PdfException ex) {
                            updateProgress(i + 1, files.size());
                            errores.append(files.get(i).getAbsolutePath()).append(":").append(ex.getCause().getMessage()).append("\n");
                        }
                    }
                }
                if (errores.length() == 0) {
                    return true;
                } else {
                    throw new RuntimeException(errores.toString());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task validarPKCS7(List<File> files) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validador> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (MagicBytes.PDF.is(files.get(i))) {
                        certs.add(new ValidadorPdf(files.get(i), getOpcionesValidador()));
                    } else {
                        if (MagicBytes.XML.is(files.get(i))) {
                            certs.add(new ValidadorXml(files.get(i), getOpcionesValidador()));
                        } else {
                            if (MagicBytes.P7S.is(files.get(i))) {
                                certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                            } else {
                                if (MagicBytes.isJWS(files.get(i))) {
                                    certs.add(new ValidadorJws(files.get(i), getOpcionesValidador()));
                                } else {
                                    certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                                }
                            }
                        }
                    }
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task firmarPKCS7(long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    Config config = Config.getInstance();
                    bo.firmadigital.jacobitus.validador.Opciones opcionesValidador = new bo.firmadigital.jacobitus.validador.Opciones();
                    opcionesValidador.setProxyHabilitado(config.isProxyEnabled());
                    opcionesValidador.setServidorProxy(config.getProxyIP());
                    opcionesValidador.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
                    
                    IFirmador firmar = FirmadorPKCS7.getInstance(slot, label, pass, getOpcionesFirmador());
                    for (int i = 0; i < files.size(); i++) {
                        File out = new File(destino, files.get(i).getFile().getName() + ".p7s");
                        try (InputStream is = new BufferedInputStream(new FileInputStream(files.get(i).getFile())); FileOutputStream os = new FileOutputStream(out)) {
                            firmar.firmar(is, os);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorPKCS7(out, opcionesValidador));
                    }
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task firmarXml(long slot, String label, String pass, String node, Boolean enveloped) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    Config config = Config.getInstance();
                    bo.firmadigital.jacobitus.validador.Opciones opcionesValidador = new bo.firmadigital.jacobitus.validador.Opciones();
                    opcionesValidador.setProxyHabilitado(config.isProxyEnabled());
                    opcionesValidador.setServidorProxy(config.getProxyIP());
                    opcionesValidador.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));

                    IFirmador firmar = FirmadorXml.getInstance(slot, label, pass, node, getOpcionesFirmador());
                    for (int i = 0; i < files.size(); i++) {
                        String name = new File(files.get(i).getAbsolutePath()).getName();
                        if (!name.endsWith(".xml")) {
                            name += ".firmado.xml";
                        } else {
                            name = name.replace(".xml", ".firmado.xml");
                        }
                        File out = new File(destino, name);
                        try (InputStream is = new BufferedInputStream(new FileInputStream(files.get(i).getFile())); FileOutputStream os = new FileOutputStream(out)) {
                            firmar.firmar(is, os, enveloped);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorXml(out, opcionesValidador));
                    }
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task firmarJws(long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    Config config = Config.getInstance();
                    bo.firmadigital.jacobitus.validador.Opciones opcionesValidador = new bo.firmadigital.jacobitus.validador.Opciones();
                    opcionesValidador.setProxyHabilitado(config.isProxyEnabled());
                    opcionesValidador.setServidorProxy(config.getProxyIP());
                    opcionesValidador.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));

                    IFirmador firmar = FirmadorJws.getInstance(slot, label, pass, getOpcionesFirmador());
                    for (int i = 0; i < files.size(); i++) {
                        String name = new File(files.get(i).getAbsolutePath()).getName();
                        if (name.endsWith(".json")) {
                            name = name.replace(".json", ".jws");
                        } else {
                            name = name + ".jws";
                        }
                        File out = new File(destino, name);
                        try (InputStream is = new BufferedInputStream(new FileInputStream(files.get(i).getFile())); FileOutputStream os = new FileOutputStream(out)) {
                            firmar.firmar(is, os, false);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorJws(out, opcionesValidador));
                    }
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public Task download(String urlFile, String token, String urlPost) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                URL url = new URL(urlFile);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (token != null) {
                    connection.setRequestProperty("Authorization", token);
                }
                connection.connect();
                int size = 0;
                List values = connection.getHeaderFields().get("content-Length");
                if (values != null && !values.isEmpty()) {
                    String sLength = (String) values.get(0);
                    if (sLength != null) {
                        size = Integer.parseInt(sLength);
                    }
                }
                String fileName = UrlFileName.getFileName(connection);
                if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                        connection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL) {
                    InputStream responseStream = connection.getInputStream();
                    File f = new File(System.getProperty("java.io.tmpdir"), fileName);
                    try (OutputStream outStream = new FileOutputStream(f)) {
                        byte[] buffer = new byte[8 * 1024];
                        int t = 0, bytesRead;
                        while ((bytesRead = responseStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                            if (size > 0) {
                                t += bytesRead;
                                updateProgress(t, size);
                            }
                        }
                    }
                    List<Validador> certs = new LinkedList();
                    if (MagicBytes.PDF.is(f)) {
                        certs.add(new ValidadorPdf(f, urlPost, token, getOpcionesValidador()));
                    } else {
                        certs.add(new ValidadorPKCS7(f, urlPost, token, getOpcionesValidador()));
                    }
                    tableFile.setItems(FXCollections.observableList(certs));
                    if (size == 0) {
                        updateProgress(1, 1);
                    }
                    return true;
                } else {
                    throw new RuntimeException("No se pudo descargar el archivo.");
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((Event evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
        return task;
    }

    public static void show() {
        Platform.runLater(() -> {
            if (taskBar) {
                stage.show();
            } else {
                stage.setIconified(false);
            }
        });
    }

    public static void show(String error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR, error, ButtonType.OK);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
        });
    }

    public static void show(File file) {
        Platform.runLater(() -> {
            if (stage.isShowing()) {
                stage.setAlwaysOnTop(true);
                stage.setAlwaysOnTop(false);
            } else {
                if (taskBar) {
                    stage.show();
                } else {
                    stage.setIconified(false);
                }
            }
            new Thread(app.validar(Arrays.asList(file))).start();
        });
    }

    public static void show(String url, String token, String urlPost) {
        Platform.runLater(() -> {
            if (stage.isShowing()) {
                stage.setAlwaysOnTop(true);
                stage.setAlwaysOnTop(false);
            } else {
                if (taskBar) {
                    stage.show();
                } else {
                    stage.setIconified(false);
                }
            }
            new Thread(app.download(url, token, urlPost)).start();
        });
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulated) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.taskBarEmulated = taskBarEmulated;
        launch();
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulated, String file) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.taskBarEmulated = taskBarEmulated;
        App.param = file;
        launch();
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulated, String url, String token, String urlPost) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.taskBarEmulated = taskBarEmulated;
        App.url = url;
        App.token = token;
        App.urlPost = urlPost;
        launch();
    }

    public static TokenSelected service(Slot slot, String ci, JSONArray files) {
        if (tokenSelected.isShown()) {
            throw new RuntimeException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(slot);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(files);
            tokenSelected.setFilesJson(null);
            Service service = new Service(stage, tokenSelected, "pades");
            service.showAndWait();
            synchronized(tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }

    public static TokenSelected serviceJWS(Slot slot, String ci, JSONArray files) {
        if (tokenSelected.isShown()) {
            throw new RuntimeException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(slot);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(files);
            tokenSelected.setFilesJson(null);
            Service service = new Service(stage, tokenSelected, "jws");
            service.showAndWait();
            synchronized(tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }

    public static TokenSelected service(Slot[] slots, String ci, JSONArray pdfs, JSONArray jsons) {
        if (tokenSelected.isShown()) {
            throw new RuntimeException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(null);
            tokenSelected.setSlots(slots);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(pdfs);
            tokenSelected.setFilesJson(jsons);
            Service service = new Service(stage, tokenSelected, "both");
            service.showAndWait();
            synchronized(tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }
}
