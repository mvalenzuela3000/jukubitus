/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.components;

import bo.firmadigital.jacobitus.validador.comun.Firma;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 *
 * @author ADSIB
 * @param <T>
 */
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
