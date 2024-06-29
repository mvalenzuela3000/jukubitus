package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCertificateTitularDto {
    private String dnQualifier;
    private String uidNumber;
    private String UID;
    private String CN;
    private String T;
    private String O;
    private String OU;
    private String EmailAddress;
    private String description;
}