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
public class TokenPublicCertificateDto implements ITokenCertificateDto {
    private String tipo;
    private String tipo_desc;
    private Boolean adsib;
    private String serialNumber;
    private String alias;
    private String id;
    private String pem;
    private TokenCertificateValidezDto validez;
    private TokenCertificateTitularDto titular;
    private String common_name;
    private TokenCertificateEmisorDto emisor;
}
