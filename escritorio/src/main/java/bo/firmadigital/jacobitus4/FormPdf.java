/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.AreaBreakType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class FormPdf extends Stage {
    private static int WIDTH = 800;
    private static int HEIGHT = 1035;
    private ProgressBar progressBar;
    private static PdfDocument pdfDocument;
    private static Document document;
    private static PdfWriter writer;
    private static File out;
    private String path;
    private ListView<String> lv;

    static class XCell extends ListCell<String> {
        HBox hbox = new HBox();
        Label label = new Label("");
        Pane pane = new Pane();
        ImageView imageView = new ImageView(new javafx.scene.image.Image(pane.getClass().getClassLoader().getResourceAsStream("dustbin.png")));
        Button button = new Button("", imageView);
        /*ImageView imageViewUp = new ImageView(new javafx.scene.image.Image(pane.getClass().getClassLoader().getResourceAsStream("up-arrow.png")));
        Button buttonUp = new Button("", imageViewUp);
        ImageView imageViewDown = new ImageView(new javafx.scene.image.Image(pane.getClass().getClassLoader().getResourceAsStream("down-arrow.png")));
        Button buttonDown = new Button("", imageViewDown);*/

        public XCell() {
            super();

            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            button.setOnAction(event -> {
                try {
                    document.close();
                    String file = out.getAbsolutePath();
                    document.setMargins(0, 0, 0, 0);
                    int c = 1;
                    do {
                        out = new File(System.getProperty("java.io.tmpdir"), "documento" + c + ".pdf");
                        c++;
                    } while (out.exists());
                    writer = new PdfWriter(new FileOutputStream(out));
                    pdfDocument = new PdfDocument(writer);
                    document = new Document(pdfDocument, new PageSize(WIDTH, HEIGHT));
                    ObservableList<String> list = FXCollections.observableArrayList();
                    PdfReader reader = new PdfReader(file);
                    PdfDocument pdf = new PdfDocument(reader);
                    for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                        if (!getItem().equals("Página " + i)) {
                            pdf.copyPagesTo(i, i, pdfDocument);
                            list.add("Página " + (list.size() + 1));
                        }
                    }
                    getListView().setItems(list);
                    new File(file).delete();
                } catch (IOException ex) {
                    Logger.getLogger(FormPdf.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);
            setGraphic(null);

            if (item != null && !empty) {
                label.setText(item);
                setGraphic(hbox);
            }
        }
    }
    
    public FormPdf(Stage parent) {
        setTitle("Paginas del PDF");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        MenuBar menuBar = new MenuBar();
        Menu mainMenu = new Menu("Archivo");
        MenuItem agregarPdfItem = new MenuItem("Agregar PDF");
        agregarPdfItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir PDF");
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showOpenDialog(FormPdf.this);
            if (file != null) {
                new Thread(insertarPdf(file)).start();
            }
        });
        MenuItem agregarImagenItem = new MenuItem("Agregar Imagen");
        agregarImagenItem.setOnAction((ActionEvent e) -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Abrir Imagen");
            FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("Imagen (*.jpg)", "*.jpg");
            FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("Imagen (*.png)", "*.png");
            FileChooser.ExtensionFilter tiffFilter = new FileChooser.ExtensionFilter("Imagen (*.tiff)", "*.tiff");
            FileChooser.ExtensionFilter bmpFilter = new FileChooser.ExtensionFilter("Imagen (*.bmp)", "*.bmp");
            fileChooser.getExtensionFilters().addAll(jpgFilter, pngFilter, tiffFilter, bmpFilter);
            File file = fileChooser.showOpenDialog(this);
            if (file != null) {
                new Thread(insertarImagen(file)).start();
            }
        });
        MenuItem configurarItem = new MenuItem("Tamaño de página");
        configurarItem.setOnAction((ActionEvent e) -> {
            config();
        });
        MenuItem guardarItem = new MenuItem("Guardar");
        guardarItem.setOnAction((ActionEvent e) -> {
            document.close();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos PDF (*.pdf)", "*.pdf");
            fileChooser.getExtensionFilters().add(extFilter);
            File file = fileChooser.showSaveDialog(this);
            if (!file.getName().endsWith(".pdf")) {
                file = new File(file.getPath() + ".pdf");
            }
            try {
                Files.copy(Paths.get(out.getPath()), Paths.get(file.getPath()), StandardCopyOption.REPLACE_EXISTING);
                path = file.getPath();
                close();
            } catch (IOException ex) {
                error(ex.getMessage());
            }
        });
        mainMenu.getItems().addAll(agregarPdfItem, agregarImagenItem, configurarItem, guardarItem);
        menuBar.getMenus().add(mainMenu);
        root.setTop(menuBar);

        ObservableList<String> list = FXCollections.observableArrayList();
        lv = new ListView<>(list);
        lv.setCellFactory(param -> new XCell());
        BorderPane progress = new BorderPane();
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(progress.widthProperty());
        progress.setTop(progressBar);
        progress.setCenter(lv);
        root.setCenter(progress);

        Scene scene = new Scene(root, 460, 260);
        setScene(scene);

        out = new File(System.getProperty("java.io.tmpdir"), "documento.pdf");
        int c = 1;
        while (out.exists()) {
            out = new File(System.getProperty("java.io.tmpdir"), "documento" + c + ".pdf");
            c++;
        }
        try {
            writer = new PdfWriter(out);
            pdfDocument = new PdfDocument(writer);
            document = new Document(pdfDocument, new PageSize(WIDTH, HEIGHT));
            document.setMargins(0, 0, 0, 0);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FormPdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Task<Boolean> insertarPdf(File file) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    PdfReader reader = new PdfReader(file.getAbsolutePath());
                    PdfDocument pdf = new PdfDocument(reader);
                    for (int i = 1; i <= pdf.getNumberOfPages(); i++) {
                        String text = PdfTextExtractor.getTextFromPage(pdf.getPage(i));
                        PdfDictionary dict = pdf.getPage(i).getPdfObject().getAsDictionary(PdfName.Resources);
                        PdfArray set = dict.getAsArray(PdfName.ProcSet);
                        PdfDictionary xobjects = dict.getAsDictionary(PdfName.XObject);
                        Image image;
                        if (xobjects != null && xobjects.keySet().size() == 1 &&
                                text.equals("") &&
                                (dict.containsKey(PdfName.ExtGState) ||
                                (set != null && (set.contains(PdfName.Image) ||
                                set.contains(new PdfName("ImageA")) ||
                                set.contains(new PdfName("ImageB")) ||
                                set.contains(PdfName.ImageMask))))) {
                            PdfName imgName = xobjects.keySet().iterator().next();
                            PdfStream imgStream = xobjects.getAsStream(imgName);
                            byte[] b;
                            try {
                                b = imgStream.getBytes();
                            } catch(PdfException ignore) {
                                b = imgStream.getBytes(false);
                            }
                            try (FileOutputStream fos = new FileOutputStream("/tmp/image.jpg")) {
                                fos.write(b);
                            }
                            javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(b), WIDTH * 1.5, HEIGHT * 1.5, false, true);
                            if (img.getException() == null) {
                                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", baos);
                                    ImageData imageData = ImageDataFactory.create(baos.toByteArray());
                                    image = new Image(imageData);
                                    image.scaleAbsolute(WIDTH, HEIGHT);
                                    if (pdfDocument.getNumberOfPages() > 0) {
                                        document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
                                        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                                    }
                                    document.add(image);
                                }
                            } else {
                                pdf.copyPagesTo(i, i, pdfDocument);
                            }
                        } else {
                            pdf.copyPagesTo(i, i, pdfDocument);
                        }
                        updateProgress(i + 1, pdfDocument.getNumberOfPages());
                        Platform.runLater(() -> {
                            lv.getItems().add("Página " + (lv.getItems().size() + 1));
                        });
                    }
                } catch (IOException ex) {
                    Logger.getLogger(FormPdf.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }
    
    public Task<Boolean> insertarImagen(File file) {
        progressBar.progressProperty().unbind();
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                try {
                    if (lv.getItems().size() > 0) {
                        document.add(new AreaBreak(AreaBreakType.LAST_PAGE));
                        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                    }
                    FileInputStream imgFile = new FileInputStream(file.getAbsolutePath());
                    javafx.scene.image.Image img = new javafx.scene.image.Image(imgFile, WIDTH * 1.5, HEIGHT * 1.5, false, true);
                    Image image;
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", baos);
                        ImageData imageData = ImageDataFactory.create(baos.toByteArray());
                        image = new Image(imageData);
                    }
                    image.scaleAbsolute(WIDTH, HEIGHT);
                    document.add(image);
                    updateProgress(1, 1);
                    Platform.runLater(() -> {
                        lv.getItems().add("Página " + (lv.getItems().size() + 1));
                    });
                } catch (IOException ex) {
                    Logger.getLogger(FormPdf.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }

    public void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.initOwner(this);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Jacobitus");
        alert.showAndWait();
    }
    
    public void config() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.initOwner(this);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setTitle("Jacobitus");
        GridPane content = new GridPane();
        Label labelAncho = new Label("Ancho:");
        TextField textAncho = new TextField(String.valueOf(WIDTH));
        content.add(labelAncho, 0, 0);
        content.add(textAncho, 1, 0);
        Label labelAlto = new Label("Alto:");
        TextField textAlto = new TextField(String.valueOf(HEIGHT));
        content.add(labelAlto, 0, 1);
        content.add(textAlto, 1, 1);
        alert.getDialogPane().getButtonTypes().add(ButtonType.OK);
        ((Button)alert.getDialogPane().lookupButton(ButtonType.OK)).setOnAction((ActionEvent t) -> {
            WIDTH = Integer.parseInt(textAncho.getText());
            HEIGHT = Integer.parseInt(textAlto.getText());
            alert.close();
        });
        alert.getDialogPane().setContent(content);
        alert.showAndWait();
        pdfDocument.setDefaultPageSize(new PageSize(WIDTH, HEIGHT));
    }

    public String getPath() {
        return path;
    }
}
