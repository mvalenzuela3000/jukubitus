/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus.validador.CertDate;
import bo.firmadigital.jacobitus.validador.DetalleValidacion;
import bo.firmadigital.jacobitus.validador.Validador;
import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.components.TreeItemBlocked;
import javafx.application.HostServices;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author ADSIB
 */
public class Detalle extends Stage {
    private String pass;

    @SuppressWarnings("unchecked")
    public Detalle(Stage parent, Validador validar, HostServices hostServices) {
        setTitle("Detalle de firmas");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        TreeItem<String> rootItem = new TreeItem<>(validar.getAbsolutePath());
        rootItem.setExpanded(true);
        for (CertDate cert : validar) {
            DetalleValidacion detalleValidacion = new DetalleValidacion(cert);
            TreeItem<String> item;
            item = new TreeItemBlocked<>(detalleValidacion.getCertificadoTitular() + detalleValidacion.getCertificadoSelladoTiempo(), new ImageView(this.obtenerIcono(detalleValidacion.getCertificadoValidacion(), "NORMAL")), cert);
            TreeItem<String> intItem, intItemDetalle;
            intItem = new TreeItem<>(detalleValidacion.getDocumentoEstado(), new ImageView(this.obtenerIcono(detalleValidacion.getDocumentoValidacion(), "PEQUENIO")));
            intItemDetalle = new TreeItem<>(detalleValidacion.getDocumentoDescripcion());
            intItem.getChildren().add(intItemDetalle);
            item.getChildren().add(intItem);
            TreeItem<String> pkiItem, pkiItemDetalle;
            pkiItem = new TreeItem<>(detalleValidacion.getCadenaConfianzaEstado(), new ImageView(this.obtenerIcono(detalleValidacion.getCadenaConfianzaValidacion(), "PEQUENIO")));
            pkiItemDetalle = new TreeItem<>(detalleValidacion.getCadenaConfianzaDescripcion());
            pkiItem.getChildren().add(pkiItemDetalle);
            item.getChildren().add(pkiItem);
            TreeItem<String> vigItem, vigItemDetalle;
            vigItem = new TreeItem<>(detalleValidacion.getPeriodoValidezEstado(), new ImageView(this.obtenerIcono(detalleValidacion.getPeriodoValidezValidacion(), "PEQUENIO")));
            vigItemDetalle = new TreeItem<>(detalleValidacion.getPeriodoValidezDescripcion());
            vigItem.getChildren().add(vigItemDetalle);
            item.getChildren().add(vigItem);
            TreeItem<String> ocspItem, ocspItemDetalle;
            ocspItem = new TreeItem<>(detalleValidacion.getRevocacionEstado(), new ImageView(this.obtenerIcono(detalleValidacion.getRevocacionValidacion(), "PEQUENIO")));
            ocspItemDetalle = new TreeItem<>(detalleValidacion.getRevocacionDescripcion());
            ocspItem.getChildren().add(ocspItemDetalle);
            item.getChildren().add(ocspItem);
            item.setExpanded(true);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        tree.setCellFactory(param -> new TextFieldTreeCell<String>() {
            @SuppressWarnings("rawtypes")
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setText(item);
                    if (getTreeItem() instanceof TreeItemBlocked) {
                        CertDate certDate = ((TreeItemBlocked)getTreeItem()).getCertDate();
                        if (certDate.isBloquea()) {
                            setTextFill(Color.BLUE);
                            //setStyle("-fx-text-fill: blue;");
                        } else {
                            setTextFill(Color.BLACK);
                        }
                        CertInformation pane = new CertInformation(certDate);
                        Tooltip tooltip = new Tooltip();
                        tooltip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        tooltip.setGraphic(pane);
                        setTooltip(tooltip);
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                hostServices.showDocument(validar.getRevisionPath(certDate.getName()));
                            }
                        });
                    } else {
                        setTextFill(Color.BLACK);
                    }
                    setGraphic(getTreeItem().getGraphic());
                }
            }
        });
        StackPane root = new StackPane();
        root.getChildren().add(tree);
        Scene scene = new Scene(root, 540, 380);
        setScene(scene);
    }

    private Image obtenerIcono(String tipo, String tamanio) {
        switch (tipo) {
            case "EXITO":
                switch (tamanio) {
                    case "NORMAL":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("ok.png"));        
                    case "PEQUENIO":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("valid.png"));    
                }
                break;
            case "PRECAUCION":
                switch (tamanio) {
                    case "NORMAL":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("no_no.png"));        
                    case "PEQUENIO":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("alert.png"));                    
                }
                break;
            case "ERROR":
                switch (tamanio) {
                    case "NORMAL":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("no_ok.png"));        
                    case "PEQUENIO":
                        return new Image(this.getClass().getClassLoader().getResourceAsStream("error.png"));    
                }
                break;
        }
        return null;
    }

    public String getPass() {
        return pass;
    }
}
