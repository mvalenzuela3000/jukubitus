package bo.firmadigital.jacobitus4.util.plataforma;

public enum SistemaOperativoEnum {

    WINDOWS("windows"),
    LINUX("linux"),
    MACOS("macos"),
    DESCONOCIDO("desconocido");

    private final String valor;

    SistemaOperativoEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}

