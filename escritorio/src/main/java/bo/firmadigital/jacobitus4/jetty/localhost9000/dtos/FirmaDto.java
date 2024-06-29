package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaDto {
    private Boolean noModificado;
    private Boolean cadenaConfianza;
    private Boolean firmadoDuranteVigencia;
    private Boolean firmadoAntesRevocacion;
    private Boolean versionado;
    private Boolean timeStamp;
    private String fechaFirma;
    private CertificadoDto certificado;
}
