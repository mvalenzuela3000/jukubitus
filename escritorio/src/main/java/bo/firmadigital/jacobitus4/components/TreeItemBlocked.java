package bo.firmadigital.jacobitus4.components;

import bo.firmadigital.jacobitus.validador.comun.Firma;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

@SuppressWarnings("rawtypes")
public class TreeItemBlocked<T extends Object> extends TreeItem {
    protected Firma firma;

    @SuppressWarnings("unchecked")
    public TreeItemBlocked(T t, Node node, Firma firma) {
        super(t, node);
        this.firma = firma;
    }

    public Firma getCertDate() {
        return firma;
    }
}
