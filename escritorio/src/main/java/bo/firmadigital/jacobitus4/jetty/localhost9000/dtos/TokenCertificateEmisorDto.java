package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import org.codehaus.jackson.annotate.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenCertificateEmisorDto {
    @Setter(onMethod = @__(@JsonProperty(value = "CN"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "CN"))) 
    private String CN;
    @Setter(onMethod = @__(@JsonProperty(value = "O"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "O"))) 
    private String O;
}