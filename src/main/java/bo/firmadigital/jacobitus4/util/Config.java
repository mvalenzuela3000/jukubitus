/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.util;

import java.io.File;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author ADSIB
 */
public class Config {
    public void configuracion() {
        File user = FileSystemView.getFileSystemView().getDefaultDirectory();
        System.out.println(user);
    }
}
