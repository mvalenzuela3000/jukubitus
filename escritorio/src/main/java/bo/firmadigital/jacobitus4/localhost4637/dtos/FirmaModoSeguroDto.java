package bo.firmadigital.jacobitus4.localhost4637.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaModoSeguroDto {
    private Boolean software = false;
    private String ci;
    private String format;
    private List<FirmaPdfItemDto> archivo;
}
