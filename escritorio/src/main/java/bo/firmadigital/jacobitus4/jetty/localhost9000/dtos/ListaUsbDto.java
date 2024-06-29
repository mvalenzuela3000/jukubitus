package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListaUsbDto {
    private List<UsbDto> usbs;
}
