/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.validar;

import com.itextpdf.io.source.PdfTokenizer.TokenType;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        int widgets = 0, signatures = 0; Map<PdfName, List<PdfDictionary>> map = new TreeMap<>();
        try {
            if (4 != byteRange.length || 0 != byteRange[0] || tokens.getSafeFile().length() != byteRange[2] + byteRange[3]) {
                tokens.seek(byteRange[2] + byteRange[3]);
                while (true) {
                    try {
                        if (tokens.nextToken()) {
                            PdfDictionary dict = null;
                            if (tokens.getTokenType() == TokenType.Obj) {
                                PdfObject obj = readObject(true, true);
                                if (obj.isDictionary()) {
                                    dict = (PdfDictionary)obj;
                                }
                            } else if (tokens.getTokenType() == TokenType.StartDic) {
                                dict = readDictionary(true);
                            }
                            if (dict != null) {
                                if (!map.containsKey(dict.getAsName(PdfName.Type))) {
                                    map.put(dict.getAsName(PdfName.Type), new LinkedList());
                                }
                                map.get(dict.getAsName(PdfName.Type)).add(dict);
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
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    } catch (Exception ignore) {
                    }
                }
                if (widgets > 0) {
                    if (widgets == signatures && signatures == (map.containsKey(PdfName.Sig) ? map.get(PdfName.Sig).size() : 0)) {
                        if ((map.containsKey(PdfName.Highlight) ? map.get(PdfName.Highlight).size() : 0) > 0) {
                            return Estado.highlight_agregado;
                        } else {
                            return Estado.widget_firma_agregado;
                        }
                    } else {
                        return Estado.widget_otro_agregado;
                    }
                } else {
                    if (map.containsKey(PdfName.Annot) || map.containsKey(PdfName.Annots) || map.containsKey(PdfName.Highlight)) {
                        return Estado.desconocido_agregado;
                    }
                    if (map.containsKey(PdfName.Sig)) {
                        return Estado.widget_firma_agregado;
                    }
                }
            }
        } catch (IOException ignore) {
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