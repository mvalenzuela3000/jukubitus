package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import java.util.List;

import bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos.comun.ITokenCertificateDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenDataDto {
    private Integer certificates;
    private List<ITokenCertificateDto> data;
    private Integer private_keys;
}
