/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.firmar.Firmar;
import bo.firmadigital.firmar.FirmarPKCS7;
import bo.firmadigital.firmar.FirmarPdf;
import bo.firmadigital.jacobitus4.util.Converter;
import bo.firmadigital.nss.Chromium;
import bo.firmadigital.nss.Firefox;
import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Slot;
import bo.firmadigital.validar.MagicBytes;
import bo.firmadigital.validar.Validar;
import bo.firmadigital.validar.ValidarPdf;
import bo.firmadigital.validar.ValidarPKCS7;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
    private Validar validar;
    private CK_TOKEN_INFO tokenInfo;
    private static boolean servicio;
    private static boolean taskBar;
    private static String url = null, token, urlPost;
    private static Stage stage;
    private static App app;
    public static final String VERSION = "1.0.0";

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
        MenuItem abrirPKCS7Item = new MenuItem("Abrir PKCS#7");
        abrirPKCS7Item.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir P7S");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos P7S (*.p7s)", "*.p7s");
            fileChooser.getExtensionFilters().add(extFilter);
            List<File> files = fileChooser.showOpenMultipleDialog(stage);
            if (files != null && files.size() > 0) {
                new Thread(validarPKCS7(files)).start();
            }
        });
        MenuItem abrirOtroItem = new MenuItem("Abrir Otro");
        abrirOtroItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir Otro");
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
        MenuItem closeItem = new MenuItem("Cerrar");
        closeItem.setOnAction((ActionEvent e) -> {
            if (servicio && !taskBar) {
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
        mainMenu.getItems().addAll(actualizarItem, abrirItem, abrirPKCS7Item, abrirOtroItem, limpiarItem,opcionesItem, closeItem);
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
                    if (((Validar)tableFile.getItems().get(0)).isRemoto()) {
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
                        Firmante firmante = new Firmante(stage, item.getSlot(), true);
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
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    if (destino == null) {
                        Alert alert = new Alert(AlertType.INFORMATION, "Por favor seleccione la ruta para el documento firmado.", ButtonType.OK);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    } else {
                        Firmante firmante = new Firmante(stage, item.getSlot(), false);
                        firmante.showAndWait();
                        if (firmante.getLabel() != null) {
                            new Thread(firmarPKCS7(item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
                        }
                    }
                }
            }
        });
        firmaMenu.getItems().addAll(firmarItem, firmarPKCS7Item);
        menuBar.getMenus().add(firmaMenu);
        
        Menu pdfMenu = new Menu("PDF");
        MenuItem nuevoItem = new MenuItem("Nuevo");
        nuevoItem.setOnAction((ActionEvent e) -> {
            Pdf pdf = new Pdf(stage);
            pdf.showAndWait();
            if (pdf.getPath() != null) {
                tableFile.getItems().add(new ValidarPdf(new File(pdf.getPath())));
            }
        });
        pdfMenu.getItems().addAll(nuevoItem);
        menuBar.getMenus().add(pdfMenu);
        
        Menu helpMenu = new Menu("Ayuda");
        MenuItem servicioItem = new MenuItem("Verificar servicio");
        servicioItem.setOnAction((ActionEvent e) -> {
            Firefox.registrarCertificado();
            Chromium.registrarCertificado();
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
        contextMenuToken.getItems().addAll(contenidoItem);

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
            TableRow<Validar> row = new TableRow<>();
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
            stage.hide();
        }
        stage.setOnCloseRequest((WindowEvent e) -> {
            if (servicio && !taskBar) {
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
            if (!taskBar) {
                new Thread(listarTokens()).start();
            }
        } else {
            new Thread(download(url, token, urlPost)).start();
        }
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
                    Slot[] slots = gestorSlot.listarSlots();
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
                List<Validar> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (files.get(i).getName().endsWith(".odt")) {
                        certs.add(new ValidarPdf(Converter.odtToPdf(files.get(i))));
                    } else if (files.get(i).getName().endsWith(".docx")) {
                        certs.add(new ValidarPdf(Converter.docxToPdf(files.get(i))));
                    } else {
                        certs.add(new ValidarPdf(files.get(i)));
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
                StringBuilder errores = new StringBuilder();
                List<Validar> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    for (int i = 0; i < files.size(); i++) {
                        InputStream is = new FileInputStream(files.get(i).getAbsolutePath());
                        try {
                            Firmar firmar = FirmarPdf.getInstance(slot, label, pass);
                            String name = new File(files.get(i).getAbsolutePath()).getName();
                            if (!name.endsWith(".pdf")) {
                                name += ".firmado.pdf";
                            } else {
                                name = name.replace(".pdf", ".firmado.pdf");
                            }
                            File out = new File(destino, name);
                            firmar.firmar(is, new FileOutputStream(out), bloquear);
                            updateProgress(i + 1, files.size());
                            tableFile.getItems().set(i, new ValidarPdf(out));
                        } catch (IOException ex) {
                            updateProgress(i + 1, files.size());
                            errores.append(files.get(i).getAbsolutePath()).append(":").append(ex.getMessage()).append("\n");
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
                List<Validar> certs = new LinkedList();
                for (int i = 0; i < files.size(); i++) {
                    if (MagicBytes.PDF.is(files.get(i))) {
                        certs.add(new ValidarPdf(files.get(i)));
                    } else {
                        certs.add(new ValidarPKCS7(files.get(i)));
                    }
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }

    public Task firmarPKCS7(long slot, String label, String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                List<Validar> files = tableFile.getItems();
                if (files.isEmpty()) {
                    updateProgress(100, 100);
                } else {
                    Firmar firmar = FirmarPKCS7.getInstance(slot, label, pass);
                    for (int i = 0; i < files.size(); i++) {
                        FileInputStream is = new FileInputStream(files.get(i).getFile());
                        File out = new File(destino, files.get(i).getFile().getName() + ".p7s");
                        FileOutputStream os = new FileOutputStream(out);
                        firmar.firmar(is, os, files.get(i).getFile().getAbsolutePath().endsWith(".p7s"));
                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidarPKCS7(out));
                    }
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
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
                if (connection.getResponseCode() >= HttpURLConnection.HTTP_OK &&
                        connection.getResponseCode() <= HttpURLConnection.HTTP_PARTIAL) {
                    InputStream responseStream = connection.getInputStream();
                    File f = new File(System.getProperty("java.io.tmpdir"), "prueba.pdf");
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
                    List<Validar> certs = new LinkedList();
                    certs.add(new ValidarPdf(f, urlPost, token));
                    tableFile.setItems(FXCollections.observableList(certs));
                    if (size == 0) {
                        updateProgress(1, 1);
                    }
                    return true;
                } else {
                    Alert alert = new Alert(AlertType.ERROR, "No se pudo descargar el archivo.", ButtonType.OK);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                    return false;
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
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
        Alert alert = new Alert(AlertType.ERROR, error, ButtonType.OK);
        alert.setTitle("Jacobitus");
        alert.showAndWait();
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

    public static void run(boolean servicio, boolean taskBar) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        launch();
    }

    public static void run(boolean servicio, boolean taskBar, String url, String token, String urlPost) {
        App.servicio = servicio;
        App.taskBar = taskBar;
        App.url = url;
        App.token = token;
        App.urlPost = urlPost;
        launch();
    }
}
