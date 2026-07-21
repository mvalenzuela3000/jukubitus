package bo.firmadigital.jacobitus.escritorio.jetty.localhost3200.dtos;

import java.math.BigInteger;

import org.codehaus.jackson.annotate.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificadoDto {
    private Boolean esFirmaBolivia;
    private BigInteger numeroSerie;
    private String nombreComunIssuer;
    private String organizacionIssuer;
    private String nombreComunSubject;
    private String ci;
    private String complemento;
    private String organizacionSubject;
    private String unidadOrganizacionalSubject;
    private String inicioValidez;
    private String finValidez;
    private String alias;
    private Boolean esValido;
    @Setter(onMethod = @__(@JsonProperty(value = "OCSP"))) 
    @Getter(onMethod = @__(@JsonProperty(value = "OCSP"))) 
    private String OCSP;
}
