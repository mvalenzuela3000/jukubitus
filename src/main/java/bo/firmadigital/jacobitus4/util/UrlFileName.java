/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.util;

import java.io.IOException;
import java.net.URLConnection;
import java.util.StringTokenizer;

/**
 *
 * @author ADSIB
 */
public class UrlFileName {
    public static final String extractFileNameFromContentDisposition(URLConnection url) {
        String contentDisposition = url.getHeaderField("Content-Disposition");
        if (contentDisposition == null) {
            return null;
        }
        String[] attributes = contentDisposition.split(";");
        for (String a : attributes) {
            if (a.toLowerCase().contains("filename")) {
                try {
                    return a.substring(a.indexOf('\"') + 1, a.lastIndexOf('\"'));
                } catch (Exception e) {
                    return a.substring(a.indexOf('=') + 1, a.length());
                }
            }
        }
        return null;
    }

    public static final String getFileName(URLConnection url) throws IOException {
        String fileName = extractFileNameFromContentDisposition(url);
        if (fileName == null) {
            StringTokenizer st = new StringTokenizer(url.getURL().getFile(), "/");
            while (st.hasMoreTokens()) {
                fileName = st.nextToken();
            }
        }
        if (fileName == null) {
            return "file.pdf";
        } else {
            return fileName;
        }
    }
}
