/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONArray;

import com.itextpdf.kernel.PdfException;

import bo.firmadigital.jacobitus.comun.SmartCard;
import bo.firmadigital.jacobitus.comun.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.firmador.Constants;
import bo.firmadigital.jacobitus.firmador.FirmadorJws;
import bo.firmadigital.jacobitus.firmador.FirmadorPKCS7;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.FirmadorXml;
import bo.firmadigital.jacobitus.firmador.IFirmador;
import bo.firmadigital.jacobitus.firmador.TokenSelected;
import bo.firmadigital.jacobitus.token.ChangePinJNI;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.utilidades.OS;
import bo.firmadigital.jacobitus.validador.Certificate;
import bo.firmadigital.jacobitus.validador.DatosCertificado;
import bo.firmadigital.jacobitus.validador.MagicBytes;
import bo.firmadigital.jacobitus.validador.Validador;
import bo.firmadigital.jacobitus.validador.ValidadorJws;
import bo.firmadigital.jacobitus.validador.ValidadorPKCS7;
import bo.firmadigital.jacobitus.validador.ValidadorPdf;
import bo.firmadigital.jacobitus.validador.ValidadorXml;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus4.util.ECA;
import bo.firmadigital.jacobitus4.util.UrlFileName;
import bo.firmadigital.utiles.CertUtil;
import bo.firmadigital.utiles.Conversor;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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
import javafx.scene.control.SeparatorMenuItem;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    private static String contraseniaMacOS = null;
    private static boolean isLaunched = false;

    private bo.firmadigital.jacobitus.firmador.Opciones getOpcionesFirmador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.firmador.Opciones opciones = new bo.firmadigital.jacobitus.firmador.Opciones();
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

    private bo.firmadigital.jacobitus.validador.Opciones getOpcionesValidador() {
        Config config = Config.getInstance();
        bo.firmadigital.jacobitus.validador.Opciones opciones = new bo.firmadigital.jacobitus.validador.Opciones();
        opciones.setProxyHabilitado(config.isProxyEnabled());
        opciones.setServidorProxy(config.getProxyIP());
        opciones.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
        return opciones;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException {
        String version = Constantes.VERSION;
        stage.setTitle("ADSIB - Jacobitus Total - " + version);
        if (!servicio) {
            // Alert alert = new Alert(AlertType.ERROR, "Servicio detenido, no podrá interactuar con aplicaciones web.", ButtonType.OK);
            Alert alert = new Alert(AlertType.ERROR, WebServer.mensaje, ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.setTitle("ADSIB - Jacobitus Total - " + version + " (Servicio detenido)");
        }
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));

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
        MenuItem abrirOtroItem = new MenuItem("Abrir otros formatos");
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
        MenuItem convertiAPdf = new MenuItem("Convertir a PDF");
        convertiAPdf.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Convertir a PDF");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos ODT (*.odt)", "*.odt");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Archivos DOCX (*.docx)", "*.docx");
            fileChooser.getExtensionFilters().add(extFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                new Thread(validar(files)).start();
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
                    VBox vBox = new VBox();
                    vBox.setPadding(new Insets(10));
                    vBox.setSpacing(4);
                    vBox.getChildren().add(information);
                    Stage info = new Stage();
                    info.setTitle("Certificado");
                    info.initOwner(stage);
                    info.initModality(Modality.APPLICATION_MODAL);
                    Scene scene = new Scene(vBox);
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
                    WebServer.detener();
                    stage.close();
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                stage.close();
            }
        });
        mainMenu.getItems().addAll(actualizarItem, abrirItem, abrirOtroItem, convertiAPdf, limpiarItem, opcionesItem, abrirCrt, closeItem);
        menuBar.getMenus().add(mainMenu);

        Menu firmaMenu = new Menu("Firma");
        MenuItem firmarItem = new MenuItem("Firmar PDF");
        firmarItem.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
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
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.PDF);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarPdf(firmante.isBloquea(), item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarPKCS7Item = new MenuItem("Firmar PKCS#7");
        firmarPKCS7Item.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
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
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
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
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), Constants.DSIG);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarXml(item.getSlot(), firmante.getLabel(), firmante.getPass(), firmante.getNode(), firmante.getForzarEnveloped(), firmante.getUsarPrefijo())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarJwsItem = new MenuItem("Firmar JSON");
        firmarJwsItem.setOnAction((ActionEvent e) -> {
            if (tableFile.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO item = (CK_TOKEN_INFO)table.getSelectionModel().getSelectedItem();
                if (item == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
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
            new Thread(verificarServicio(version)).start();
        });

        MenuItem aboutItem = new MenuItem("Acerca de ...");
        aboutItem.setOnAction((ActionEvent e) -> {
            String versionLibreria = bo.firmadigital.jacobitus.Version.VERSION;
            String javaVersion = System.getProperty("java.version");
            String javafxVersion = System.getProperty("javafx.version");
            String changePinVersion = new ChangePinJNI().version();
            ImageView adsib = new ImageView(new Image(this.getClass().getClassLoader().getResource("adsib.png").toExternalForm()));
            StringBuilder sb = new StringBuilder();
            sb.append("Jacobitus Escritorio " + version + "\n");
            sb.append("Jacobitus Librería " + versionLibreria + "\n");
            sb.append("ChangePin Library " + changePinVersion + "\n");
            sb.append("JavaFX " + javafxVersion + "\n");
            sb.append("Java " + javaVersion);
            
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Acerca de ...");
            alert.setHeaderText(sb.toString());
            alert.setContentText("Agencia para el Desarrollo de la Sociedad de la Información en Bolivia");
            alert.setGraphic(adsib);
            alert.showAndWait();
        });

        SeparatorMenuItem sep = new SeparatorMenuItem();
        helpMenu.getItems().addAll(servicioItem, sep, aboutItem);
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
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.showAndWait();
                } catch (IOException ex) {
                    Alert alert = new Alert(AlertType.ERROR, ex.getMessage());
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
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
        if (taskBar && !OS.isDebian()) {
            Platform.setImplicitExit(false);
            if (url == null && param == null) {
                stage.hide();
            }
        } else {
            Platform.runLater(() -> {
                new Thread(listarTokens()).start();
            });
        }
        stage.setOnCloseRequest((WindowEvent e) -> {
            if (servicio && (!taskBar || taskBarEmulated)) {
                Platform.setImplicitExit(taskBarEmulated);
                try {
                    WebServer.detener();
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
                    // new Thread(listarTokens()).start();
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

    public Task<Boolean> registrarCertificado() {
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                // TODO: Registrar certificados al iniciar la aplicacion
                return true;
            }
        };
        return task;
    }
    
    public Task<Boolean> listarTokens() {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                App.stage.getScene().setCursor(Cursor.WAIT);
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setOpciones(getOpcionesFirmador());
                    Slot[] slots = gestorSlot.listarSlots();
                    List<CK_TOKEN_INFO> list = new LinkedList();
                    for (Slot s : slots) {
                        list.add(s.detalleToken());
                    }
                    table.setItems(FXCollections.observableList(list));
                    updateProgress(100, 100);
                    App.stage.getScene().setCursor(Cursor.DEFAULT);
                    return true;
                } catch (RuntimeException ex) {
                    updateProgress(100, 100);
                    table.getItems().clear();
                    throw ex;
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            String err = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
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
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> validar(List<File> files) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
                List<Validador> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getName().endsWith(".odt")) {
                        certs.add(new ValidadorPdf(Conversor.odtAPdf(files.get(i)), getOpcionesValidador()));
                    } else if (files.get(i).getName().endsWith(".docx")) {
                        certs.add(new ValidadorPdf(Conversor.docxAPdf(files.get(i)), getOpcionesValidador()));
                    } else if (files.get(i).getName().endsWith(".pdf")) {
                        certs.add(new ValidadorPdf(files.get(i), getOpcionesValidador()));
                    // TODO: Ajustar los mensajes de validacion de documentos xml y json, considerando que no se puede determinar si fueron firmados dentro del periodo de vigencia del certificado
                    } else if (files.get(i).getName().endsWith(".xml")) {
                        certs.add(new ValidadorXml(files.get(i), null, getOpcionesValidador()));
                    } else if (files.get(i).getName().endsWith(".jws")) {
                        certs.add(new ValidadorJws(files.get(i), null, getOpcionesValidador()));
                    } else {
                        certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                    }
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                stage.getScene().setCursor(Cursor.DEFAULT);
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> firmarPdf(boolean bloquear, long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpcionesFirmador());
                IToken token = gestorSlot.obtenerSlot(slot).getToken();
                token.iniciar(pass);
                DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
                token.salir();
                if (!ECA.esValida(datos) || !ECA.esPublica(datos)) {
                    throw new RuntimeException("Certificado no emitido por la ECP-ADSIB.");
                }

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
                                firmar.firmar(is, os, bloquear, false);
                            }
                            updateProgress(i + 1, files.size());
                            tableFile.getItems().set(i, new ValidadorPdf(out, getOpcionesValidador()));
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
                    stage.getScene().setCursor(Cursor.DEFAULT);
                    return true;
                } else {
                    throw new RuntimeException(errores.toString());
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> validarPKCS7(List<File> files) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
                List<Validador> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (MagicBytes.PDF.is(files.get(i))) {
                        certs.add(new ValidadorPdf(files.get(i), getOpcionesValidador()));
                    } else {
                        if (MagicBytes.XML.is(files.get(i))) {
                            certs.add(new ValidadorXml(files.get(i), null, getOpcionesValidador()));
                        } else {
                            if (MagicBytes.P7S.is(files.get(i))) {
                                certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                            } else {
                                if (MagicBytes.isJWS(files.get(i))) {
                                    certs.add(new ValidadorJws(files.get(i), null, getOpcionesValidador()));
                                } else {
                                    certs.add(new ValidadorPKCS7(files.get(i), getOpcionesValidador()));
                                }
                            }
                        }
                    }
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                stage.getScene().setCursor(Cursor.DEFAULT);
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> firmarPKCS7(long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpcionesFirmador());
                IToken token = gestorSlot.obtenerSlot(slot).getToken();
                token.iniciar(pass);
                DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
                token.salir();
                if (!ECA.esValida(datos) || !ECA.esPublica(datos)) {
                    throw new RuntimeException("Certificado no emitido por la ECP-ADSIB.");
                }

                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    IFirmador firmar = FirmadorPKCS7.getInstance(slot, label, pass, getOpcionesFirmador());
                    for (int i = 0; i < files.size(); i++) {
                        File out = new File(destino, files.get(i).getFile().getName() + ".p7s");
                        try (InputStream is = new BufferedInputStream(new FileInputStream(files.get(i).getFile())); FileOutputStream os = new FileOutputStream(out)) {
                            firmar.firmar(is, os);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorPKCS7(out, getOpcionesValidador()));
                    }
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> firmarXml(long slot, String label, String pass, String node, Boolean forzarEnveloped, Boolean usarPrefijo) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpcionesFirmador());
                IToken token = gestorSlot.obtenerSlot(slot).getToken();
                token.iniciar(pass);
                DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
                token.salir();
                if (!ECA.esValida(datos) || !ECA.esPublica(datos)) {
                    throw new RuntimeException("Certificado no emitido por la ECP-ADSIB.");
                }

                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
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
                            firmar.firmar(is, os, forzarEnveloped, usarPrefijo);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorXml(out, null, getOpcionesValidador()));
                    }
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> firmarJws(long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setOpciones(getOpcionesFirmador());
                IToken token = gestorSlot.obtenerSlot(slot).getToken();
                token.iniciar(pass);
                DatosCertificado datos = new DatosCertificado(label, token.obtenerCertificado(label));
                token.salir();
                if (!ECA.esValida(datos) || !ECA.esPublica(datos)) {
                    throw new RuntimeException("Certificado no emitido por la ECP-ADSIB.");
                }

                List<Validador> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
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
                            firmar.firmar(is, os, false, false);
                        }
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidadorJws(out, null, getOpcionesValidador()));
                    }
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> download(String urlFile, String token, String urlPost) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
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
                    stage.getScene().setCursor(Cursor.DEFAULT);
                    return true;
                } else {
                    throw new RuntimeException("No se pudo descargar el archivo.");
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Void> verificarServicio(String version) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    stage.getScene().setCursor(Cursor.WAIT);
                    String certificadoServicioLocal = "";
                    boolean certificadoServicioLocalInstalado = false;
                    boolean errorCertificadoServicioLocalInstalado = false;
                    try {
                        if (OS.isMac()) {
                            ContrasenaMac contrasena = new ContrasenaMac(stage);
                            contrasena.showAndWait();
                            App.contraseniaMacOS = contrasena.getPass();
                            if (App.contraseniaMacOS == null) {
                                return;
                            }
                        } else {
                            App.contraseniaMacOS = null;
                        }
                        if (OS.isMac()) {
                            certificadoServicioLocalInstalado = CertUtil.verificarCertificadoServicioLocal(App.contraseniaMacOS);
                        } else {
                            certificadoServicioLocalInstalado = CertUtil.verificarCertificadoServicioLocal();
                        }
                        if (certificadoServicioLocalInstalado) {
                            certificadoServicioLocal = "Certificado de servicio local instalado";
                        } else {
                            certificadoServicioLocal = "Certificado de servicio local sin instalar";
                        }
                    } catch (IOException e1) {
                        certificadoServicioLocal = "Problemas al verificar certificado de servicio local";
                        errorCertificadoServicioLocalInstalado = true;
                    }
                    
                    ImageView adsib = new ImageView(new Image(this.getClass().getClassLoader().getResource("adsib.png").toExternalForm()));
                    StringBuilder sb = new StringBuilder();
                    sb.append("Jacobitus Total " + version + "\n");
                    sb.append(certificadoServicioLocal);
                    
                    Alert verificarServicioAlerta = new Alert(AlertType.INFORMATION);
                    verificarServicioAlerta.initOwner(stage);
                    verificarServicioAlerta.initModality(Modality.APPLICATION_MODAL);
                    verificarServicioAlerta.setTitle("Verificar servicio");
                    verificarServicioAlerta.setHeaderText(sb.toString());
                    verificarServicioAlerta.setContentText("Agencia para el Desarrollo de la Sociedad de la Información en Bolivia");
                    verificarServicioAlerta.setGraphic(adsib);
    
                    VBox vbox = new VBox();
                    vbox.setSpacing(5);
    
                    FlowPane fpCertificadoServicioLocal = new FlowPane();
                    Label lblCertificadoServicioLocal = new Label("Certificado de servicio local ->");
                    Hyperlink instalarCertificadoServicioLocal = new Hyperlink("Instalar");
                    Hyperlink desinstalarCertificadoServicioLocal = new Hyperlink("Desinstalar");
                    if (certificadoServicioLocalInstalado) {
                        desinstalarCertificadoServicioLocal.setOnAction(e1 -> {
                            try {
                                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                                confirmacion.initOwner(stage);
                                confirmacion.initModality(Modality.APPLICATION_MODAL);
                                confirmacion.setHeaderText(null);
                                confirmacion.setTitle("Confirmación");
                                confirmacion.setContentText("¿Está seguro de desinstalar el certificado?");
    
                                Optional<ButtonType> action = confirmacion.showAndWait();
                                if (action.get() == ButtonType.OK) {
                                    if (OS.isMac()) {
                                        CertUtil.desinstalarCertificadoServicioLocal(App.contraseniaMacOS);
                                    } else {
                                        CertUtil.desinstalarCertificadoServicioLocal();
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            verificarServicioAlerta.close();
                        });
                        if (errorCertificadoServicioLocalInstalado) {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal);
                        } else {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal, desinstalarCertificadoServicioLocal);
                        }
                    } else {
                        instalarCertificadoServicioLocal.setOnAction((e1) -> {
                            try {
                                Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
                                confirmacion.initOwner(stage);
                                confirmacion.initModality(Modality.APPLICATION_MODAL);
                                confirmacion.setHeaderText(null);
                                confirmacion.setTitle("Confirmación");
                                confirmacion.setContentText("¿Está seguro de instalar el certificado?");
    
                                Optional<ButtonType> action = confirmacion.showAndWait();
                                if (action.get() == ButtonType.OK) {
                                    if (OS.isMac()) {
                                        CertUtil.instalarCertificadoServicioLocal(App.contraseniaMacOS);
                                    } else {
                                        CertUtil.instalarCertificadoServicioLocal();
                                    }
                                }
                            } catch (IOException | InterruptedException ex) {
                                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            verificarServicioAlerta.close();
                        });
                        if (errorCertificadoServicioLocalInstalado) {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal);
                        } else {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal, instalarCertificadoServicioLocal);
                        }
                    }

                    vbox.getChildren().addAll(fpCertificadoServicioLocal);
    
                    verificarServicioAlerta.getDialogPane().contentProperty().set(vbox);
                    verificarServicioAlerta.showAndWait();
                    stage.getScene().setCursor(Cursor.DEFAULT);
                });
                return null;
            }
        };
        task.setOnFailed((WorkerStateEvent evt) -> {
            Alert alert = new Alert(AlertType.WARNING, task.getException().getMessage(), ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
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
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
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
        if (!App.isLaunched) {
            launch();
            App.isLaunched = true;
        }
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulated, String file) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.taskBarEmulated = taskBarEmulated;
        App.param = file;
        if (!App.isLaunched) {
            launch();
            App.isLaunched = true;
        }
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulated, String url, String token, String urlPost) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.taskBarEmulated = taskBarEmulated;
        App.url = url;
        App.token = token;
        App.urlPost = urlPost;
        if (!App.isLaunched) {
            launch();
            App.isLaunched = true;
        }
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
