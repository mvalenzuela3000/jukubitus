package bo.firmadigital.jacobitus4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Request {
    X509TrustManager trustManager;
    X509Certificate server;

    protected TrustManager[] trustCerts = new TrustManager[] {
        new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return trustManager.getAcceptedIssuers();
            }
            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                trustManager.checkClientTrusted(certs, authType);
            }
            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
                try {
                    trustManager.checkServerTrusted(certs, authType);
                } catch (CertificateException ex) {
                    try {
                        certs[0].verify(server.getPublicKey());
                    } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException | SignatureException ex1) {
                        throw ex;
                    }
                }
            }
        }
    };

    public Request() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            trustManager = null;
            for (TrustManager tm : trustManagerFactory.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    trustManager = (X509TrustManager) tm;
                    break;
                }
            }
        }  catch (NoSuchAlgorithmException | KeyStoreException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            server = (X509Certificate)certFactory.generateCertificate(this.getClass().getClassLoader().getResourceAsStream("server.crt"));
        } catch (CertificateException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, trustCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hv = (String urlHostName, SSLSession session) -> {
                if (!urlHostName.equalsIgnoreCase(session.getPeerHost())) {
                    Logger.getLogger(Request.class.getName()).log(Level.WARNING, "URL host '" + urlHostName + "' is different to SSLSession host '" + session.getPeerHost() + "'.");
                }
                return true;
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(Request.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONObject request(String ruta, String method, String body, String token) {
        JSONObject res = new JSONObject();
        try {
            URL url = new URL(ruta);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            if (token != null) {
                connection.setRequestProperty("Authorization", token);
            }
            connection.setRequestProperty("Content-Type", "application/json");

            if (!method.equals("GET")) {
                connection.setDoOutput(true);
                try (DataOutputStream request = new DataOutputStream(connection.getOutputStream())) {
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(request, "UTF-8"))) {
                        bw.write(body);
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
            JSONObject json = new JSONObject(stringBuilder.toString().replaceAll("\n", "").trim());
            res.put("data", json);
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

    public void enviar(File file, String post, String token) {
        JSONObject body = new JSONObject();
        try {
            byte[] b = Files.readAllBytes(file.toPath());
            body.put("base64", Base64.getEncoder().encodeToString(b));
            JSONObject res = request(post, "POST", body.toString(), token);
            if (res.getInt("code") != 201) {
                throw new RuntimeException("No fue posible enviar el archivo firmado.");
            }
        } catch (IOException | JSONException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public boolean estado() {
        JSONObject res = request("https://localhost:9000/api/status", "GET", null, null);
        try {
            return res.getInt("code") == 200;
        } catch (JSONException ex) {
            return false;
        }
    }
    
    public void show() {
        request("https://localhost:9000/api/app/show", "POST", "{}", null);
    }

    public static JSONObject splitQuery(String query) throws UnsupportedEncodingException, JSONException {
        JSONObject query_pairs = new JSONObject();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

    public void show(String url) {
        if (url.startsWith("app")) {
            try {
                String[] parts = url.split("\\?");
                if (parts.length == 2) {
                    String body = splitQuery(parts[1]).toString();
                    request("https://localhost:9000/api/app/show", "POST", body, null);
                } else {
                    request("https://localhost:9000/api/app/show", "POST", "{\"error\":\"Error al procesar la URL.\"}", null);
                }
            } catch (UnsupportedEncodingException | JSONException ex) {
                request("https://localhost:9000/api/app/show", "POST", "{\"error\":\"Error al procesar la URL.\"}", null);
            }
        } else {
            request("https://localhost:9000/api/app/show", "POST", "{\"file\":\"" + url.replace("\\", "\\\\") + "\"}", null);
        }
    }
}
