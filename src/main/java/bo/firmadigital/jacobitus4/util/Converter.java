/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

/**
 *
 * @author ADSIB
 */
public class Converter {
    protected final static File TMP = new File(System.getProperty("java.io.tmpdir"), "jacobitus");

    static {
        if (!TMP.exists()) {
            TMP.mkdir();
        }
    }

    public static File docxToPdf(File docx) {
        Config config = new Config();
        URLClassLoader loader = config.getConversor();
        if (loader == null) {
            throw new RuntimeException("Por favor descargue el conversor previamente.");
        }
        try {
            Class DocxConverter = Class.forName("bo.firmadigital.conversorpdf.DocxConverter", true, loader);
            Method docxToPdf = DocxConverter.getMethod("docxToPdf", File.class, File.class);
            File out = new File(TMP, docx.getName().replace(".docx", ".pdf"));
            docxToPdf.invoke(null, docx, out);
            return out;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static File odtToPdf(File odt) {
        Config config = new Config();
        URLClassLoader loader = config.getConversor();
        if (loader == null) {
            throw new RuntimeException("Por favor descargue el conversor previamente.");
        }
        try {
            Class OdtConverter = Class.forName("bo.firmadigital.conversorpdf.OdtConverter", true, loader);
            Method odtToPdf = OdtConverter.getMethod("odtToPdf", File.class, File.class);
            File out = new File(TMP, odt.getName().replace(".odt", ".pdf"));
            odtToPdf.invoke(null, odt, out);
            return out;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
