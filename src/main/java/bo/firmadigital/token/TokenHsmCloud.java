/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.token;

import bo.firmadigital.jacobitus4.util.Config;
import bo.firmadigital.pkcs11.CK_TOKEN_INFO;
import bo.firmadigital.token.provider.HsmProvider;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author ADSIB
 */
public class TokenHsmCloud implements Token {
    private String PIN;
    private final Config config;

    public TokenHsmCloud() {
        config = new Config();
        if (Security.getProvider("HSM Cloud") == null) {
            Security.addProvider(new HsmProvider());
        }
    }

    @Override
    public void iniciar(String pin) throws GeneralSecurityException {
        this.PIN = pin;
    }

    @Override
    public void salir() { }

    @Override
    public String getProviderName() {
        return "HsmCloud";
    }

    @Override
    public void modificarPin(String oldPin, String newPin) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unlockPin(String osPin, String newPin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PublicKey generarClaves(String clavesId, String pin, int slotNumber) throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String generarCSR(String alias, JSONArray subject) throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void eliminarClaves(String clavesId) throws KeyStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cargarClaves(PrivateKey priv, PublicKey pub, String clavesId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cargarCertificado(X509Certificate certificado, String clavesId) throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void cargarCertificado(String pem, String clavesId) throws GeneralSecurityException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void eliminarCertificado(String clavesId) throws KeyStoreException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> listarIdentificadorClaves() throws KeyStoreException {
        List<String> claves = new ArrayList<>();
        try {
            JSONObject body = new JSONObject();
            body.put("tipo_hsm", "HSM");
            body.put("pin", PIN);
            JSONObject response = request(config.getHsmCloud() + "/listar_claves", config.getHsmJWT(), "POST", body.toString());
            JSONArray arr = response.getJSONObject("data").getJSONArray("claveprivadas");
            for (int i = 0; i < arr.length(); i++) {
                claves.add(arr.getJSONObject(i).getString("alias"));
            }
            return claves;
        } catch (JSONException ex) {
            throw new KeyStoreException(ex.getMessage());
        }
    }

    @Override
    public List<Certificate> listarCertificados() throws GeneralSecurityException {
        List<Certificate> certificados = new ArrayList<>();
        try {
            JSONObject body = new JSONObject();
            body.put("tipo_hsm", "HSM");
            body.put("pin", PIN);
            JSONObject response = request(config.getHsmCloud() + "/listar_claves", config.getHsmJWT(), "POST", body.toString());
            JSONArray arr = response.getJSONObject("data").getJSONArray("claveprivadas");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < arr.length(); i++) {
                String pem = arr.getJSONObject(i).getJSONObject("certificado").getString("pem");
                certificados.add((X509Certificate)cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(pem))));
            }
            return certificados;
        } catch (JSONException ex) {
            throw new KeyStoreException(ex.getMessage());
        }
    }

    @Override
    public boolean existeCertificadoClaves(String clavesId) throws KeyStoreException {
        List<String> labels = listarIdentificadorClaves();
        return labels.contains(clavesId);
    }

    @Override
    public X509Certificate obtenerCertificado(String clavesId) throws KeyStoreException {
        try {
            JSONObject body = new JSONObject();
            body.put("tipo_hsm", "HSM");
            body.put("pin", PIN);
            body.put("alias", clavesId);
            JSONObject response = request(config.getHsmCloud() + "/certificado", config.getHsmJWT(), "POST", body.toString());
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate)cf.generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(response.getJSONObject("data").getString("certificate"))));
        } catch (JSONException | CertificateException ex) {
            throw new KeyStoreException(ex.getMessage());
        }
    }

    @Override
    public PrivateKey obtenerClavePrivada(String clavesId) throws GeneralSecurityException {
        return new HsmPrivateKey(config.getHsmCloud(), config.getHsmJWT(), PIN, clavesId);
    }

    @Override
    public PublicKey obtenerClavePublica(String clavesId) throws GeneralSecurityException {
        return obtenerCertificado(clavesId).getPublicKey();
    }

    @Override
    public Certificate[] getCertificateChain(String clavesId) throws GeneralSecurityException {
        return new Certificate[]{obtenerCertificado(clavesId)};
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

    public class HsmPrivateKey implements PrivateKey {
        private final String url;
        private final String jwt;
        private final String pin;
        private final String alias;

        public HsmPrivateKey(String url, String jwt, String pin, String alias) {
            this.url = url;
            this.jwt = jwt;
            this.pin = pin;
            this.alias = alias;
        }

        @Override
        public String getAlgorithm() {
            return "RSA";
        }

        @Override
        public String getFormat() {
            return "HSM Cloud";
        }

        @Override
        public byte[] getEncoded() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public final byte[] sign(byte[] sh) throws SignatureException {
            try {
                JSONObject body = new JSONObject();
                body.put("tipo_hsm", "HSM");
                body.put("pin", PIN);
                body.put("alias", alias);
                JSONObject data = new JSONObject();
                data.put("hash", Base64.getEncoder().encodeToString(sh).replace("\n", ""));
                body.put("data", data);
                JSONObject response = request(config.getHsmCloud() + "/firmar_pkcs7", config.getHsmJWT(), "POST", body.toString());
                return Base64.getDecoder().decode(response.getJSONObject("data").getString("signature"));
            } catch (JSONException ex) {
                throw new SignatureException(ex.getMessage());
            }
        }

        public String getUrl() {
            return url;
        }

        public String getJwt() {
            return jwt;
        }

        public String getPin() {
            return pin;
        }

        public String getAlias() {
            return alias;
        }
    }

    public static CK_TOKEN_INFO getTokenInfo() {
        return new CK_TOKEN_INFO(-1001, new SoftInfo());
    }

    public static class SoftInfo {
        public char[] label;
        public char[] manufacturerID;
        public char[] model;
        public char[] serialNumber;
        public long flags;
        public long ulMaxSessionCount;
        public long ulSessionCount;
        public long ulMaxRwSessionCount;
        public long ulRwSessionCount;
        public long ulMaxPinLen;
        public long ulMinPinLen;
        public long ulTotalPublicMemory;
        public long ulFreePublicMemory;
        public long ulTotalPrivateMemory;
        public long ulFreePrivateMemory;
        public Version hardwareVersion; 
        public Version firmwareVersion;
        public char[] utcTime;

        public SoftInfo() {
            label = "HSM ADSIB".toCharArray();
            manufacturerID = "ADSIB".toCharArray();
            model = "1.0".toCharArray();
            serialNumber = "ADSIB".toCharArray();
            hardwareVersion = new Version();
            firmwareVersion = new Version();
            utcTime = "".toCharArray();
        }

        public class Version {
            public byte major;
            public byte minor;
        }
    }
}
