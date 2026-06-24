package bo.firmadigital.jacobitus.escritorio.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

public class TableViewHelper {
    private TableViewHelper() {}

    public static <S> void ajustarColumnas(TableView<S> table) {
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        for (TableColumn<S, ?> column : table.getColumns()) {
            double maxAncho = maxAnchoTexto(column.getText()) + 25; 
            for (S fila : table.getItems()) {
                if (column.getCellData(fila) != null) {
                    String valorCelda = column.getCellData(fila).toString();
                    double anchoTexto = maxAnchoTexto(valorCelda) + 15;
                    if (anchoTexto > maxAncho) {
                        maxAncho = anchoTexto;
                    }
                }
            }
            column.setPrefWidth(maxAncho);
        }
    }

    private static double maxAnchoTexto(String texto) {
        Text t = new Text(texto);
        return t.getLayoutBounds().getWidth();
    }
}
