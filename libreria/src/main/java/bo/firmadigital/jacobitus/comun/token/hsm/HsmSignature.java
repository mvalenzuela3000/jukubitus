/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.comun.token.hsm;

import bo.firmadigital.jacobitus.comun.token.TokenHsmCloud.HsmPrivateKey;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.util.Base64;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
public class HsmSignature extends SignatureSpi {
    private String url;
    private String jwt;
    private String pin;
    private String alias;
    private PublicKey publicKey;
    private byte[] data;

    @Override
    protected void engineInitVerify(PublicKey pk) throws InvalidKeyException {
        if (pk instanceof PublicKey) {
            publicKey = pk;
        } else {
            throw new InvalidKeyException("PublicKey unrecoverable.");
        }
    }

    @Override
    protected void engineInitSign(PrivateKey pk) throws InvalidKeyException {
        if (pk instanceof HsmPrivateKey) {
            HsmPrivateKey privateKey = (HsmPrivateKey)pk;
            url = privateKey.getUrl();
            jwt = privateKey.getJwt();
            pin = privateKey.getPin();
            alias = privateKey.getAlias();
        } else {
            throw new InvalidKeyException("PrivateKey unrecoverable.");
        }
    }

    @Override
    protected void engineUpdate(byte b) throws SignatureException {
        data = new byte[1];
        System.arraycopy(b, 0, data, 0, 1);
    }

    @Override
    protected void engineUpdate(byte[] bytes, int i, int f) throws SignatureException {
        data = new byte[f - i];
        System.arraycopy(bytes, i, data, 0, f - i);
    }

    @Override
    protected byte[] engineSign() throws SignatureException {
        try {
            JSONObject body = new JSONObject();
            body.put("tipo_hsm", "HSM");
            body.put("pin", pin);
            body.put("alias", alias);
            JSONObject data = new JSONObject();
            data.put("hash", Base64.getEncoder().encodeToString(this.data).replace("\n", ""));
            body.put("data", data);
            JSONObject response = request(url + "/firmar_pkcs7", jwt, "POST", body.toString());
            return Base64.getDecoder().decode(response.getJSONObject("data").getString("signature"));
        } catch (JSONException ex) {
            throw new SignatureException(ex.getMessage());
        }
    }

    @Override
    protected boolean engineVerify(byte[] bytes) throws SignatureException {
        try {
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(publicKey);
            return sign.verify(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new SignatureException(ex.getMessage());
        }
    }

    @Override
    protected void engineSetParameter(String string, Object o) throws InvalidParameterException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected Object engineGetParameter(String string) throws InvalidParameterException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected JSONObject request(String... params) {
        JSONObject res = new JSONObject();
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(params[2]);
            connection.setRequestProperty("Authorization", params[1]);
            connection.setRequestProperty("Content-Type", "application/json");

            if (!params[2].equals("GET")) {
                connection.setDoOutput(true);
                try (DataOutputStream request = new DataOutputStream(connection.getOutputStream())) {
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(request, "UTF-8"))) {
                        bw.write(params[3]);
                    }
                    request.flush();
                }
            }

            res.put("code", connection.getResponseCode());

            InputStream responseStream;
            if (res.getInt("code") >= HttpURLConnection.HTTP_OK &&
                    res.getInt("code") <= HttpURLConnection.HTTP_PARTIAL) {
                responseStream = connection.getInputStream();
            } else {
                responseStream = connection.getErrorStream();
            }

            StringBuilder stringBuilder;
            try (BufferedReader responseStreamReader = new BufferedReader(new InputStreamReader(responseStream, "UTF-8"))) {
                String line;
                stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
            }

            responseStream.close();
            connection.disconnect();
            if (res.getInt("code") >= HttpURLConnection.HTTP_OK &&
                    res.getInt("code") <= HttpURLConnection.HTTP_PARTIAL) {
                JSONObject datos = new JSONObject(stringBuilder.toString().replaceAll("\n", "").trim()).getJSONObject("datos");
                if (datos.has("data")) {
                    res.put("data", datos.getJSONObject("data"));
                } else {
                    res.put("data", datos);
                }
            } else {
                res.put("message", new JSONObject(stringBuilder.toString().replaceAll("\n", "").trim()).getString("mensaje"));
            }
            return res;
        } catch (IOException | JSONException e) {
            JSONObject err = new JSONObject();
            try {
                err.put("code", 402);
                err.put("message", e.getMessage());
            } catch (JSONException ignore) {
            }
            return err;
        }
    }
}
