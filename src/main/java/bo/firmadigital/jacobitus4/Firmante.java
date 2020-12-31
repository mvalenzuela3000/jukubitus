/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.token.GestorSlot;
import bo.firmadigital.token.Token;
import bo.firmadigital.validar.DatosCertificado;
import java.util.LinkedList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author ADSIB
 */
public class Firmante extends Stage {
    private final ProgressBar progressBar;
    private final TableView table;
    private final long slot;
    private String label;
    private String pass;

    public Firmante(Stage parent, long slot) {
        this.slot = slot;
        this.label = null;
        setTitle("Seleccione el certificado a utilizar para la firma");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        BorderPane root = new BorderPane();
        progressBar = new ProgressBar();
        progressBar.prefWidthProperty().bind(root.widthProperty());
        root.setTop(progressBar);
        table = new TableView();
        TableColumn tokenCol = new TableColumn("Certificado");
        tokenCol.setCellValueFactory(new PropertyValueFactory("label"));
        TableColumn nombreCol = new TableColumn("Signatario");
        nombreCol.setCellValueFactory(new PropertyValueFactory("nombreComunSubject"));
        TableColumn descCol = new TableColumn("Descripcion");
        descCol.setCellValueFactory(new PropertyValueFactory("descripcionSubject"));
        table.getColumns().setAll(tokenCol, nombreCol, descCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        root.setCenter(table);
        Scene scene = new Scene(root, 560, 260);
        setScene(scene);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            label = ((DatosCertificado)newSelection).getLabel();
            close();
        });

        setOnShown((WindowEvent t) -> {
            Contrasena contrasena = new Contrasena(Firmante.this);
            contrasena.showAndWait();
            if (contrasena.getPass() == null) {
                close();
            } else {
                pass = contrasena.getPass();
                new Thread(listarCertificados(contrasena.getPass())).start();
            }
        });
    }

    public String getLabel() {
        return label;
    }

    public String getPass() {
        return pass;
    }

    public Task listarCertificados(String pass) {
        progressBar.progressProperty().unbind();
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    GestorSlot gestorSlot = GestorSlot.getInstance();
                    Token token = gestorSlot.obtenerSlot(slot).getToken();
                    token.iniciar(pass);
                    List<String> labels = token.listarIdentificadorClaves();
                    List<DatosCertificado> certificados = new LinkedList<>();
                    for (String label : labels) {
                        certificados.add(new DatosCertificado(label, token.obtenerCertificado(label)));
                    }
                    token.salir();
                    table.setItems(FXCollections.observableList(certificados));
                    updateProgress(100, 100);
                    return true;
                } catch (RuntimeException ex) {
                    throw ex;
                }
            }
        };
        progressBar.progressProperty().bind(task.progressProperty());
        return task;
    }
}
