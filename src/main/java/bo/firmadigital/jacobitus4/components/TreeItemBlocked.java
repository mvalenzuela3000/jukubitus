/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.components;

import bo.firmadigital.jacobitus.validador.CertDate;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

/**
 *
 * @author ADSIB
 * @param <T>
 */
public class TreeItemBlocked<T extends Object> extends TreeItem {
    protected CertDate cert;

    public TreeItemBlocked(T t, Node node, CertDate cert) {
        super(t, node);
        this.cert = cert;
    }

    public CertDate getCertDate() {
        return cert;
    }
}
