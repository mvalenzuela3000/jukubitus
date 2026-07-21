package bo.firmadigital.jacobitus.escritorio.componentes;

import bo.firmadigital.jacobitus.validador.comun.Firma;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import lombok.Getter;

@SuppressWarnings("rawtypes")
@Getter
public class TreeItemBlocked<T extends Object> extends TreeItem {
    protected Firma firma;

    @SuppressWarnings("unchecked")
    public TreeItemBlocked(T t, Node node, Firma firma) {
        super(t, node);
        this.firma = firma;
    }
}
