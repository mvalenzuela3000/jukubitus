package bo.firmadigital.jacobitus4.jetty.localhost9000.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsbDto {
    private String name;
    private String serial;
    private String type;
    private String tran;
}
