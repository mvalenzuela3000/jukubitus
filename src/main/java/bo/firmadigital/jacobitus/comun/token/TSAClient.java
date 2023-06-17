/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.comun.token;

import com.itextpdf.io.codec.Base64;
import com.itextpdf.io.util.SystemUtil;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.signatures.DigestAlgorithms;
import com.itextpdf.signatures.ITSAClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

/**
 *
 * @author ADSIB
 */
public class TSAClient implements ITSAClient {
    protected int tokenSizeEstimate = 4096;
    protected String digestAlgorithm = "SHA-256";

    protected final String tsaURL;
    protected final String tsaJWT;

    public TSAClient(String tsaURL, String tsaJWT) {
        this.tsaURL = tsaURL;
        this.tsaJWT = tsaJWT;
    }

    @Override
    public int getTokenSizeEstimate() {
        return tokenSizeEstimate;
    }

    @Override
    public MessageDigest getMessageDigest() throws GeneralSecurityException {
        return SignUtils.getMessageDigest(digestAlgorithm);
    }

    @Override
    public byte[] getTimeStampToken(byte[] imprint) throws Exception {
        byte[] respBytes;
        // Setup the time stamp request
        TimeStampRequestGenerator tsqGenerator = new TimeStampRequestGenerator();
        tsqGenerator.setCertReq(true);
        // tsqGenerator.setReqPolicy("1.3.6.1.4.1.601.10.3.1");
        BigInteger nonce = BigInteger.valueOf(SystemUtil.getTimeBasedSeed());
        TimeStampRequest request = tsqGenerator.generate(new ASN1ObjectIdentifier(DigestAlgorithms.getAllowedDigest(digestAlgorithm)), imprint, nonce);
        byte[] requestBytes = request.getEncoded();

        // Call the communications layer
        respBytes = getTSAResponse(requestBytes);

        // Handle the TSA response
        TimeStampResponse response = new TimeStampResponse(respBytes);

        // validate communication level attributes (RFC 3161 PKIStatus)
        response.validate(request);
        PKIFailureInfo failure = response.getFailInfo();
        int value = (failure == null) ? 0 : failure.intValue();
        if (value != 0) {
            // @todo: Translate value of 15 error codes defined by PKIFailureInfo to string
            throw new PdfException(PdfException.InvalidTsa1ResponseCode2).setMessageParams(tsaURL, String.valueOf(value));
        }

        // extract just the time stamp token (removes communication status info)
        TimeStampToken tsToken = response.getTimeStampToken();
        if (tsToken == null) {
            throw new PdfException(PdfException.Tsa1FailedToReturnTimeStampToken2).setMessageParams(tsaURL, response.getStatusString());
        }
        byte[] encoded = tsToken.getEncoded();

        // Update our token size estimate for the next call (padded to be safe)
        this.tokenSizeEstimate = encoded.length + 32;
        return encoded;
    }

    protected byte[] getTSAResponse(byte[] requestBytes) throws IOException {
        // Setup the TSA connection
        SignUtils.TsaResponse response = SignUtils.getTsaResponseForUserRequest(tsaURL, requestBytes, tsaJWT);
        // Get TSA response as a byte array
        InputStream inp = response.tsaResponseStream;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inp.read(buffer, 0, buffer.length)) >= 0) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] respBytes = baos.toByteArray();

        if (response.encoding != null && response.encoding.toLowerCase().equals("base64".toLowerCase())) {
            respBytes = Base64.decode(new String(respBytes, "US-ASCII"));
        }
        return respBytes;
    }
}
