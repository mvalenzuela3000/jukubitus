package bo.firmadigital.jacobitus.escritorio.comun.plataforma;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlataformaInfo {

    private final SistemaOperativoEnum sistemaOperativo;
    private final ArquitecturaEnum arquitectura;

    @Override
    public String toString() {
        return sistemaOperativo.getValor()
                + "/"
                + arquitectura.getValor();
    }
}
