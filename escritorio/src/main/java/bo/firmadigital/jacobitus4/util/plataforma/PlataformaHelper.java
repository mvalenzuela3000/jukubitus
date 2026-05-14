package bo.firmadigital.jacobitus4.util.plataforma;

public final class PlataformaHelper {

    private PlataformaHelper() {
    }

    public static PlataformaInfo identificar() {

        String soNombre = System.getProperty("os.name")
                .toLowerCase();

        String soArquitectura = System.getProperty("os.arch")
                .toLowerCase();

        SistemaOperativoEnum sistemaOperativo = identificarSistemaOperativo(soNombre);

        ArquitecturaEnum arquitectura = identificarArquitectura(soArquitectura);

        return new PlataformaInfo(
                sistemaOperativo,
                arquitectura);
    }

    private static SistemaOperativoEnum identificarSistemaOperativo(
            String sistemaOperativo) {

        if (sistemaOperativo.contains("win")) {
            return SistemaOperativoEnum.WINDOWS;
        }

        if (sistemaOperativo.contains("mac")) {
            return SistemaOperativoEnum.MACOS;
        }

        if (sistemaOperativo.contains("nix")
                || sistemaOperativo.contains("nux")
                || sistemaOperativo.contains("aix")) {

            return SistemaOperativoEnum.LINUX;
        }

        return SistemaOperativoEnum.DESCONOCIDO;
    }

    private static ArquitecturaEnum identificarArquitectura(
            String arquitectura) {

        if (arquitectura.contains("amd64")
                || arquitectura.contains("x86_64")) {

            return ArquitecturaEnum.X64;
        }

        if (arquitectura.contains("86")) {
            return ArquitecturaEnum.X86;
        }

        if (arquitectura.contains("aarch64")
                || arquitectura.contains("arm64")) {

            return ArquitecturaEnum.ARM64;
        }

        return ArquitecturaEnum.DESCONOCIDA;
    }
}
