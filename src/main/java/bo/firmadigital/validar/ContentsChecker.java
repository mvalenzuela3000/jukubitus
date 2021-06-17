/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import com.itextpdf.io.source.PdfTokenizer.TokenType;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfReader;
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
     * @param sign Dictionary que da acceso a la firma.
     * @return 
     */
    public Estado checkElementAdded(PdfDictionary sign) {
        long[] byteRange = sign.getAsArray(PdfName.ByteRange).toLongArray();
        try {
            if (4 != byteRange.length || 0 != byteRange[0] || tokens.getSafeFile().length() != byteRange[2] + byteRange[3]) {
                tokens.seek(byteRange[2] + byteRange[3]);
                int widgets = 0, signatures = 0, highlihght = 0, forms = 0, signaturesContent = 0;
                while (tokens.nextToken()) {
                    if (tokens.getTokenType() == TokenType.StartDic) {
                        PdfDictionary dict = readDictionary(true);
                        if (dict.containsKey(PdfName.Type) && dict.containsKey(PdfName.Subtype)) {
                            if (dict.getAsName(PdfName.Type).equals(PdfName.Annot) && dict.getAsName(PdfName.Subtype).equals(PdfName.Widget)) {
                                widgets++;
                                if (dict.containsKey(PdfName.V) && dict.containsKey(PdfName.FT)) {
                                    if (dict.getAsName(PdfName.FT).equals(PdfName.Sig)) {
                                        signatures++;
                                    }
                                } else {
                                    if (dict.containsKey(PdfName.FT) && dict.getAsName(PdfName.FT).equals(PdfName.Sig)) {
                                        widgets--;
                                    }
                                }
                            } else if (dict.getAsName(PdfName.Type).equals(PdfName.XObject) && dict.getAsName(PdfName.Subtype).equals(PdfName.Form)) {
                                forms++;
                            } else if (dict.getAsName(PdfName.Type).equals(PdfName.Annot) && dict.getAsName(PdfName.Subtype).equals(PdfName.Highlight)) {
                                highlihght++;
                            }
                        } else if (dict.containsKey(PdfName.Type) && dict.getAsName(PdfName.Type).equals(PdfName.Sig)) {
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
        } catch (Exception ignore) {
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