package bo.firmadigital.jacobitus.escritorio.formularios;

import java.awt.Desktop;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import bo.firmadigital.jacobitus.escritorio.comun.actualizaciones.ActualizacionInfo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FormActualizacionDisponible extends Stage {

    private final Stage parent;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public FormActualizacionDisponible(Stage parent, ActualizacionInfo result) {

        this.parent = parent;

        setTitle("Jacobitus - Actualización disponible");

        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);

        ImageView imagen = new ImageView(new Image(
                            this.getClass().getClassLoader().getResource("logo-agetic.png").toExternalForm()));

        Label titulo = new Label("Nueva actualización disponible");
        titulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Label versionInstalada = new Label(
                "Versión instalada: " + result.getVersionInstalada());
        versionInstalada.setStyle("-fx-font-size: 14px;");

        Label ultimaVersion = new Label(
                "Nueva versión: " + result.getUltimaVersion());
        ultimaVersion.setStyle("-fx-font-size: 14px;");

        Label tipo = new Label(
                result.esNecesaria()
                        ? "Actualización NECESARIA"
                        : "Se recomienda actualizar");

        tipo.setStyle(
                result.esNecesaria()
                        ? "-fx-font-size: 14px; -fx-text-fill: #c0392b; -fx-font-weight: bold;"
                        : "-fx-font-size: 14px; -fx-text-fill: #2c3e50;");

        VBox texto = new VBox(5, titulo, versionInstalada, ultimaVersion, tipo);

        HBox encabezado = new HBox(10);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        encabezado.getChildren().addAll(imagen, texto);

        Button btnActualizar = new Button("Actualizar");
        Button btnMasTarde = new Button("Más tarde");

        btnActualizar.setOnAction(e -> {
            abrirNavegador(result.getEnlaceDescarga());
            close();
        });

        btnMasTarde.setOnAction(e -> close());

        Region espacio = new Region();
        HBox.setHgrow(espacio, javafx.scene.layout.Priority.ALWAYS);

        HBox botones = new HBox(10, espacio, btnActualizar, btnMasTarde);
        botones.setPadding(new Insets(10, 0, 0, 0));

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getChildren().addAll(encabezado, botones);

        Scene scene = new Scene(root, 480, 180);
        setScene(scene);
    }

    private void abrirNavegador(String url) {
        executor.submit(() -> {
            try {
                if (url == null || url.isBlank()) {
                    Platform.runLater(() -> mostrarError("URL inválida"));
                    return;
                }
                URI uri = URI.create(url);
                if (Desktop.isDesktopSupported()
                        && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    Runtime.getRuntime().exec(new String[] { "xdg-open", url });
                }
            } catch (Exception e) {
                Platform.runLater(() -> mostrarError("Error al abrir navegador: " + e.getMessage()));
            }
        });
    }

    private void mostrarError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(parent);
        alert.setTitle("Jacobitus");
        alert.setHeaderText("Error");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
