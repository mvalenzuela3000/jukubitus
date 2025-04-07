package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidacionArchivoDto {
    private String file;
    private Date fecFirma = null;
}
