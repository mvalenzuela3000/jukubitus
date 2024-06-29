package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenPemDto {
    private String pin;
    private Long slot;
    private String id;
    @NotBlank(message = "Name must not be blank")
    private String pem;
}
