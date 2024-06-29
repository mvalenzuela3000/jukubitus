package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenChangePinDto {
    private String old_pin;
    private Long slot;
    private String new_pin;
}