package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import org.codehaus.jackson.annotate.JsonProperty;

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
    @Setter(onMethod = @__(@JsonProperty(value = "UID"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "UID"))) 
    private String UID;
    @Setter(onMethod = @__(@JsonProperty(value = "CN"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "CN"))) 
    private String CN;
    @Setter(onMethod = @__(@JsonProperty(value = "T"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "T"))) 
    private String T;
    @Setter(onMethod = @__(@JsonProperty(value = "O"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "O"))) 
    private String O;
    @Setter(onMethod = @__(@JsonProperty(value = "OU"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "OU"))) 
    private String OU;
    private String EmailAddress;
    private String description;
}