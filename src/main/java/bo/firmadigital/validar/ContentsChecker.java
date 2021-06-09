/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import com.itextpdf.text.pdf.PRTokeniser.TokenType;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ADSIB
 */
public class ContentsChecker extends PdfReader {
    public ContentsChecker(InputStream is) throws IOException {
        super(is);
    }

    /**
     * Verifica si la firma cubre todo el documento.
     * @param V Dictionary que da acceso a la firma.
     * @return 
     */
    public Estado checkElementAdded(PdfDictionary V) {
        long[] byteRange = V.getAsArray(PdfName.BYTERANGE).asLongArray();
        try {
            if (4 != byteRange.length || 0 != byteRange[0] || tokens.getSafeFile().length() != byteRange[2] + byteRange[3]) {
                tokens.seek(byteRange[2] + byteRange[3]);
                int widgets = 0, signatures = 0, highlihght = 0, forms = 0, signaturesContent = 0;
                while (tokens.nextToken()) {
                    if (tokens.getTokenType() == TokenType.START_DIC) {
                        PdfDictionary dict = readDictionary();
                        if (dict.contains(PdfName.TYPE) && dict.contains(PdfName.SUBTYPE)) {
                            if (dict.getAsName(PdfName.TYPE).equals(PdfName.ANNOT) && dict.getAsName(PdfName.SUBTYPE).equals(PdfName.WIDGET)) {
                                widgets++;
                                if (dict.contains(PdfName.V) && dict.contains(PdfName.FT)) {
                                    if (dict.getAsName(PdfName.FT).equals(PdfName.SIG)) {
                                        signatures++;
                                    }
                                } else {
                                    if (dict.contains(PdfName.FT) && dict.getAsName(PdfName.FT).equals(PdfName.SIG)) {
                                        widgets--;
                                    }
                                }
                            } else if (dict.getAsName(PdfName.TYPE).equals(PdfName.XOBJECT) && dict.getAsName(PdfName.SUBTYPE).equals(PdfName.FORM)) {
                                forms++;
                            } else if (dict.getAsName(PdfName.TYPE).equals(PdfName.ANNOT) && dict.getAsName(PdfName.SUBTYPE).equals(PdfName.HIGHLIGHT)) {
                                highlihght++;
                            }
                        } else if (dict.contains(PdfName.TYPE) && dict.getAsName(PdfName.TYPE).equals(PdfName.SIG)) {
                            signaturesContent++;
                        }
                    }
                }
                if (widgets > 0) {
                    if (widgets == signatures && signatures == signaturesContent) {
                        if (highlihght > 0) {
                            return Estado.highlight_agregado;
                        } else {
                            return Estado.widget_firma_agregado;
                        }
                    } else {
                        return Estado.widget_otro_agregado;
                    }
                }
                return Estado.desconocido_agregado;
            }
        } catch (IOException ex) {
            // That's not expected because if the signature is invalid, it should have already failed
            return Estado.desconocido_agregado;
        }

        return Estado.sin_cambios;
    }

    public enum Estado {
        widget_firma_agregado,
        widget_otro_agregado,
        highlight_agregado,
        desconocido_agregado,
        sin_cambios
    }
}