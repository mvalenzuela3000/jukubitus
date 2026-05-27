package bo.firmadigital.jacobitus4.util.actualizacion;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.codehaus.jackson.map.ObjectMapper;

import com.vdurmont.semver4j.Semver;

public class ActualizacionHelper {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    private final String enlaceBaseDescarga;

    public ActualizacionHelper(String enlaceBaseDescarga) {
        this.enlaceBaseDescarga = removerBarraFinal(enlaceBaseDescarga);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public ActualizacionInfo verificarActualizacion(
            String enlaceUltimaVersion,
            String versionInstalada,
            String sistemaOperativo,
            String arquitectura) throws IOException, InterruptedException {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(enlaceUltimaVersion))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Error consultando versión: HTTP "
                                + response.statusCode());
            }

            VersionResponse versionResponse = objectMapper.readValue(
                    response.body(),
                    VersionResponse.class);

            String ultimaVersion = versionResponse.getVersion();

            boolean actualizacionDisponible = esVersionRemotaMayor(
                    versionInstalada,
                    ultimaVersion);

            boolean esNecesaria = "necesaria".equalsIgnoreCase(
                    versionResponse.getTipoActualizacion());

            String enlaceDescarga = generarEnlaceDescarga(sistemaOperativo, arquitectura);

            return new ActualizacionInfo(
                    actualizacionDisponible,
                    esNecesaria,
                    versionInstalada,
                    ultimaVersion,
                    enlaceDescarga);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean esVersionRemotaMayor(
            String versionInstalada,
            String ultimaVersion) {

        Semver local = new Semver(
                versionInstalada,
                Semver.SemverType.LOOSE);

        Semver remota = new Semver(
                ultimaVersion,
                Semver.SemverType.LOOSE);

        return remota.isGreaterThan(local);
    }

    private String generarEnlaceDescarga(
            String sistemaOpertivo,
            String arquitectura) {

        return enlaceBaseDescarga
                + "/"
                + normalizar(sistemaOpertivo)
                + "/"
                + normalizar(arquitectura);
    }

    public void abrirPaginaDescarga(String enlaceDescarga) {
        try {
            if (!Desktop.isDesktopSupported()) {
                throw new RuntimeException(
                        "Desktop no soportado");
            }
            Desktop.getDesktop().browse(
                    URI.create(enlaceDescarga));
        } catch (Exception e) {
            throw new RuntimeException(
                    "No fue posible abrir la URL de descarga",
                    e);
        }
    }

    private String normalizar(String cadena) {
        return cadena
                .trim()
                .toLowerCase();
    }

    private String removerBarraFinal(String enlace) {
        if (enlace.endsWith("/")) {
            return enlace.substring(0, enlace.length() - 1);
        }
        return enlace;
    }
}
