package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import bo.firmadigital.jacobitus4.jetty.localhost9000.dtos.comun.ITokenCertificateDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenPrivateCertificateDto implements ITokenCertificateDto {
    private String tipo;
    private String tipo_desc;
    private String alias;
    private String id;
    private Boolean tiene_certificado;
}
