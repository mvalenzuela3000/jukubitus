package bo.firmadigital.jacobitus4.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaPdfItemDto {
    private String id;
    private String pdf;
    private Boolean bloquear = false;
}
