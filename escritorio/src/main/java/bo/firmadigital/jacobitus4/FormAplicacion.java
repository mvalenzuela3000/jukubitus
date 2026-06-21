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

import com.itextpdf.kernel.exceptions.PdfException;

import bo.firmadigital.jacobitus.comun.InfoCertificado;
import bo.firmadigital.jacobitus.comun.JacobitusException;
import bo.firmadigital.jacobitus.firmador.FirmadorJws;
import bo.firmadigital.jacobitus.firmador.FirmadorPKCS7;
import bo.firmadigital.jacobitus.firmador.FirmadorPdf;
import bo.firmadigital.jacobitus.firmador.FirmadorXml;
import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.firmador.base.IFirmador;
import bo.firmadigital.jacobitus.firmador.base.SmartCard;
import bo.firmadigital.jacobitus.firmador.comun.MagicBytes;
import bo.firmadigital.jacobitus.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.jacobitus.token.ChangePinJNI;
import bo.firmadigital.jacobitus.token.GestorSlot;
import bo.firmadigital.jacobitus.token.IToken;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.utilidades.CertificadoHelper;
import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;
import bo.firmadigital.jacobitus.validador.base.ConfiguracionValidador;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.comun.Constants;
import bo.firmadigital.jacobitus4.comun.TokenSelected;
import bo.firmadigital.jacobitus4.extendidos.ValidadorExtendido;
import bo.firmadigital.jacobitus4.extendidos.ValidadorExtendidoJws;
import bo.firmadigital.jacobitus4.extendidos.ValidadorExtendidoPKCS7;
import bo.firmadigital.jacobitus4.extendidos.ValidadorExtendidoPdf;
import bo.firmadigital.jacobitus4.extendidos.ValidadorExtendidoXml;
import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.jacobitus4.util.ECA;
import bo.firmadigital.jacobitus4.util.UrlFileName;
import bo.firmadigital.jacobitus4.util.actualizacion.ActualizacionHelper;
import bo.firmadigital.jacobitus4.util.actualizacion.ActualizacionInfo;
import bo.firmadigital.jacobitus4.util.plataforma.PlataformaHelper;
import bo.firmadigital.jacobitus4.util.plataforma.PlataformaInfo;
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
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("rawtypes")
public class FormAplicacion extends Application {
    private ProgressBar progressBar;
    private TableView<CK_TOKEN_INFO> tbvDispositivos;
    private TableView<ValidadorExtendido> tbvArchivos;

    private File rutaDestino;
    private ValidadorExtendido validador;
    private CK_TOKEN_INFO tokenInfoSeleccionado;

    private static Stage stage;

    private static boolean servicio;
    private static boolean taskBar;
    private static boolean taskBarEmulado;
    
    private static String urlArchivo = null;
    private static String tokenAutorizacion;
    private static String urlRespuesta;
    
    private static String urlParametro = null;
    private static final TokenSelected tokenSelected = new TokenSelected();
    private static String contraseniaMacOS = null;
    private static boolean lanzada = false;
    
    private static FormAplicacion app;

    private ConfiguracionFirmador getConfigFirmador() {
        Config config = Config.getInstance();
        ConfiguracionFirmador configFirmador = new ConfiguracionFirmador();
        configFirmador.setControlador(config.getDriver());
        configFirmador.setToken(config.getToken());
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

    private ConfiguracionValidador getConfigValidador() {
        Config config = Config.getInstance();
        ConfiguracionValidador configValidador = new ConfiguracionValidador();
        configValidador.setProxyHabilitado(config.isProxyEnabled());
        configValidador.setServidorProxy(config.getProxyIP());
        configValidador.setPuertoServidorProxy(Integer.parseInt(config.getProxyPort()));
        return configValidador;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void start(Stage stage) throws IOException, URISyntaxException, InterruptedException {
        MenuItem exportarItem;
        ContextMenu contextMenu;
        ContextMenu tokenContextMenu;
        String version = Informacion.VERSION;
        stage.setTitle("Jacobitus - " + version);
        if (!servicio) {
            // Alert alert = new Alert(AlertType.ERROR, "Servicio detenido, no podrá
            // interactuar con aplicaciones web.", ButtonType.OK);
            Alert alert = new Alert(AlertType.ERROR, WebServer.mensaje, ButtonType.OK);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            alert.showAndWait();
            stage.setTitle("Jacobitus - " + version + " (Servicio detenido)");
        }
        stage.getIcons().add(new Image(this.getClass().getClassLoader().getResourceAsStream("icon.png")));

        Config config = Config.getInstance();
        PlataformaInfo plataformaInfo = PlataformaHelper.identificar();
        ActualizacionHelper actualizacionHelper = new ActualizacionHelper(
                config.getEnlaceInstaladores());

        ActualizacionInfo actualizacionInfo = actualizacionHelper.verificarActualizacion(
                config.getEnlaceVersion(),
                Informacion.VERSION,
                plataformaInfo
                        .getSistemaOperativo()
                        .getValor(),
                plataformaInfo
                        .getArquitectura()
                        .getValor());

        BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar();

        Menu archivoMenu = new Menu("Archivo");
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
            FileChooser.ExtensionFilter extFilterDocs = new FileChooser.ExtensionFilter("Documentos", "*.odt",
                    "*.docx");
            fileChooser.getExtensionFilters().add(extFilterDocs);
            List<File> archivosSeleccionados = fileChooser.showOpenMultipleDialog(stage);
            if (archivosSeleccionados != null && !archivosSeleccionados.isEmpty()) {
                new Thread(validar(archivosSeleccionados)).start();
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
            List<File> archivosSeleccionados = fileChooser.showOpenMultipleDialog(stage);
            if (archivosSeleccionados != null && !archivosSeleccionados.isEmpty()) {
                new Thread(validarPKCS7(archivosSeleccionados)).start();
            }
        });
        MenuItem convertirAPdf = new MenuItem("Convertir a PDF");
        convertirAPdf.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Convertir a PDF");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos ODT (*.odt)", "*.odt");
            fileChooser.getExtensionFilters().add(extFilter);
            extFilter = new FileChooser.ExtensionFilter("Archivos DOCX (*.docx)", "*.docx");
            fileChooser.getExtensionFilters().add(extFilter);
            List<File> archivosSeleccionados = fileChooser.showOpenMultipleDialog(stage);
            if (archivosSeleccionados != null && !archivosSeleccionados.isEmpty()) {
                new Thread(validar(archivosSeleccionados)).start();
            }
        });
        MenuItem limpiarItem = new MenuItem("Limpiar Lista");
        limpiarItem.setOnAction((ActionEvent e) -> {
            tbvArchivos.getItems().clear();
        });
        MenuItem opcionesItem = new MenuItem("Opciones");
        opcionesItem.setOnAction((ActionEvent e) -> {
            FormConfiguracion formConfiguracion = new FormConfiguracion(stage);
            formConfiguracion.showAndWait();
        });
        MenuItem abrirCrt = new MenuItem("Ver Certificado");
        abrirCrt.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Ver Certificado");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Certificados", "*.crt", "*.pem");
            fileChooser.getExtensionFilters().add(extFilter);
            File archivoSeleccionado = fileChooser.showOpenDialog(stage);
            if (archivoSeleccionado != null) {
                try {
                    X509Certificate x509Certificate;
                    try (FileInputStream is = new FileInputStream(archivoSeleccionado)) {
                        byte[] certificado = is.readAllBytes();
                        x509Certificate = CertificadoHelper.obtenerCertificado(certificado);
                    }
                    InfoCertificado infoCertificado = new InfoCertificado(archivoSeleccionado.getName(), x509Certificate);
                    CertInformation certInformation = new CertInformation(infoCertificado, true);
                    VBox vBox = new VBox();
                    vBox.setPadding(new Insets(10));
                    vBox.setSpacing(4);
                    vBox.getChildren().add(certInformation);
                    Stage certificadoStage = new Stage();
                    certificadoStage.setTitle("Certificado");
                    certificadoStage.initOwner(stage);
                    certificadoStage.initModality(Modality.APPLICATION_MODAL);
                    Scene scene = new Scene(vBox);
                    certificadoStage.setScene(scene);
                    certificadoStage.showAndWait();
                } catch (IOException | CertificateEncodingException ex) {
                    throw new JacobitusException(ex.getMessage());
                }
            }
        });
        MenuItem cerrarItem = new MenuItem("Cerrar");
        cerrarItem.setOnAction((ActionEvent e) -> {
            if (servicio && (!taskBar || taskBarEmulado)) {
                Platform.setImplicitExit(taskBarEmulado);
                try {
                    WebServer.detener();
                    stage.close();
                } catch (Exception ex) {
                    Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                stage.close();
            }
        });
        archivoMenu.getItems().addAll(actualizarItem, abrirItem, abrirOtroItem, convertirAPdf, limpiarItem, opcionesItem,
                abrirCrt, cerrarItem);
        menuBar.getMenus().add(archivoMenu);

        Menu firmaMenu = new Menu("Firma");
        MenuItem firmarItem = new MenuItem("Firmar PDF");
        firmarItem.setOnAction((ActionEvent e) -> {
            if (tbvArchivos.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO itemSeleccionado = tbvDispositivos.getSelectionModel().getSelectedItem();
                if (itemSeleccionado == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    if ((tbvArchivos.getItems().get(0)).isRemoto()) {
                        rutaDestino = new File(System.getProperty("java.io.tmpdir"));
                    } else {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setTitle("Seleccione directorio de destino");
                        rutaDestino = directoryChooser.showDialog(stage);
                    }
                    if (rutaDestino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION,
                                "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        FormFirmante formFirmante = new FormFirmante(stage, itemSeleccionado.getSlot(), Constants.PDF);
                        formFirmante.showAndWait();
                        if (formFirmante.getLabel() != null) {
                            new Thread(firmarPdf(formFirmante.isBloquea(), itemSeleccionado.getSlot(), formFirmante.getLabel(),
                                    formFirmante.getPass())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarPKCS7Item = new MenuItem("Firmar PKCS#7");
        firmarPKCS7Item.setOnAction((ActionEvent e) -> {
            if (tbvArchivos.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO itemSeleccionado = tbvDispositivos.getSelectionModel().getSelectedItem();
                if (itemSeleccionado == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    if ((tbvArchivos.getItems().get(0)).isRemoto()) {
                        rutaDestino = new File(System.getProperty("java.io.tmpdir"));
                    } else {
                        DirectoryChooser directoryChooser = new DirectoryChooser();
                        directoryChooser.setTitle("Seleccione directorio de destino");
                        rutaDestino = directoryChooser.showDialog(stage);
                    }
                    if (rutaDestino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION,
                                "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        FormFirmante formFirmante = new FormFirmante(stage, itemSeleccionado.getSlot(), Constants.PKCS7);
                        formFirmante.showAndWait();
                        if (formFirmante.getLabel() != null) {
                            new Thread(firmarPKCS7(itemSeleccionado.getSlot(), formFirmante.getLabel(), formFirmante.getPass())).start();
                        }
                    }
                }
            }
        });
        MenuItem firmarXmlItem = new MenuItem("Firmar XML");
        firmarXmlItem.setOnAction((ActionEvent e) -> {
            if (tbvArchivos.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO itemSeleccionado = tbvDispositivos.getSelectionModel().getSelectedItem();
                if (itemSeleccionado == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    rutaDestino = directoryChooser.showDialog(stage);
                    if (rutaDestino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION,
                                "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        FormFirmante formFirmante = new FormFirmante(stage, itemSeleccionado.getSlot(), Constants.DSIG);
                        formFirmante.showAndWait();
                        if (formFirmante.getLabel() != null) {
                            new Thread(firmarXml(itemSeleccionado.getSlot(), formFirmante.getLabel(), formFirmante.getPass(),
                                    formFirmante.getNode(), formFirmante.getForzarEnveloped(), formFirmante.getUsarPrefijo()))
                                    .start();
                        }
                    }
                }
            }
        });
        MenuItem firmarJwsItem = new MenuItem("Firmar JSON");
        firmarJwsItem.setOnAction((ActionEvent e) -> {
            if (tbvArchivos.getItems().isEmpty()) {
                Alert alert = new Alert(AlertType.WARNING, "No se tienen documentos para firmar.", ButtonType.OK);
                alert.initOwner(stage);
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.setTitle("Jacobitus");
                alert.showAndWait();
            } else {
                CK_TOKEN_INFO itemSeleccionado = tbvDispositivos.getSelectionModel().getSelectedItem();
                if (itemSeleccionado == null) {
                    Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione un Token.", ButtonType.OK);
                    alert.initOwner(stage);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                } else {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    rutaDestino = directoryChooser.showDialog(stage);
                    if (rutaDestino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION,
                                "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.initOwner(stage);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        FormFirmante formFirmante = new FormFirmante(stage, itemSeleccionado.getSlot(), Constants.JWS);
                        formFirmante.showAndWait();
                        if (formFirmante.getLabel() != null) {
                            new Thread(firmarJws(itemSeleccionado.getSlot(), formFirmante.getLabel(), formFirmante.getPass())).start();
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
            FormPdf formPdf = new FormPdf(stage);
            formPdf.showAndWait();
            if (formPdf.getPath() != null) {
                tbvArchivos.getItems().add(new ValidadorExtendidoPdf(new File(formPdf.getPath()), this.getConfigValidador()));
            }
        });
        pdfMenu.getItems().addAll(nuevoItem);
        menuBar.getMenus().add(pdfMenu);

        Menu ayudaMenu = new Menu("Ayuda");
        MenuItem servicioItem = new MenuItem("Verificar servicio");
        servicioItem.setOnAction((ActionEvent e) -> {
            new Thread(verificarServicio(version)).start();
        });

        MenuItem acercaDeItem = new MenuItem("Acerca de ...");
        acercaDeItem.setOnAction((ActionEvent e) -> {
            String versionLibreria = bo.firmadigital.jacobitus.Informacion.VERSION;
            String javaVersion = System.getProperty("java.version");
            String javafxVersion = System.getProperty("javafx.version");
            String changePinVersion = new ChangePinJNI().version();
            ImageView logo = new ImageView(
                    new Image(this.getClass().getClassLoader().getResource("logo-agetic.png").toExternalForm()));
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
            alert.setContentText("Agencia de Gobierno Electrónico y Tecnologías de Información y Comunicación");
            alert.setGraphic(logo);
            alert.showAndWait();
        });

        SeparatorMenuItem separador = new SeparatorMenuItem();
        ayudaMenu.getItems().addAll(servicioItem, separador, acercaDeItem);
        menuBar.getMenus().add(ayudaMenu);
        root.setTop(menuBar);

        tokenContextMenu = new ContextMenu();
        MenuItem informacionItem = new MenuItem("Información");
        informacionItem.setOnAction((ActionEvent e) -> {
            FormTokenInfo formTokenInfo = new FormTokenInfo(stage, tokenInfoSeleccionado.getSlot());
            formTokenInfo.showAndWait();
        });
        MenuItem cambiarPinItem = new MenuItem("Cambiar pin");
        cambiarPinItem.setOnAction((ActionEvent e) -> {
            FormCambiarContrasena formCambiarContrasena = new FormCambiarContrasena(stage, tokenInfoSeleccionado.getSlot());
            formCambiarContrasena.showAndWait();
        });
        MenuItem exportarSoftokenItem = new MenuItem("Exportar Softoken");
        exportarSoftokenItem.setOnAction((ActionEvent e) -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Seleccione directorio de destino");
            File destino = directoryChooser.showDialog(stage);
            if (destino != null) {
                try {
                    Files.copy(config.getToken().toPath(), new File(destino, "softoken.p12").toPath(),
                            StandardCopyOption.COPY_ATTRIBUTES);
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
        tokenContextMenu.getItems().addAll(informacionItem, cambiarPinItem, exportarSoftokenItem);

        contextMenu = new ContextMenu();
        MenuItem detalleItem = new MenuItem("Detalle Validación");
        detalleItem.setOnAction((ActionEvent e) -> {
            FormDetalle formDetalle = new FormDetalle(stage, validador, getHostServices());
            formDetalle.showAndWait();
        });
        exportarItem = new MenuItem("Exportar contenido");
        exportarItem.setVisible(false);
        exportarItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exportar archivo");
            File archivoSeleccionado = fileChooser.showSaveDialog(stage);
            if (archivoSeleccionado != null) {
                validador.export(archivoSeleccionado);
            }
        });
        contextMenu.getItems().addAll(detalleItem, exportarItem);

        tbvDispositivos = new TableView();
        TableColumn tbcDispositivo = new TableColumn("Token");
        tbcDispositivo.setCellValueFactory(new PropertyValueFactory("label"));
        tbvDispositivos.getColumns().setAll(tbcDispositivo);
        tbvDispositivos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbvDispositivos.setMaxHeight(76);
        tbvDispositivos.setRowFactory(tv -> {
            TableRow<CK_TOKEN_INFO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.SECONDARY) {
                    tokenInfoSeleccionado = row.getItem();
                    tokenContextMenu.getItems().get(2).setVisible(tokenInfoSeleccionado.getSlot() == -1);
                    tokenContextMenu.show(tbvDispositivos, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });

        tbvArchivos = new TableView();
        TableColumn tbcArchivo = new TableColumn("Archivo");
        tbcArchivo.setCellValueFactory(new PropertyValueFactory("pathValidated"));
        tbvArchivos.getColumns().setAll(tbcArchivo);
        tbvArchivos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tbvArchivos.setRowFactory(tv -> {
            TableRow<ValidadorExtendido> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    HostServices hostServices = getHostServices();
                    hostServices.showDocument(row.getItem().getAbsolutePath());
                }
                if (event.getButton() == MouseButton.SECONDARY) {
                    validador = row.getItem();
                    try {
                        exportarItem.setVisible(MagicBytes.P7S.is(validador.getFile()));
                    } catch (IOException ignore) {
                        exportarItem.setVisible(false);
                    }
                    contextMenu.show(tbvArchivos, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
        tbvArchivos.setOnDragOver((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            } else {
                event.consume();
            }
        });
        tbvArchivos.setOnDragDropped((DragEvent event) -> {
            Dragboard db = event.getDragboard();
            boolean exito = false;
            if (db.hasFiles()) {
                exito = true;
                new Thread(validar(db.getFiles())).start();
            }
            event.setDropCompleted(exito);
            event.consume();
        });

        BorderPane tables = new BorderPane(tbvArchivos);
        BorderPane middle = new BorderPane(tables);
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(middle.widthProperty());

        tables.setTop(tbvDispositivos);

        root.setCenter(middle);
        middle.setTop(progressBar);

        BorderPane footer = new BorderPane();
        footer.setPadding(new Insets(5, 10, 5, 10));

        Label pie = new Label("AGETIC - https://firmadigital.bo");
        pie.setPadding(new Insets(5, 5, 5, 5));
        footer.setRight(pie);

        Platform.runLater(() -> {
            if (actualizacionInfo != null && actualizacionInfo.isActualizacionDisponible()) {
                Hyperlink enlaceActualizacion = new Hyperlink("Hay una actualización disponible");
                enlaceActualizacion.setStyle(
                        "-fx-text-fill: #2a7ae2; -fx-underline: true;");
                enlaceActualizacion.setOnAction(e -> new FormActualizacionDisponible(stage, actualizacionInfo).showAndWait());
                footer.setLeft(enlaceActualizacion);
            }
        });

        footer.setStyle(
                "-fx-border-color: #e0e0e0; " +
                        "-fx-border-width: 1 0 0 0;");
        root.setBottom(footer);

        Scene scene = new Scene(root, 640, 480);
        scene.getStylesheets().add(this.getClass().getClassLoader().getResource("jacobitus.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        if (taskBar && !SistemaOperativoHelper.esDebian()) {
            Platform.setImplicitExit(false);
            if (urlArchivo == null && urlParametro == null) {
                stage.hide();
            }
        } else {
            Platform.runLater(() -> {
                if (actualizacionInfo != null && actualizacionInfo.isActualizacionDisponible()) {
                    new FormActualizacionDisponible(stage, actualizacionInfo).showAndWait();
                    new Thread(listarTokens()).start();
                } else {
                    new Thread(listarTokens()).start();
                }
            });
        }
        stage.setOnCloseRequest((WindowEvent e) -> {
            if (servicio && (!taskBar || taskBarEmulado)) {
                Platform.setImplicitExit(taskBarEmulado);
                try {
                    WebServer.detener();
                } catch (Exception ex) {
                    Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        if (urlArchivo == null) {
            if (urlParametro == null) {
                if (taskBar) {
                    SmartCard.cards(this.getConfigFirmador());
                } else {
                    // new Thread(listarTokens()).start();
                }
            } else {
                File archivo = new File(urlParametro);
                new Thread(validar(Arrays.asList(archivo))).start();
            }
        } else {
            new Thread(descargarArchivo(urlArchivo, tokenAutorizacion, urlRespuesta)).start();
        }
        stage.setOnShown((WindowEvent e) -> {
            Platform.runLater(() -> {
                if (taskBar) {
                    if (actualizacionInfo != null && actualizacionInfo.isActualizacionDisponible()) {
                        new FormActualizacionDisponible(stage, actualizacionInfo).showAndWait();
                        new Thread(listarTokens()).start();
                    } else {
                        new Thread(listarTokens()).start();
                    }
                }
            });
        });
        FormAplicacion.stage = stage;
        FormAplicacion.app = this;
    }

    public Task<Boolean> listarTokens() {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                FormAplicacion.stage.getScene().setCursor(Cursor.WAIT);
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    gestorSlot.setConfigFirmador(getConfigFirmador());
                    Slot[] slots = gestorSlot.listarSlots();
                    List<CK_TOKEN_INFO> listaInfoToken = new LinkedList();
                    for (Slot slot : slots) {
                        listaInfoToken.add(slot.detalleToken());
                    }
                    tbvDispositivos.setItems(FXCollections.observableList(listaInfoToken));
                    updateProgress(100, 100);
                    FormAplicacion.stage.getScene().setCursor(Cursor.DEFAULT);
                    return true;
                } catch (RuntimeException ex) {
                    updateProgress(100, 100);
                    tbvDispositivos.getItems().clear();
                    FormAplicacion.stage.getScene().setCursor(Cursor.DEFAULT);
                    throw ex;
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnFailed((WorkerStateEvent evt) -> {
            String error = task.getException().getMessage();
            Alert alert = new Alert(AlertType.WARNING);
            alert.initOwner(stage);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Jacobitus");
            BorderPane pane = new BorderPane();
            Label label = new Label(error);
            pane.setTop(label);
            alert.getDialogPane().setContent(pane);
            if (error.startsWith("http")) {
                Hyperlink enlace = new Hyperlink(error);
                enlace.setOnAction((ActionEvent t) -> {
                    getHostServices().showDocument(error);
                });
                pane.setCenter(enlace);
                label.setText(
                        "No se encontro el controlador del token, por favor descargue e instale del siguiente link.");
            }
            alert.showAndWait();
            stage.getScene().setCursor(Cursor.DEFAULT);
        });
        return task;
    }

    public Task<Boolean> validar(List<File> archivos) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
                List<ValidadorExtendido> archivosProcesados = new LinkedList();
                for (int i = 0; i < archivos.size(); i++) {
                    if (archivos.get(i).getName().endsWith(".odt")) {
                        archivosProcesados.add(new ValidadorExtendidoPdf(Conversor.odtAPdf(archivos.get(i)), getConfigValidador()));
                    } else if (archivos.get(i).getName().endsWith(".docx")) {
                        archivosProcesados.add(new ValidadorExtendidoPdf(Conversor.docxAPdf(archivos.get(i)), getConfigValidador()));
                    } else if (archivos.get(i).getName().endsWith(".pdf")) {
                        archivosProcesados.add(new ValidadorExtendidoPdf(archivos.get(i), getConfigValidador()));
                        // TODO: Ajustar los mensajes de validacion de documentos xml y json,
                        // considerando que no se puede determinar si fueron firmados dentro del periodo
                        // de vigencia del certificado
                    } else if (archivos.get(i).getName().endsWith(".xml")) {
                        archivosProcesados.add(new ValidadorExtendidoXml(archivos.get(i), null, getConfigValidador()));
                    } else if (archivos.get(i).getName().endsWith(".jws")) {
                        archivosProcesados.add(new ValidadorExtendidoJws(archivos.get(i), null, getConfigValidador()));
                    } else {
                        archivosProcesados.add(new ValidadorExtendidoPKCS7(archivos.get(i), getConfigValidador()));
                    }
                    updateProgress(i + 1, archivos.size());
                }
                tbvArchivos.setItems(FXCollections.observableList(archivosProcesados));
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

    public Task<Boolean> firmarPdf(boolean bloquear, long slotID, String alias, String contrasenia) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setConfigFirmador(getConfigFirmador());
                IToken token = gestorSlot.obtenerSlot(slotID).getToken();
                token.iniciar(contrasenia);
                InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                token.salir();
                if (!ECA.esValida(infoCertificado) || !ECA.esPublica(infoCertificado)) {
                    updateProgress(100, 100);
                    throw new JacobitusException("Certificado no emitido por la ECP.");
                }

                StringBuilder errores = new StringBuilder();
                List<ValidadorExtendido> archivos = tbvArchivos.getItems();
                if (archivos.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    for (int i = 0; i < archivos.size(); i++) {
                        try {
                            IFirmador firmador = FirmadorPdf.getInstance(slotID, alias, contrasenia, getConfigFirmador());
                            String nomArchivo = new File(archivos.get(i).getAbsolutePath()).getName();
                            if (!nomArchivo.endsWith(".pdf")) {
                                nomArchivo += ".firmado.pdf";
                            } else {
                                nomArchivo = nomArchivo.replace(".pdf", ".firmado.pdf");
                            }
                            File archivoFirmado = new File(rutaDestino, nomArchivo);
                            try (InputStream is = new FileInputStream(archivos.get(i).getAbsolutePath());
                                    OutputStream os = new FileOutputStream(archivoFirmado)) {
                                firmador.firmar(is, os, bloquear, false);
                            }
                            updateProgress(i + 1, archivos.size());
                            tbvArchivos.getItems().set(i, new ValidadorExtendidoPdf(archivoFirmado, getConfigValidador()));
                        } catch (PdfException ex) {
                            updateProgress(i + 1, archivos.size());
                            errores.append(archivos.get(i).getAbsolutePath()).append(": ")
                                    .append(ex.getCause().getMessage()).append("\n");
                        } catch (Exception ex) {
                            updateProgress(i + 1, archivos.size());
                            errores.append(archivos.get(i).getAbsolutePath()).append(": ").append(ex.getMessage())
                                    .append("\n");
                        }
                    }
                    updateProgress(100, 100);
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                if (errores.length() == 0) {
                    return true;
                } else {
                    throw new JacobitusException(errores.toString());
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

    public Task<Boolean> validarPKCS7(List<File> archivos) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
                List<ValidadorExtendido> archivosProcesados = new LinkedList();
                for (int i = 0; i < archivos.size(); i++) {
                    if (MagicBytes.PDF.is(archivos.get(i))) {
                        archivosProcesados.add(new ValidadorExtendidoPdf(archivos.get(i), getConfigValidador()));
                    } else {
                        if (MagicBytes.XML.is(archivos.get(i))) {
                            archivosProcesados.add(new ValidadorExtendidoXml(archivos.get(i), null, getConfigValidador()));
                        } else {
                            if (MagicBytes.P7S.is(archivos.get(i))) {
                                archivosProcesados.add(new ValidadorExtendidoPKCS7(archivos.get(i), getConfigValidador()));
                            } else {
                                if (MagicBytes.isJWS(archivos.get(i))) {
                                    archivosProcesados.add(new ValidadorExtendidoJws(archivos.get(i), null, getConfigValidador()));
                                } else {
                                    archivosProcesados.add(new ValidadorExtendidoPKCS7(archivos.get(i), getConfigValidador()));
                                }
                            }
                        }
                    }
                    updateProgress(i + 1, archivos.size());
                }
                tbvArchivos.setItems(FXCollections.observableList(archivosProcesados));
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

    public Task<Boolean> firmarPKCS7(long slotID, String alias, String contrasenia) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setConfigFirmador(getConfigFirmador());
                IToken token = gestorSlot.obtenerSlot(slotID).getToken();
                token.iniciar(contrasenia);
                InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                token.salir();
                if (!ECA.esValida(infoCertificado) || !ECA.esPublica(infoCertificado)) {
                    updateProgress(100, 100);
                    throw new JacobitusException("Certificado no emitido por la ECP.");
                }

                StringBuilder errores = new StringBuilder();
                List<ValidadorExtendido> archivos = tbvArchivos.getItems();
                if (archivos.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    IFirmador firmador = FirmadorPKCS7.getInstance(slotID, alias, contrasenia, getConfigFirmador());
                    for (int i = 0; i < archivos.size(); i++) {
                        try {
                            File archivoFirmado = new File(rutaDestino, archivos.get(i).getFile().getName() + ".p7s");
                            try (InputStream is = new BufferedInputStream(new FileInputStream(archivos.get(i).getFile()));
                                    FileOutputStream os = new FileOutputStream(archivoFirmado)) {
                                firmador.firmar(is, os);
                            }
                            updateProgress(i + 1, archivos.size());
                            tbvArchivos.getItems().set(i, new ValidadorExtendidoPKCS7(archivoFirmado, getConfigValidador()));
                        } catch (Exception ex) {
                            updateProgress(i + 1, archivos.size());
                            errores.append(archivos.get(i).getAbsolutePath()).append(": ").append(ex.getMessage())
                                    .append("\n");
                        }
                    }
                    updateProgress(100, 100);
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                if (errores.length() == 0) {
                    return true;
                } else {
                    throw new JacobitusException(errores.toString());
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

    public Task<Boolean> firmarXml(long slotID, String alias, String contrasenia, String nodoFirma, Boolean forzarEnvoltura,
            Boolean usarPrefijo) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setConfigFirmador(getConfigFirmador());
                IToken token = gestorSlot.obtenerSlot(slotID).getToken();
                token.iniciar(contrasenia);
                InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                token.salir();
                if (!ECA.esValida(infoCertificado) || !ECA.esPublica(infoCertificado)) {
                    updateProgress(100, 100);
                    throw new JacobitusException("Certificado no emitido por la ECP.");
                }

                StringBuilder errores = new StringBuilder();
                List<ValidadorExtendido> archivos = tbvArchivos.getItems();
                if (archivos.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    IFirmador firmador = FirmadorXml.getInstance(slotID, alias, contrasenia, nodoFirma, getConfigFirmador());
                    for (int i = 0; i < archivos.size(); i++) {
                        try {
                            String nomArchivo = new File(archivos.get(i).getAbsolutePath()).getName();
                            if (!nomArchivo.endsWith(".xml")) {
                                nomArchivo += ".firmado.xml";
                            } else {
                                nomArchivo = nomArchivo.replace(".xml", ".firmado.xml");
                            }
                            File archivoFirmado = new File(rutaDestino, nomArchivo);
                            try (InputStream is = new BufferedInputStream(new FileInputStream(archivos.get(i).getFile()));
                                    FileOutputStream os = new FileOutputStream(archivoFirmado)) {
                                firmador.firmar(is, os, forzarEnvoltura, usarPrefijo);
                            }
                            updateProgress(i + 1, archivos.size());
                            tbvArchivos.getItems().set(i, new ValidadorExtendidoXml(archivoFirmado, null, getConfigValidador()));
                        } catch (Exception ex) {
                            updateProgress(i + 1, archivos.size());
                            errores.append(archivos.get(i).getAbsolutePath()).append(": ").append(ex.getMessage())
                                    .append("\n");
                        }
                    }
                    updateProgress(100, 100);
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                if (errores.length() == 0) {
                    return true;
                } else {
                    throw new JacobitusException(errores.toString());
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

    public Task<Boolean> firmarJws(long slotID, String alias, String contrasenia) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);

                GestorSlot gestorSlot = GestorSlot.getInstance();
                gestorSlot.setConfigFirmador(getConfigFirmador());
                IToken token = gestorSlot.obtenerSlot(slotID).getToken();
                token.iniciar(contrasenia);
                InfoCertificado infoCertificado = new InfoCertificado(alias, token.obtenerCertificado(alias));
                token.salir();
                if (!ECA.esValida(infoCertificado) || !ECA.esPublica(infoCertificado)) {
                    updateProgress(100, 100);
                    throw new JacobitusException("Certificado no emitido por la ECP.");
                }

                StringBuilder errores = new StringBuilder();
                List<ValidadorExtendido> archivos = tbvArchivos.getItems();
                if (archivos.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    IFirmador firmador = FirmadorJws.getInstance(slotID, alias, contrasenia, getConfigFirmador());
                    for (int i = 0; i < archivos.size(); i++) {
                        try {
                            String nomArchivo = new File(archivos.get(i).getAbsolutePath()).getName();
                            if (nomArchivo.endsWith(".json")) {
                                nomArchivo = nomArchivo.replace(".json", ".jws");
                            } else {
                                nomArchivo = nomArchivo + ".jws";
                            }
                            File archivoFirmado = new File(rutaDestino, nomArchivo);
                            try (InputStream is = new BufferedInputStream(new FileInputStream(archivos.get(i).getFile()));
                                    FileOutputStream os = new FileOutputStream(archivoFirmado)) {
                                firmador.firmar(is, os, false, false);
                            }
                            updateProgress(i + 1, archivos.size());
                            tbvArchivos.getItems().set(i, new ValidadorExtendidoJws(archivoFirmado, null, getConfigValidador()));
                        } catch (Exception ex) {
                            updateProgress(i + 1, archivos.size());
                            errores.append(archivos.get(i).getAbsolutePath()).append(": ").append(ex.getMessage())
                                    .append("\n");
                        }
                    }
                    updateProgress(100, 100);
                }
                stage.getScene().setCursor(Cursor.DEFAULT);
                if (errores.length() == 0) {
                    return true;
                } else {
                    throw new JacobitusException(errores.toString());
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

    public Task<Boolean> descargarArchivo(String urlArchivo, String tokenAutorizacion, String urlRespuesta) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @SuppressWarnings("unchecked")
            @Override
            protected Boolean call() throws Exception {
                stage.getScene().setCursor(Cursor.WAIT);
                URL url = new URL(urlArchivo);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                if (tokenAutorizacion != null) {
                    connection.setRequestProperty("Authorization", tokenAutorizacion);
                }
                connection.connect();
                int size = 0;
                List contentLength = connection.getHeaderFields().get("content-Length");
                if (contentLength != null && !contentLength.isEmpty()) {
                    String sLength = (String) contentLength.get(0);
                    if (sLength != null) {
                        size = Integer.parseInt(sLength);
                    }
                }
                String nomArchivo = UrlFileName.getFileName(connection);
                if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                        connection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL) {
                    InputStream responseStream = connection.getInputStream();
                    File f = new File(System.getProperty("java.io.tmpdir"), nomArchivo);
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
                    List<ValidadorExtendido> archivosDescargados = new LinkedList();
                    if (MagicBytes.PDF.is(f)) {
                        archivosDescargados.add(new ValidadorExtendidoPdf(f, urlRespuesta, tokenAutorizacion, getConfigValidador()));
                    } else {
                        archivosDescargados.add(new ValidadorExtendidoPKCS7(f, urlRespuesta, tokenAutorizacion, getConfigValidador()));
                    }
                    tbvArchivos.setItems(FXCollections.observableList(archivosDescargados));
                    if (size == 0) {
                        updateProgress(1, 1);
                    }
                    stage.getScene().setCursor(Cursor.DEFAULT);
                    return true;
                } else {
                    throw new JacobitusException("No se pudo descargar el archivo.");
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
                        if (SistemaOperativoHelper.esMacOS()) {
                            FormContrasenaMac contrasena = new FormContrasenaMac(stage);
                            contrasena.showAndWait();
                            FormAplicacion.contraseniaMacOS = contrasena.getPass();
                            if (FormAplicacion.contraseniaMacOS == null) {
                                return;
                            }
                        } else {
                            FormAplicacion.contraseniaMacOS = null;
                        }
                        if (SistemaOperativoHelper.esMacOS()) {
                            certificadoServicioLocalInstalado = CertUtil
                                    .verificarCertificadoServicioLocal(FormAplicacion.contraseniaMacOS);
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

                    ImageView logo = new ImageView(new Image(
                            this.getClass().getClassLoader().getResource("logo-agetic.png").toExternalForm()));
                    StringBuilder sb = new StringBuilder();
                    sb.append("Jacobitus " + version + "\n");
                    sb.append(certificadoServicioLocal);

                    Alert verificarServicioAlerta = new Alert(AlertType.INFORMATION);
                    verificarServicioAlerta.initOwner(stage);
                    verificarServicioAlerta.initModality(Modality.APPLICATION_MODAL);
                    verificarServicioAlerta.setTitle("Verificar servicio");
                    verificarServicioAlerta.setHeaderText(sb.toString());
                    verificarServicioAlerta.setContentText(
                            "Agencia de Gobierno Electrónico y Tecnologías de Información y Comunicación");
                    verificarServicioAlerta.setGraphic(logo);

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
                                    if (SistemaOperativoHelper.esMacOS()) {
                                        CertUtil.desinstalarCertificadoServicioLocal(FormAplicacion.contraseniaMacOS);
                                    } else {
                                        CertUtil.desinstalarCertificadoServicioLocal();
                                    }
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            verificarServicioAlerta.close();
                        });
                        if (errorCertificadoServicioLocalInstalado) {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal);
                        } else {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal,
                                    desinstalarCertificadoServicioLocal);
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
                                    if (SistemaOperativoHelper.esMacOS()) {
                                        CertUtil.instalarCertificadoServicioLocal(FormAplicacion.contraseniaMacOS);
                                    } else {
                                        CertUtil.instalarCertificadoServicioLocal();
                                    }
                                }
                            } catch (IOException | InterruptedException ex) {
                                Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            verificarServicioAlerta.close();
                        });
                        if (errorCertificadoServicioLocalInstalado) {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal);
                        } else {
                            fpCertificadoServicioLocal.getChildren().addAll(lblCertificadoServicioLocal,
                                    instalarCertificadoServicioLocal);
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

    public static void show(File archivo) {
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
            new Thread(app.validar(Arrays.asList(archivo))).start();
        });
    }

    public static void show(String urlArchivo, String tokenAutorizacion, String urlRespuesta) {
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
            new Thread(app.descargarArchivo(urlArchivo, tokenAutorizacion, urlRespuesta)).start();
        });
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulado) {
        FormAplicacion.servicio = servicio;
        FormAplicacion.taskBar = taskBar;
        FormAplicacion.taskBarEmulado = taskBarEmulado;
        if (!FormAplicacion.lanzada) {
            launch();
            FormAplicacion.lanzada = true;
        }
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulado, String urlParametro) {
        FormAplicacion.servicio = servicio;
        FormAplicacion.taskBar = taskBar;
        FormAplicacion.taskBarEmulado = taskBarEmulado;
        FormAplicacion.urlParametro = urlParametro;
        if (!FormAplicacion.lanzada) {
            launch();
            FormAplicacion.lanzada = true;
        }
    }

    public static void run(boolean servicio, boolean taskBar, boolean taskBarEmulado, String urlArchivo, String tokenAutorizacion,
            String urlRespuesta) {
        FormAplicacion.servicio = servicio;
        FormAplicacion.taskBar = taskBar;
        FormAplicacion.taskBarEmulado = taskBarEmulado;
        FormAplicacion.urlArchivo = urlArchivo;
        FormAplicacion.tokenAutorizacion = tokenAutorizacion;
        FormAplicacion.urlRespuesta = urlRespuesta;
        if (!FormAplicacion.lanzada) {
            launch();
            FormAplicacion.lanzada = true;
        }
    }

    public static TokenSelected service(Slot slot, String ci, JSONArray files) {
        if (tokenSelected.isShown()) {
            throw new JacobitusException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(slot);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(files);
            tokenSelected.setFilesJson(null);
            FormService service = new FormService(stage, tokenSelected, "pades");
            service.showAndWait();
            synchronized (tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }

    public static TokenSelected serviceJWS(Slot slot, String ci, JSONArray files) {
        if (tokenSelected.isShown()) {
            throw new JacobitusException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(slot);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(files);
            tokenSelected.setFilesJson(null);
            FormService service = new FormService(stage, tokenSelected, "jws");
            service.showAndWait();
            synchronized (tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }

    public static TokenSelected service(Slot[] slots, String ci, JSONArray pdfs, JSONArray jsons) {
        if (tokenSelected.isShown()) {
            throw new JacobitusException("Ya se tiene una solicitud de firma pendiente.");
        }
        Platform.runLater(() -> {
            tokenSelected.setSlot(null);
            tokenSelected.setSlots(slots);
            tokenSelected.setCI(ci);
            tokenSelected.setFiles(pdfs);
            tokenSelected.setFilesJson(jsons);
            FormService service = new FormService(stage, tokenSelected, "both");
            service.showAndWait();
            synchronized (tokenSelected) {
                tokenSelected.notify();
            }
        });
        tokenSelected.showAndWait();
        return tokenSelected;
    }
}
