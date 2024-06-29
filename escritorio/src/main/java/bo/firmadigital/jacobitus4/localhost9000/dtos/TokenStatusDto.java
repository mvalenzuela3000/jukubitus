package bo.firmadigital.jacobitus4.localhost9000.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenStatusDto {
    private Boolean connected;
    private List<String> tokens;
}
