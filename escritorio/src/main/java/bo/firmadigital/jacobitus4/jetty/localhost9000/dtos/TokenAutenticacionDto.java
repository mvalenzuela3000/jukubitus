package bo.firmadigital.jacobitus.escritorio.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenAutenticacionDto {
    private Long slot;
    private String pin;
}
