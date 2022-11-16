/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.token;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.signatures.BouncyCastleDigest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 *
 * @author ADSIB
 */
public class SignUtils {
    static MessageDigest getMessageDigest(String hashAlgorithm) throws GeneralSecurityException {
        return new BouncyCastleDigest().getMessageDigest(hashAlgorithm);
    }

    static class TsaResponse {
        String encoding;
        InputStream tsaResponseStream;
    }

    static TsaResponse getTsaResponseForUserRequest(String tsaUrl, byte[] requestBytes, String tsaJWT) throws IOException {
        URL url = new URL(tsaUrl);
        URLConnection tsaConnection;
        try {
            tsaConnection = url.openConnection();
        }
        catch (IOException ioe) {
            throw new PdfException(PdfException.FailedToGetTsaResponseFrom1).setMessageParams(tsaUrl);
        }
        tsaConnection.setDoInput(true);
        tsaConnection.setDoOutput(true);
        tsaConnection.setUseCaches(false);
        tsaConnection.setRequestProperty("Content-Type", "application/timestamp-query");
        tsaConnection.setRequestProperty("Content-Transfer-Encoding", "binary");

        if ((tsaJWT != null) && !tsaJWT.equals("") ) {
            tsaConnection.setRequestProperty("Authorization", tsaJWT);
        }
        try (OutputStream out = tsaConnection.getOutputStream()) {
            out.write(requestBytes);
        }

        TsaResponse response = new TsaResponse();
        response.tsaResponseStream = tsaConnection.getInputStream();
        response.encoding = tsaConnection.getContentEncoding();
        return response;
    }
}
