/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.exceptions.UnsupportedPdfException;
import com.itextpdf.text.pdf.BadPdfFormatException;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
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
import javax.imageio.ImageIO;

/**
 *
 * @author ADSIB
 */
public class Pdf extends Stage {
    private static int WIDTH = 800;
    private static int HEIGHT = 1035;
    private ProgressBar progressBar;
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
                    document = new Document();
                    document.setPageSize(new Rectangle(WIDTH, HEIGHT));
                    document.setMargins(0, 0, 0, 0);
                    int c = 1;
                    do {
                        out = new File(System.getProperty("java.io.tmpdir"), "documento" + c + ".pdf");
                        c++;
                    } while (out.exists());
                    writer = PdfWriter.getInstance(document, new FileOutputStream(out));
                    document.open();
                    ObservableList<String> list = FXCollections.observableArrayList();
                    PdfReader reader = new PdfReader(file);
                    for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                        if (!getItem().equals("Página " + i)) {
                            document.newPage();
                            Image image = Image.getInstance(writer.getImportedPage(reader, i));
                            image.scaleAbsolute(new Rectangle(WIDTH, HEIGHT));
                            document.add(image);
                            list.add("Página " + (list.size() + 1));
                        }
                    }
                    //reader.close();
                    getListView().setItems(list);
                    new File(file).delete();
                } catch (BadPdfFormatException | IOException | BadElementException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DocumentException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public Pdf(Stage parent) {
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
            File file = fileChooser.showOpenDialog(Pdf.this);
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
        document = new Document();
        document.setPageSize(new Rectangle(WIDTH, HEIGHT));
        document.setMargins(0, 0, 0, 0);
        try {
            writer = PdfWriter.getInstance(document, new FileOutputStream(out));
            document.open();
        } catch (DocumentException | FileNotFoundException ex) {
            Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Task insertarPdf(File file) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    PdfReader reader = new PdfReader(file.getAbsolutePath());
                    for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                        document.newPage();
                        PdfDictionary dict = reader.getPageResources(i);
                        PdfArray set = dict.getAsArray(PdfName.PROCSET);
                        PdfDictionary xobjects = dict.getAsDict(PdfName.XOBJECT);
                        Image image;
                        if (xobjects != null && xobjects.getKeys().size() == 1 && (set.contains(PdfName.IMAGE) || set.contains(PdfName.IMAGEB) || set.contains(PdfName.IMAGEC) || set.contains(PdfName.IMAGEI))) {
                            PdfName imgName = xobjects.getKeys().iterator().next();
                            PRStream imgStream = (PRStream)xobjects.getDirectObject(imgName);
                            byte[] b;
                            try {
                                b = PdfReader.getStreamBytes(imgStream);
                            } catch(UnsupportedPdfException ignore) {
                                b = PdfReader.getStreamBytesRaw(imgStream);
                            }
                            try (FileOutputStream fos = new FileOutputStream("/tmp/image.jpg")) {
                                fos.write(b);
                            }
                            javafx.scene.image.Image img = new javafx.scene.image.Image(new ByteArrayInputStream(b), WIDTH * 1.5, HEIGHT * 1.5, false, true);
                            if (img.getException() == null) {
                                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", baos);
                                    image = Image.getInstance(baos.toByteArray(), true);
                                }
                            } else {
                                image = Image.getInstance(writer.getImportedPage(reader, i));
                            }
                        } else {
                            image = Image.getInstance(writer.getImportedPage(reader, i));
                        }
                        /*float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin()) / image.getWidth()) * 100;
                        image.scalePercent(scaler);*/
                        image.scaleAbsolute(new Rectangle(WIDTH, HEIGHT));
                        document.add(image);
                        updateProgress(i + 1, reader.getNumberOfPages());
                        lv.getItems().add("Página " + (lv.getItems().size() + 1));
                    }
                    //reader.close();
                } catch (BadPdfFormatException | IOException | BadElementException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DocumentException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }
    
    public Task insertarImagen(File file) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    document.newPage();
                    FileInputStream imgFile = new FileInputStream(file.getAbsolutePath());
                    System.out.println(imgFile);
                    javafx.scene.image.Image img = new javafx.scene.image.Image(imgFile, WIDTH * 1.5, HEIGHT * 1.5, false, true);
                    Image image;
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(SwingFXUtils.fromFXImage(img, null), "jpg", baos);
                        image = Image.getInstance(baos.toByteArray(), true);
                    }
                    //Image image = Image.getInstance(file.getAbsolutePath(), true);
                    /*float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin()) / image.getWidth()) * 100;
                    image.scalePercent(scaler);*/
                    image.scaleAbsolute(new Rectangle(WIDTH, HEIGHT));
                    document.add(image);
                    updateProgress(1, 1);
                    lv.getItems().add("Página " + (lv.getItems().size() + 1));
                } catch (BadElementException | IOException | BadPdfFormatException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                } catch (DocumentException ex) {
                    Logger.getLogger(Pdf.class.getName()).log(Level.SEVERE, null, ex);
                }
                return true;
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }

    public void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle("Jacobitus");
        alert.showAndWait();
    }
    
    public void config() {
        Alert alert = new Alert(Alert.AlertType.NONE);
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
        document.setPageSize(new Rectangle(WIDTH, HEIGHT));
    }

    public String getPath() {
        return path;
    }
}
