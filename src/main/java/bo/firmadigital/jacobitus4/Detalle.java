/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.components.CertInformation;
import bo.firmadigital.jacobitus4.components.TreeItemBlocked;
import bo.firmadigital.validar.CertDate;
import bo.firmadigital.validar.Validar;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
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
    private final Image okIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("ok.png"));
    private final Image alertIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("no_no.png"));
    private final Image errorIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("no_ok.png"));
    private final Image okSmallIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("valid.png"));
    private final Image alertSmallIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("alert.png"));
    private final Image errorSmallIcon = new Image(this.getClass().getClassLoader().getResourceAsStream("error.png"));

    public Detalle(Stage parent, Validar validar, HostServices hostServices) {
        setTitle("Detalle de firmas");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        TreeItem<String> rootItem = new TreeItem<>(validar.getAbsolutePath());
        rootItem.setExpanded(true);
        for (CertDate cert : validar) {
            TreeItem<String> item;
            if (cert.isOk()) {
                if (cert.isAlerted()) {
                    item = new TreeItemBlocked<>(cert.getDatos().getNombreComunSubject(), new ImageView(alertIcon), cert);
                } else {
                    item = new TreeItemBlocked<>(cert.getDatos().getNombreComunSubject(), new ImageView(okIcon), cert);
                }
            } else {
                item = new TreeItemBlocked<>(cert.getDatos().getNombreComunSubject(), new ImageView(errorIcon), cert);
            }
            TreeItem<String> intItem, intItemDet;
            if (cert.isValid()) {
                if (cert.isValidAlerted()) {
                    intItem = new TreeItem<>("Documento modificado", new ImageView(alertSmallIcon));
                    switch (cert.getValidAdd()) {
                        case widget_firma_agregado:
                            intItemDet = new TreeItem<>("Se agregaron firmas posteriormente a esta firma");
                            break;
                        case widget_otro_agregado:
                            intItemDet = new TreeItem<>("Se agregaron widgets posteriormente a esta firma");
                            break;
                        default:
                            intItemDet = new TreeItem<>("Se modificó el contenido de widgets posteriormente a esta firma");
                            break;
                    }
                } else {
                    intItem = new TreeItem<>("Documento auténtico", new ImageView(okSmallIcon));
                    intItemDet = new TreeItem<>("El documento no ha sido modificado después de la firma");
                }
            } else {
                intItem = new TreeItem<>("Documento modificado", new ImageView(errorSmallIcon));
                intItemDet = new TreeItem<>("El documento ha sido modificado después de la firma");
            }
            intItem.getChildren().add(intItemDet);
            item.getChildren().add(intItem);
            TreeItem<String> pkiItem, pkiItemDet;
            if (cert.isPKI()) {
                pkiItem = new TreeItem<>("Cadena de confianza", new ImageView(okSmallIcon));
                pkiItemDet = new TreeItem<>("La cadena de confianza está bajo la Infraestructura de Clave Pública del Estado Plurinacional de Bolivia, y por lo tanto, tiene valor legal");
            } else {
                pkiItem = new TreeItem<>("Cadena de confianza", new ImageView(errorSmallIcon));
                pkiItemDet = new TreeItem<>("La cadena de confianza no está bajo la Infraestructura de Clave Pública del Estado Plurinacional de Bolivia, y por lo tanto, no tiene valor legal");
            }
            pkiItem.getChildren().add(pkiItemDet);
            item.getChildren().add(pkiItem);
            TreeItem<String> vigItem, vigItemDet;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String periodo = dateFormat.format(((X509Certificate) cert.getCertificate()).getNotBefore())  + " y " 
                + dateFormat.format(((X509Certificate) cert.getCertificate()).getNotAfter());
            if (cert.isActive()) {
                if (cert.isActiveAlerted()) {
                    vigItem = new TreeItem<>("Firmado en el periodo de vigencia (Sin sellado de tiempo)", new ImageView(alertSmallIcon));
                } else {
                    vigItem = new TreeItem<>("Firmado en el periodo de vigencia", new ImageView(okSmallIcon));
                }
                vigItemDet = new TreeItem<>("La firma fue realizada dentro del periodo comprendido entre " + periodo);
            } else {
                vigItem = new TreeItem<>("Firmado fuera del periodo de vigencia", new ImageView(errorSmallIcon));
                vigItemDet = new TreeItem<>("La firma fue realizada fuera del periodo comprendido entre " + periodo);
            }
            vigItem.getChildren().add(vigItemDet);
            item.getChildren().add(vigItem);
            TreeItem<String> ocspItem, ocspItemDet;
            if (cert.isOCSP()) {
                if (cert.isOCSPAlerted()) {
                    ocspItem = new TreeItem<>("Firmado con certificado válido, revocado después de la firma (Sin sellado de tiempo)", new ImageView(alertSmallIcon));
                    ocspItemDet = new TreeItem<>("El documento fue firmado con un certificado revocado antes de la firma");
                } else {
                    ocspItem = new TreeItem<>("Firmado con certificado no revocado", new ImageView(okSmallIcon));
                    ocspItemDet = new TreeItem<>("El documento fue firmado con un certificado no revocado");
                }
            } else {
                ocspItem = new TreeItem<>("Firmado con certificado revocado", new ImageView(errorSmallIcon));
                if (cert.getOCSP().getState() == Validar.OCSPState.CONNECTION) {
                    ocspItemDet = new TreeItem<>("No se pudo acceder al servicio para verificar el estado del certificado.");
                    ocspItem.setExpanded(true);
                } else {
                    ocspItemDet = new TreeItem<>("El documento fue firmado con un certificado revocado antes de la firma");
                }
            }
            ocspItem.getChildren().add(ocspItemDet);
            item.getChildren().add(ocspItem);
            item.setExpanded(true);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        tree.setCellFactory(param -> new TextFieldTreeCell<String>() {
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

    public String getPass() {
        return pass;
    }
}
