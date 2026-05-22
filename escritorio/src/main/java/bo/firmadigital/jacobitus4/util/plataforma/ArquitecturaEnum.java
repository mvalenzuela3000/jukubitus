package bo.firmadigital.jacobitus4.util.plataforma;

public enum ArquitecturaEnum {

    X64("x64"),
    X86("x86"),
    ARM64("arm64"),
    DESCONOCIDA("desconocida");

    private final String valor;

    ArquitecturaEnum(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }
}
