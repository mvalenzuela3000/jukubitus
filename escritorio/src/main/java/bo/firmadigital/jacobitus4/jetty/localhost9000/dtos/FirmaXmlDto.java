package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaXmlDto {
    private Long slot;
    private String pin;
    private String alias;
    private String file;

    private String node = null;
    private String digest = null;
    private String signatureAlgorithm = null;
    private Boolean enveloped = false;
    private Boolean prefix = false;
}
