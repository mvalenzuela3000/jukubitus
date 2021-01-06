/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.token.ExternalSignatureLocal;
import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Slot;
import bo.firmadigital.token.Token;
import bo.firmadigital.validar.Validar;
import bo.firmadigital.validar.ValidarPdf;
import bo.firmadigital.validar.ValidarPKCS7;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSigLockDictionary;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.ExternalBlankSignatureContainer;
import com.itextpdf.text.pdf.security.ExternalSignatureContainer;
import com.itextpdf.text.pdf.security.MakeSignature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableFile;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;

/**
 *
 * @author ADSIB
 */
public class App extends Application {
    private ProgressBar progressBar;
    private TableView table;
    private TableView tableFile;
    private File destino;
    private static boolean servicio;

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
        MenuItem closeItem = new MenuItem("Salir");
        closeItem.setOnAction((ActionEvent e) -> {
            try {
                Main.jettyServer.stop();
                Main.jettyServer.destroy();
                stage.close();
            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        mainMenu.getItems().addAll(actualizarItem, abrirItem, abrirPKCS7Item, abrirOtroItem, limpiarItem, closeItem);
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
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Seleccione directorio de destino");
                    destino = directoryChooser.showDialog(stage);
                    Firmante firmante = new Firmante(stage, item.getSlot());
                    firmante.showAndWait();
                    if (firmante.getLabel() != null) {
                        new Thread(firmar(false, item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
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
                    Firmante firmante = new Firmante(stage, item.getSlot());
                    firmante.showAndWait();
                    if (firmante.getLabel() != null) {
                        new Thread(firmarPKCS7(item.getSlot(), firmante.getLabel(), firmante.getPass())).start();
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
        MenuItem aboutItem = new MenuItem("Acerca de ...");
        aboutItem.setOnAction((ActionEvent e) -> {
            Alert alert = new Alert(AlertType.NONE, "Jacobitus Total, JavaFX " + javafxVersion + ", con Java " + javaVersion + ".", ButtonType.OK);
            alert.showAndWait();
        });
        helpMenu.getItems().addAll(aboutItem);
        menuBar.getMenus().add(helpMenu);
        root.setTop(menuBar);

        table = new TableView();
        TableColumn tokenCol = new TableColumn("Token");
        tokenCol.setCellValueFactory(new PropertyValueFactory("label"));
        table.getColumns().setAll(tokenCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setMaxHeight(76);

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
            });
            return row;
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
        stage.setOnCloseRequest((WindowEvent e) -> {
            try {
                Main.jettyServer.stop();
                Main.jettyServer.destroy();
            } catch (Exception ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        new Thread(listarTokens()).start();
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
                    certs.add(new ValidarPdf(files.get(i)));
                    updateProgress(i + 1, files.size());
                }
                tableFile.setItems(FXCollections.observableList(certs));
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
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
                            PdfReader reader = new PdfReader(is);
                            ArrayList<String> signatures = reader.getAcroFields().getSignatureNames();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();

                            if (bloquear) {
                                PdfStamper stp = new PdfStamper(reader, baos, '\0', true);
                                PdfFormField field = PdfFormField.createSignature(stp.getWriter());
                                field.setFieldName("Signature " + (signatures.size() + 1));
                                PdfSigLockDictionary lock = new PdfSigLockDictionary(PdfSigLockDictionary.LockPermissions.NO_CHANGES_ALLOWED);
                                field.put(PdfName.LOCK, stp.getWriter().addToBody(lock).getIndirectReference());
                                field.setWidget(new Rectangle(0, 0, 0, 0), PdfAnnotation.HIGHLIGHT_NONE);
                                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                                stp.addAnnotation(field, 1);
                                stp.close();
                                reader.close();
                                reader = new PdfReader(new ByteArrayInputStream(baos.toByteArray()));
                                baos = new ByteArrayOutputStream();
                            }
                            PdfStamper stamper = PdfStamper.createSignature(reader, baos, '\0', null, true);
                            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
                            if (bloquear) {
                                appearance.setVisibleSignature("Signature " + (signatures.size() + 1));
                                AcroFields form = stamper.getAcroFields();
                                form.setFieldProperty("Signature " + (signatures.size() + 1), "setfflags", PdfFormField.FF_READ_ONLY, null);
                            } else {
                                appearance.setVisibleSignature(new Rectangle(0, 0, 0, 0), 1, "Signature " + (signatures.size() + 1));
                            }
                            ExternalSignatureContainer external = new ExternalBlankSignatureContainer(PdfName.ADOBE_PPKLITE, PdfName.ADBE_PKCS7_DETACHED);
                            MakeSignature.signExternalContainer(appearance, external, 8192);
                            stamper.flush();
                            stamper.close();
                            reader.close();

                            String name = new File(files.get(i).getAbsolutePath()).getName();
                            if (!name.endsWith(".pdf")) {
                                name += ".firmado.pdf";
                            } else {
                                name = name.replace(".pdf", ".firmado.pdf");
                            }
                            File out = new File(destino, name);
                            PdfReader reader2 = new PdfReader(new ByteArrayInputStream(baos.toByteArray()));
                            FileOutputStream os2 = new FileOutputStream(out);
                            ExternalSignatureContainer external2 = new ExternalSignatureLocal(slot, label, pass);
                            MakeSignature.signDeferred(reader2, "Signature " + (signatures.size() + 1), os2, external2);
                            os2.close();
                            updateProgress(i + 1, files.size());
                            tableFile.getItems().set(i, new ValidarPdf(out));
                        } catch (DocumentException | IOException | GeneralSecurityException ex) {
                            updateProgress(i + 1, files.size());
                            errores.append(files.get(i).getAbsolutePath()).append(":").append(ex.getMessage()).append("\n");
                        }
                    }
                }
                if (errores.isEmpty()) {
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
                    certs.add(new ValidarPKCS7(files.get(i)));
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
                    if (Security.getProvider("BC") == null) {
                        Security.addProvider(new BouncyCastleProvider());
                    }
                    Token token = GestorSlot.getInstance().obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    PrivateKey privateKey = token.obtenerClavePrivada(label);
                    X509Certificate x509Certificate = token.obtenerCertificado(label);
                    for (int i = 0; i < files.size(); i++) {
                        List<Certificate> certlist = new ArrayList<>();
                        certlist.add(x509Certificate);
                        Store certstore = new JcaCertStore(certlist);
                        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);
                        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
                        generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().setProvider("BC").
                                build()).build(signer, (X509Certificate) x509Certificate));
                        generator.addCertificates(certstore);

                        CMSTypedData cmsdata;
                        if (files.get(i).getFile().getAbsolutePath().endsWith(".p7s")) {
                            try (InputStream is = new FileInputStream(files.get(i).getFile())) {
                                CMSSignedData signedData = new CMSSignedData(is);
                                cmsdata = signedData.getSignedContent();
                                generator.addSigners(signedData.getSignerInfos());
                            }
                        } else {
                            cmsdata = new CMSProcessableFile(files.get(i).getFile());
                        }
                        CMSSignedData signeddata = generator.generate(cmsdata, true);
                        String name = new File(files.get(i).getAbsolutePath()).getName();
                        File out = new File(destino, name + ".p7s");
                        new FileOutputStream(out).write(signeddata.getEncoded());

                        updateProgress(i + 1, files.size());
                        tableFile.getItems().set(i, new ValidarPKCS7(out));
                    }
                    token.salir();
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }

    public static void run(boolean servicio) {
        App.servicio = servicio;
        launch();
    }
}
