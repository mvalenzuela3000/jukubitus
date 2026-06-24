package bo.firmadigital.jacobitus.escritorio;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import bo.firmadigital.jacobitus.escritorio.comun.Config;
import bo.firmadigital.jacobitus.firmador.base.ConfiguracionFirmador;
import bo.firmadigital.jacobitus.token.Slot;
import bo.firmadigital.jacobitus.token.TokenPKCS12;
import bo.firmadigital.jacobitus.utilidades.SistemaOperativoHelper;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormConfiguracion extends Stage {
    private Config config;
    private CheckBox checkBox;
    private final CheckBox checkBoxPort2;
    private final CheckBox checkBoxPort3;
    private boolean checkBoxEvent = true;
    private TextField textFieldIP;
    private TextField textFieldPort;
    private TextField textFieldToken;
    private final Button buttonControlador;
    // private final CheckBox checkBoxHsm;
    // private TextField textFieldHsmCloud;
    // private TextField textFieldHsmJWT;
    // private final CheckBox checkBoxTS;
    // private TextField textFieldTS;
    // private TextField textFieldTSJWT;

    private ConfiguracionFirmador getConfigFirmador() {
        Config config = Config.getInstance();
        ConfiguracionFirmador configFirmador = new ConfiguracionFirmador();
        configFirmador.setControlador(config.getDriver());
        configFirmador.setSoftoken(config.getToken());
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

    public FormConfiguracion(Stage parent) {
        setTitle("Opciones de configuración");
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        config = Config.getInstance();
        HBox root = new HBox();
        VBox vbox1 = new VBox();
        vbox1.setPadding(new Insets(10));
        vbox1.setSpacing(8);
        vbox1.setMinWidth(210);

        // Opciones de configuracion de proxy
        Label titleP = new Label("Proxy");
        titleP.setStyle("-fx-font-weight: bold");
        vbox1.getChildren().add(titleP);
        checkBox = new CheckBox("Utilizar proxy");
        checkBox.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    textFieldIP.setDisable(!newValue);
                    textFieldPort.setDisable(!newValue);
                    if (newValue == false && config.isProxyEnabled() == false) {
                        textFieldIP.setText(config.getProxyIP());
                        textFieldPort.setText(config.getProxyPort());
                    }
                });
        vbox1.getChildren().add(checkBox);
        Label labelIP = new Label("Introduzca la IP:");
        vbox1.getChildren().add(labelIP);
        textFieldIP = new TextField();
        vbox1.getChildren().add(textFieldIP);
        Label labelPort = new Label("Introduzca el puerto:");
        vbox1.getChildren().add(labelPort);
        textFieldPort = new TextField("3128");
        vbox1.getChildren().add(textFieldPort);
        Button buttonGuardar = new Button("Guardar Proxy");
        buttonGuardar.setOnAction(t -> {
            config.setProxyEnabled(checkBox.isSelected());
            config.setProxyIP(textFieldIP.getText());
            config.setProxyPort(textFieldPort.getText());
            config.save();
            close();
        });
        vbox1.getChildren().add(buttonGuardar);
        vbox1.getChildren().add(new Separator(Orientation.HORIZONTAL));

        // Configuracion de puertos (compatibilidad con otros firmadores)
        Label titleS = new Label("Puerto secundario");
        titleS.setStyle("-fx-font-weight: bold");
        vbox1.getChildren().add(titleS);
        checkBoxPort2 = new CheckBox("Habilitar puerto 4637");
        checkBoxPort2.setSelected(config.isSecondaryPortEnabled());
        vbox1.getChildren().add(checkBoxPort2);
        checkBoxPort3 = new CheckBox("Habilitar puerto 3200");
        checkBoxPort3.setSelected(config.isTertiaryPortEnabled());
        checkBoxPort2.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    config.setSecondaryPortEnabled(newValue);
                    config.save();
                    if (checkBoxEvent) {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Para que este cambio tenga efecto,\ndeberá reiniciar la aplicación.", ButtonType.OK);
                        alert.initOwner(parent);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    }
                    // checkBoxEvent = true;
                    // if (newValue && checkBoxPort3.isSelected()) {
                    //     checkBoxEvent = false;
                    //     checkBoxPort3.setSelected(false);
                    // }
                });
        checkBoxPort3.selectedProperty()
                .addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    config.setTertiaryPortEnabled(newValue);
                    config.save();
                    if (checkBoxEvent) {
                        Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Para que este cambio tenga efecto,\ndeberá reiniciar la aplicación.", ButtonType.OK);
                        alert.initOwner(parent);
                        alert.initModality(Modality.APPLICATION_MODAL);
                        alert.setTitle("Jacobitus");
                        alert.showAndWait();
                    }
                    // checkBoxEvent = true;
                    // if (newValue && checkBoxPort2.isSelected()) {
                    //     checkBoxEvent = false;
                    //     checkBoxPort2.setSelected(false);
                    // }
                });
        vbox1.getChildren().add(checkBoxPort3);

        root.getChildren().add(vbox1);

        Separator separator = new Separator(Orientation.VERTICAL);
        root.getChildren().add(separator);

        VBox vbox2 = new VBox();
        vbox2.setPadding(new Insets(10));
        vbox2.setSpacing(8);
        vbox2.setMinWidth(210);

        // Configuracion de controlador por defecto
        Label titleD = new Label("Controlador");
        titleD.setStyle("-fx-font-weight: bold");
        vbox2.getChildren().add(titleD);
        final Label labelDriver = new Label(
                "Nombre: " + (config.getDriver() == null ? "Ninguno" : config.getDriver().getName()));
        vbox2.getChildren().add(labelDriver);
        if (config.getDriver() == null) {
            buttonControlador = new Button("Seleccionar");
        } else {
            buttonControlador = new Button("Remover");
        }
        vbox2.getChildren().add(buttonControlador);
        buttonControlador.setOnAction(t -> {
            buttonControlador.setDisable(true);
            if (config.getDriver() == null) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Abrir Controlador");
                if (SistemaOperativoHelper.esWindows()) {
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Controlador (*.dll)",
                            "*.dll");
                    fileChooser.getExtensionFilters().add(extFilter);
                } else if (SistemaOperativoHelper.esUnix()) {
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Controlador (*.so)",
                            "*.so");
                    fileChooser.getExtensionFilters().add(extFilter);
                    FileChooser.ExtensionFilter extFilterDocs = new FileChooser.ExtensionFilter("Todos", "*.*", "*.*");
                    fileChooser.getExtensionFilters().add(extFilterDocs);
                } else {
                    FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Controlador (*.dylib)",
                            "*.dylib");
                    fileChooser.getExtensionFilters().add(extFilter);
                }
                File file = fileChooser.showOpenDialog(parent);
                if (file != null) {
                    config.setDriver(file);
                    config.save();
                    buttonControlador.setText("Remover");
                }
            } else {
                config.setDriver(null);
                config.save();
                buttonControlador.setText("Seleccionar");
            }
            labelDriver.setText("Nombre: " + (config.getDriver() == null ? "Ninguno" : config.getDriver().getName()));
            buttonControlador.setDisable(false);
        });
        vbox2.getChildren().add(new Separator(Orientation.HORIZONTAL));

        // Configuracion HSM
        // vbox2.setPadding(new Insets(10));
        // vbox2.setSpacing(8);
        // vbox2.setMinWidth(210);
        // Label titleHsm = new Label("Opciones HSM");
        // titleHsm.setStyle("-fx-font-weight: bold");
        // vbox2.getChildren().add(titleHsm);
        // checkBoxHsm = new CheckBox("Utilizar HSM");
        // checkBoxHsm.selectedProperty().addListener((ObservableValue<? extends
        // Boolean> observable, Boolean oldValue, Boolean newValue) -> {
        // textFieldHsmCloud.setDisable(!newValue);
        // textFieldHsmJWT.setDisable(!newValue);
        // if (newValue == false && config.isHsmEnabled() == false) {
        // textFieldHsmCloud.setText(config.getHsmCloud());
        // textFieldHsmJWT.setText(config.getHsmJWT());
        // }
        // });
        // vbox2.getChildren().add(checkBoxHsm);
        // Label labelHsmUrl = new Label("URL:");
        // vbox2.getChildren().add(labelHsmUrl);
        // textFieldHsmCloud = new TextField();
        // textFieldHsmCloud.setText(config.getHsmCloud());
        // vbox2.getChildren().add(textFieldHsmCloud);
        // Label labelHsmJWT = new Label("Json Web Token:");
        // vbox2.getChildren().add(labelHsmJWT);
        // textFieldHsmJWT = new TextField();
        // textFieldHsmJWT.setText(config.getHsmJWT());
        // vbox2.getChildren().add(textFieldHsmJWT);
        // Button buttonGuardarHsm = new Button("Guardar HSM");
        // vbox2.getChildren().add(buttonGuardarHsm);
        // buttonGuardarHsm.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) ->
        // {
        // config.setHsmEnabled(checkBoxHsm.isSelected());
        // config.setHsmCloud(textFieldHsmCloud.getText());
        // config.setHsmJWT(textFieldHsmJWT.getText());
        // config.save();
        // close();
        // });

        // Configuracion de softoken
        Label titleT = new Label("Softoken");
        titleT.setStyle("-fx-font-weight: bold");
        vbox2.getChildren().add(titleT);
        Label labelToken = new Label("Archivo para token/software:");
        vbox2.getChildren().add(labelToken);
        textFieldToken = new TextField();
        textFieldToken.setDisable(true);
        vbox2.getChildren().add(textFieldToken);
        Button buttonCrear = new Button("Crear Token");
        Button btnAbrirUbicacion = new Button("Abrir ubicación");
        buttonCrear.setOnAction(t -> {
            FormContrasenaNueva contrasena = new FormContrasenaNueva(parent);
            contrasena.showAndWait();
            if (contrasena.getPass() != null) {
                Slot slot = new Slot(config.getTokenToCreate(), this.getConfigFirmador());
                TokenPKCS12 token = new TokenPKCS12(slot);
                try {
                    token.crear(contrasena.getPass());
                    textFieldToken.setText(config.getToken().getName());
                    buttonCrear.setDisable(true);
                    btnAbrirUbicacion.setDisable(false);
                } catch (GeneralSecurityException ex) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK);
                    alert.initOwner(parent);
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.setTitle("Jacobitus");
                    alert.showAndWait();
                }
            }
        });
        vbox2.getChildren().add(buttonCrear);
        btnAbrirUbicacion.setOnAction(t -> {
            try {
                if (SistemaOperativoHelper.esUnix()) {
                    Runtime.getRuntime().exec(new String[] { "sh", "-c", "/usr/bin/xdg-open '" + config.getToken().getParentFile().getPath() + "'" });
                }
                if (SistemaOperativoHelper.esWindows()) {
                    Runtime.getRuntime().exec("explorer " + config.getToken().getParentFile().getPath());
                }
                if (SistemaOperativoHelper.esMacOS()) {
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/open", config.getToken().getParentFile().getPath()});
                }
            } catch (IOException ex) {
                Logger.getLogger(FormAplicacion.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        vbox2.getChildren().add(btnAbrirUbicacion);

        root.getChildren().add(vbox2);

        // Separator separator2 = new Separator(Orientation.VERTICAL);
        // root.getChildren().add(separator2);

        // VBox vbox3 = new VBox();

        // Configuracion de sellado de tiempo
        // vbox3.setPadding(new Insets(10));
        // vbox3.setSpacing(8);
        // vbox3.setMinWidth(210);
        // Label titleTS = new Label("Opciones Sellado de Tiempo");
        // titleTS.setStyle("-fx-font-weight: bold");
        // vbox3.getChildren().add(titleTS);
        // checkBoxTS = new CheckBox("Utilizar Sellado");
        // checkBoxTS.selectedProperty().addListener((ObservableValue<? extends Boolean>
        // observable, Boolean oldValue, Boolean newValue) -> {
        // textFieldTS.setDisable(!newValue);
        // textFieldTSJWT.setDisable(!newValue);
        // if (newValue == false && config.isTSEnabled() == false) {
        // textFieldTS.setText(config.getTS());
        // textFieldTSJWT.setText(config.getTSJWT());
        // }
        // });
        // vbox3.getChildren().add(checkBoxTS);
        // Label labelTSUrl = new Label("URL:");
        // vbox3.getChildren().add(labelTSUrl);
        // textFieldTS = new TextField();
        // textFieldTS.setText(config.getTS());
        // vbox3.getChildren().add(textFieldTS);
        // Label labelTSJWT = new Label("Json Web Token:");
        // vbox3.getChildren().add(labelTSJWT);
        // textFieldTSJWT = new TextField();
        // textFieldTSJWT.setText(config.getTSJWT());
        // vbox3.getChildren().add(textFieldTSJWT);
        // Button buttonGuardarTS = new Button("Guardar Sellado");
        // vbox2.getChildren().add(buttonGuardarTS);
        // buttonGuardarTS.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent t) -> {
        // config.setTSEnabled(checkBoxTS.isSelected());
        // config.setTS(textFieldTS.getText());
        // config.setTSJWT(textFieldTSJWT.getText());
        // config.save();
        // close();
        // });
        // vbox3.getChildren().add(buttonGuardarTS);

        // root.getChildren().add(vbox3);

        // Establecer valores configurados
        checkBox.setSelected(true);
        checkBox.setSelected(config.isProxyEnabled());
        textFieldIP.setText(config.getProxyIP());
        textFieldPort.setText(config.getProxyPort());
        if (config.getToken() == null) {
            textFieldToken.setText("Ninguno");
            buttonCrear.setDisable(false);
            btnAbrirUbicacion.setDisable(true);
        } else {
            textFieldToken.setText(config.getToken().getName());
            buttonCrear.setDisable(true);
            btnAbrirUbicacion.setDisable(false);
        }
        // checkBoxHsm.setSelected(true);
        // checkBoxHsm.setSelected(config.isHsmEnabled());
        // checkBoxTS.setSelected(true);
        // checkBoxTS.setSelected(config.isTSEnabled());

        Scene scene = new Scene(root, 430, 300);
        setScene(scene);
    }
}
