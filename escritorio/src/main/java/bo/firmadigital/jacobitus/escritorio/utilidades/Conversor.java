package bo.firmadigital.jacobitus.escritorio.utilidades;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.odftoolkit.odfdom.doc.OdfDocument;

import bo.firmadigital.jacobitus.comun.JacobitusException;

public class Conversor {
    protected final static File TMP = new File(System.getProperty("java.io.tmpdir"), "jacobitus");

    static {
        if (!TMP.exists()) {
            TMP.mkdir();
        }
    }

    public static File docxAPdf(File docx) {
        try {
            File out = new File(TMP, docx.getName().replace(".docx", ".pdf"));
            InputStream doc = new FileInputStream(docx);
            XWPFDocument document = new XWPFDocument(doc);
            FileOutputStream pdf = new FileOutputStream(out);
            org.apache.poi.xwpf.converter.pdf.PdfOptions options = org.apache.poi.xwpf.converter.pdf.PdfOptions.create();
            org.apache.poi.xwpf.converter.pdf.PdfConverter.getInstance().convert(document, pdf, options);
            return out;
        } catch (IOException | XWPFConverterException ex) {
            // throw new JacobitusException(ex.getMessage());
            throw new JacobitusException("El documento no pudo ser convertido a pdf.");
        }
    }

    public static File odtAPdf(File odt) {
         try {
            File out = new File(TMP, odt.getName().replace(".odt", ".pdf"));
            InputStream doc = new FileInputStream(odt);
            OdfDocument document = OdfDocument.loadDocument(doc);
            FileOutputStream pdf = new FileOutputStream(out);
            org.odftoolkit.odfdom.converter.pdf.PdfOptions options = org.odftoolkit.odfdom.converter.pdf.PdfOptions.create();
            org.odftoolkit.odfdom.converter.pdf.PdfConverter.getInstance().convert(document, pdf, options);
            return out;
        } catch(Exception ex) {
            // throw new JacobitusException(ex.getMessage());
            throw new JacobitusException("El documento no pudo ser convertido a pdf.");
        }
    }
}
