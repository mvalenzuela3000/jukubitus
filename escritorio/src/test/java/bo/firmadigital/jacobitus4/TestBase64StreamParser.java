/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus.utilidades.Base64StreamParser;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author ADSIB
 */
public class TestBase64StreamParser {
    @Test
    public void jsonParserCutJson() throws Exception {
        byte[] file = new byte[3060];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"bloquear\":false}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"bloquear\":false}"));
        }
    }

    @Test
    public void jsonParserNonCut() throws Exception {
        byte[] file = new byte[3070];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"bloquear\":false}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"bloquear\":false}"));
        }
    }

    @Test
    public void jsonParserCutB64() throws Exception {
        byte[] file = new byte[3200];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"bloquear\":false}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"bloquear\":false}"));
        }
    }

    @Test
    public void jsonParserCustom() throws Exception {
        byte[] file = new byte[107513];
        String json = "{\"alias\":\"660681795331\",\"pdf\":\"" + Base64.getEncoder().encodeToString(file) + "\",\"slot\":1,\"pin\":\"12345678\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{\"slot\":1,\"pin\":\"12345678\"}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCustom1() throws Exception {
        byte[] file = new byte[125930];
        String json = "{\"pdf\":\"" + Base64.getEncoder().encodeToString(file) + "\",\"alias\":\"66068179533\",\"slot\":1,\"pin\":\"12345678\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{\"alias\":\"66068179533\",\"slot\":1,\"pin\":\"12345678\"}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCustom2() throws Exception {
        byte[] file = new byte[107513];
        byte[] image = new byte[15000];
        String json = "{\"alias\":\"6606817953\",\"pdf\":\"" + Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"slot\":1,\"pin\":\"12345678\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"slot\":1,\"pin\":\"12345678\"}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCustom3() throws Exception {
        byte[] file;
        String json = "{\"alias\":\"6606817953\",\"pdf\":\"SG9sYQ==\",\"image\":\"SG9sYQ==\",\"slot\":1,\"pin\":\"12345678\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{\"image\":\"SG9sYQ==\",\"slot\":1,\"pin\":\"12345678\"}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCustom4() throws Exception {
        byte[] file;
        String json = "{\"pdf\":\"SG9sYQ==\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCustom5() throws Exception {
        byte[] file;
        String json = "{\"pdf\":\"VGhlIHN0YW5kYXJkIExvcmVtIElwc3VtIHBhc3NhZ2UsIHVzZWQgc2luY2UgdGhlIDE1MDBzICJMb3JlbSBpcHN1bSBkb2xvciBzaXQgYW1ldCwgY29uc2VjdGV0dXIgYWRpcGlzY2luZyBlbGl0LCBzZWQgZG8gZWl1c21vZCB0ZW1wb3IgaW5jaWRpZHVudCB1dCBsYWJvcmUgZXQgZG9sb3JlIG1hZ25hIGFsaXF1YS4gVXQgZW5pbSBhZCBtaW5pbSB2ZW5pYW0sIHF1aXMgbm9zdHJ1ZCBleGVyY2l0YXRpb24gdWxsYW1jbyBsYWJvcmlzIG5pc2kgdXQgYWxpcXVpcCBleCBlYSBjb21tb2RvIGNvbnNlcXVhdC4gRHVpcyBhdXRlIGlydXJlIGRvbG9yIGluIHJlcHJlaGVuZGVyaXQgaW4gdm9sdXB0YXRlIHZlbGl0IGVzc2UgY2lsbHVtIGRvbG9yZSBldSBmdWdpYXQgbnVsbGEgcGFyaWF0dXIuIEV4Y2VwdGV1ciBzaW50IG9jY2FlY2F0IGN1cGlkYXRhdCBub24gcHJvaWRlbnQsIHN1bnQgaW4gY3VscGEgcXVpIG9mZmljaWEgZGVzZXJ1bnQgbW9sbGl0IGFuaW0gaWQgZXN0IGxhYm9ydW0uIiBTZWN0aW9uIDEuMTAuMzIgb2YgImRlIEZpbmlidXMgQm9ub3J1bSBldCBNYWxvcnVtIiwgd3JpdHRlbiBieSBDaWNlcm8gaW4gNDUgQkMgIlNlZCB1dCBwZXJzcGljaWF0aXMgdW5kZSBvbW5pcyBpc3RlIG5hdHVzIGVycm9yIHNpdCB2b2x1cHRhdGVtIGFjY3VzYW50aXVtIGRvbG9yZW1xdWUgbGF1ZGFudGl1bSwgdG90YW0gcmVtIGFwZXJpYW0sIGVhcXVlIGlwc2EgcXVhZSBhYiBpbGxvIGludmVudG9yZSB2ZXJpdGF0aXMgZXQgcXVhc2kgYXJjaGl0ZWN0byBiZWF0YWUgdml0YWUgZGljdGEgc3VudCBleHBsaWNhYm8uIE5lbW8gZW5pbSBpcHNhbSB2b2x1cHRhdGVtIHF1aWEgdm9sdXB0YXMgc2l0IGFzcGVybmF0dXIgYXV0IG9kaXQgYXV0IGZ1Z2l0LCBzZWQgcXVpYSBjb25zZXF1dW50dXIgbWFnbmkgZG9sb3JlcyBlb3MgcXVpIHJhdGlvbmUgdm9sdXB0YXRlbSBzZXF1aSBuZXNjaXVudC4gTmVxdWUgcG9ycm8gcXVpc3F1YW0gZXN0LCBxdWkgZG9sb3JlbSBpcHN1bSBxdWlhIGRvbG9yIHNpdCBhbWV0LCBjb25zZWN0ZXR1ciwgYWRpcGlzY2kgdmVsaXQsIHNlZCBxdWlhIG5vbiBudW1xdWFtIGVpdXMgbW9kaSB0ZW1wb3JhIGluY2lkdW50IHV0IGxhYm9yZSBldCBkb2xvcmUgbWFnbmFtIGFsaXF1YW0gcXVhZXJhdCB2b2x1cHRhdGVtLiBVdCBlbmltIGFkIG1pbmltYSB2ZW5pYW0sIHF1aXMgbm9zdHJ1bSBleGVyY2l0YXRpb25lbSB1bGxhbSBjb3Jwb3JpcyBzdXNjaXBpdCBsYWJvcmlvc2FtLCBuaXNpIHV0IGFsaXF1aWQgZXggZWEgY29tbW9kaSBjb25zZXF1YXR1cj8gUXVpcyBhdXRlbSB2ZWwgZXVtIGl1cmUgcmVwcmVoZW5kZXJpdCBxdWkgaW4gZWEgdm9sdXB0YXRlIHZlbGl0IGVzc2UgcXVhbSBuaWhpbCBtb2xlc3RpYWUgY29uc2VxdWF0dXIsIHZlbCBpbGx1bSBxdWkgZG9sb3JlbSBldW0gZnVnaWF0IHF1byB2b2x1cHRhcyBudWxsYSBwYXJpYXR1cj8iIDE5MTQgdHJhbnNsYXRpb24gYnkgSC4gUmFja2hhbSAiQnV0IEkgbXVzdCBleHBsYWluIHRvIHlvdSBob3cgYWxsIHRoaXMgbWlzdGFrZW4gaWRlYSBvZiBkZW5vdW5jaW5nIHBsZWFzdXJlIGFuZCBwcmFpc2luZyBwYWluIHdhcyBib3JuIGFuZCBJIHdpbGwgZ2l2ZSB5b3UgYSBjb21wbGV0ZSBhY2NvdW50IG9mIHRoZSBzeXN0ZW0sIGFuZCBleHBvdW5kIHRoZSBhY3R1YWwgdGVhY2hpbmdzIG9mIHRoZSBncmVhdCBleHBsb3JlciBvZiB0aGUgdHJ1dGgsIHRoZSBtYXN0ZXItYnVpbGRlciBvZiBodW1hbiBoYXBwaW5lc3MuIE5vIG9uZSByZWplY3RzLCBkaXNsaWtlcywgb3IgYXZvaWRzIHBsZWFzdXJlIGl0c2VsZiwgYmVjYXVzZSBpdCBpcyBwbGVhc3VyZSwgYnV0IGJlY2F1c2UgdGhvc2Ugd2hvIGRvIG5vdCBrbm93IGhvdyB0byBwdXJzdWUgcGxlYXN1cmUgcmF0aW9uYWxseSBlbmNvdW50ZXIgY29uc2VxdWVuY2VzIHRoYXQgYXJlIGV4dHJlbWVseSBwYWluZnVsLiBOb3IgYWdhaW4gaXMgdGhlcmUgYW55b25lIHdobyBsb3ZlcyBvciBwdXJzdWVzIG9yIGRlc2lyZXMgdG8gb2J0YWluIHBhaW4gb2YgaXRzZWxmLCBiZWNhdXNlIGl0IGlzIHBhaW4sIGJ1dCBiZWNhdXNlIG9jY2FzaW9uYWxseSBjaXJjdW1zdGFuY2VzIG9jY3VyIGluIHdoaWNoIHRvaWwgYW5kIHBhaW4gY2FuIHByb2N1cmUgaGltIHNvbWUgZ3JlYXQgcGxlYXN1cmUuIFRvIHRha2UgYSB0cml2aWFsIGV4YW1wbGUsIHdoaWNoIG9mIHVzIGV2ZXIgdW5kZXJ0YWtlcyBsYWJvcmlvdXMgcGh5c2ljYWwgZXhlcmNpc2UsIGV4Y2VwdCB0byBvYnRhaW4gc29tZSBhZHZhbnRhZ2UgZnJvbSBpdD8gQnV0IHdobyBoYXMgYW55IHJpZ2h0IHRvIGZpbmQgZmF1bHQgd2l0aCBhIG1hbiB3aG8gY2hvb3NlcyB0byBlbmpveSBhIHBsZWFzdXJlIHRoYXQgaGFzIG5vIGFubm95aW5nIGNvbnNlcXVlbmNlcywgb3Igb25lIHdobyBhdm9pZHMgYSBwYWluIHRoYXQgcHJvZHVjZXMgbm8gcmVzdWx0YW50IHBsZWFzdXJlPyIgU2VjdGlvbiAxLjEwLjMzIG9mICJkZSBGaW5pYnVzIEJvbm9ydW0gZXQgTWFsb3J1bSIsIHdyaXR0ZW4gYnkgQ2ljZXJvIGluIDQ1IEJDICJBdCB2ZXJvIGVvcyBldCBhY2N1c2FtdXMgZXQgaXVzdG8gb2RpbyBkaWduaXNzaW1vcyBkdWNpbXVzIHF1aSBibGFuZGl0aWlzIHByYWVzZW50aXVtIHZvbHVwdGF0dW0gZGVsZW5pdGkgYXRxdWUgY29ycnVwdGkgcXVvcyBkb2xvcmVzIGV0IHF1YXMgbW9sZXN0aWFzIGV4Y2VwdHVyaSBzaW50IG9jY2FlY2F0aSBjdXBpZGl0YXRlIG5vbiBwcm92aWRlbnQsIHNpbWlsaXF1ZSBzdW50IGluIGN1bHBhIHF1aSBvZmZpY2lhIGRlc2VydW50IG1vbGxpdGlhIGFuaW1pLCBpZCBlc3QgbGFib3J1bSBldCBkb2xvcnVtIGZ1Z2EuIEV0IGhhcnVtIHF1aWRlbSByZXJ1bSBmYWNpbGlzIGVzdCBldCBleHBlZGl0YSBkaXN0aW5jdGlvLiBOYW0gbGliZXJvIHRlbXBvcmUsIGN1bSBzb2x1dGEgbm9iaXMgZXN0IGVsaWdlbmRpIG9wdGlvIGN1bXF1ZSBuaWhpbCBpbXBlZGl0IHF1byBtaW51cyBpZCBxdW9kIG1heGltZSBwbGFjZWF0IGZhY2VyZSBwb3NzaW11cywgb21uaXMgdm9sdXB0YXMgYXNzdW1lbmRhIGVzdCwgb21uaXMgZG9sb3IgcmVwZWxsZW5kdXMuIFRlbXBvcmlidXMgYXV0ZW0gcXVpYnVzZGFtIGV0IGF1dCBvZmZpY2lpcyBkZWJpdGlzIGF1dCByZXJ1bSBuZWNlc3NpdGF0aWJ1cyBzYWVwZSBldmVuaWV0IHV0IGV0IHZvbHVwdGF0ZXMgcmVwdWRpYW5kYWUgc2ludCBldCBtb2xlc3RpYWUgbm9uIHJlY3VzYW5kYWUuIEl0YXF1ZSBlYXJ1bSByZXJ1bSBoaWMgdGVuZXR1ciBhIHNhcGllbnRlIGRlbGVjdHVzLCB1dCBhdXQgcmVpY2llbmRpcyB2b2x1cHRhdGlidXMgbWFpb3JlcyBhbGlhcyBjb25zZXF1YXR1ciBhdXQgcGVyZmVyZW5kaXMgZG9sb3JpYnVzIGFzcGVyaW9yZXMgcmVwZWxsYXQuIiAxOTE0IHRyYW5zbGF0aW9uIGJ5IEguIFJhY2toYW0gIk9uIHRoZSBvdGhlciBoYW5kLCB3ZSBkZW5vdW5jZSB3aXRoIHJpZ2h0ZW91cyBpbmRpZ25hdGlvbiBhbmQgZGlzbGlrZSBtZW4gd2hvIGFyZSBzbyBiZWd1aWxlZCBhbmQgZGVtb3JhbGl6ZWQgYnkgdGhlIGNoYXJtcyBvZiBwbGVhc3VyZSBvZiB0aGUgbW9tZW50LCBzbyBibGluZGVkIGJ5IGRlc2lyZSwgdGhhdCB0aGV5IGNhbm5vdCBmb3Jlc2VlIHRoZSBwYWluIGFuZCB0cm91YmxlIHRoYXQgYXJlIGJvdW5kIHRvIGVuc3VlOyBhbmQgZXF1YWwgYmxhbWUgYmVsb25ncyB0byB0aG9zZSB3aG8gZmFpbCBpbiB0aGVpciBkdXR5IHRocm91Z2ggd2Vha25lc3Mgb2Ygd2lsbCwgd2hpY2ggaXMgdGhlIHNhbWUgYXMgc2F5aW5nIHRocm91Z2ggc2hyaW5raW5nIGZyb20gdG9pbCBhbmQgcGFpbi4gVGhlc2UgY2FzZXMgYXJlIHBlcmZlY3RseSBzaW1wbGUgYW5kIGVhc3kgdG8gZGlzdGluZ3Vpc2guIEluIGEgZnJlZSBob3VyLCB3aGVuIG91ciBwb3dlciBvZiBjaG9pY2UgaXMgdW50cmFtbWVsbGVkIGFuZCB3aGVuIG5vdGhpbmcgcHJldmVudHMgb3VyIGJlaW5nIGFibGUgdG8gZG8gd2hhdCB3ZSBsaWtlIGJlc3QsIGV2ZXJ5IHBsZWFzdXJlIGlzIHRvIGJlIHdlbGNvbWVkIGFuZCBldmVyeSBwYWluIGF2b2lkZWQuIEJ1dCBpbiBjZXJ0YWluIGNpcmN1bXN0YW5jZXMgYW5kIG93aW5nIHRvIHRoZSBjbGFpbXMgb2YgZHV0eSBvciB0aGUgb2JsaWdhdGlvbnMgb2YgYnVzaW5lc3MgaXQgd2lsbCBmcmVxdWVudGx5IG9jY3VyIHRoYXQgcGxlYXN1cmVzIGhhdmUgdG8gYmUgcmVwdWRpYXRlZCBhbmQgYW5ub3lhbmNlcyBhY2NlcHRlZC4gVGhlIHdpc2UgbWFuIHRoZXJlZm9yZSBhbHdheXMgaG9sZHMgaW4gdGhlc2UgbWF0dGVycyB0byB0aGlzIHByaW5jaXBsZSBvZiBzZWxlY3Rpb246IGhlIHJlamVjdHMgcGxlYXN1cmVzIHRvIHNlY3VyZSBvdGhlciBncmVhdGVyIHBsZWFzdXJlcywgb3IgZWxzZSBoZSBlbmR1cmVzIHBhaW5zIHRvIGF2b2lkIHdvcnNlIHBhaW5zLgo=\"}";
        ByteArrayInputStream body = new ByteArrayInputStream(json.getBytes());
        JsonFactory factory = new ObjectMapper().getJsonFactory();
        JsonParser jsonReader = factory.createJsonParser(body);
        jsonReader.nextToken();
        while (jsonReader.nextToken() == JsonToken.FIELD_NAME) {
            String label = jsonReader.getText();
            jsonReader.nextToken();
            switch (label) {
                case "pdf":
                    try (InputStream is = (InputStream)jsonReader.getInputSource()) {
                        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                            jsonReader.releaseBuffered(os);
                            byte[] buff = os.toByteArray();
                            Base64StreamParser parser = new Base64StreamParser(is, buff);
                            file = parser.getRemanent();
                            jsonReader.close();
                        }
                    }
                    Assert.assertTrue(new String(file).equals("{}"));
                    return;
                default:
                    break;
            }
        }
    }

    @Test
    public void jsonParserCutJsonShortImg() throws Exception {
        byte[] file = new byte[3060];
        byte[] image = new byte[50];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}"));
        }
    }

    @Test
    public void jsonParserNonCutShortImg() throws Exception {
        byte[] file = new byte[3070];
        byte[] image = new byte[50];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}"));
        }
    }

    @Test
    public void jsonParserCutB64ShortImg() throws Exception {
        byte[] file = new byte[3200];
        byte[] image = new byte[50];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}"));
        }
    }

    @Test
    public void jsonParserCutJsonLongImg() throws Exception {
        byte[] file = new byte[3060];
        byte[] image = new byte[15000];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"y\":20,\"x\":20}\"}"));
        }
    }

    @Test
    public void jsonParserNonCutLongImg() throws Exception {
        byte[] file = new byte[3070];
        byte[] image = new byte[15000];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}"));
        }
    }

    @Test
    public void jsonParserCutB64LongtImg() throws Exception {
        byte[] file = new byte[3200];
        byte[] image = new byte[15000];
        String json = Base64.getEncoder().encodeToString(file) + "\",\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}";
        try (ByteArrayInputStream is = new ByteArrayInputStream(json.getBytes())) {
            byte[] buff = new byte[1024];
            is.read(buff);
            Base64StreamParser parser = new Base64StreamParser(is, buff);
            Assert.assertTrue(new String(parser.getRemanent()).equals("{\"image\":\"" + Base64.getEncoder().encodeToString(image) + "\",\"point\":\"{\"x\":20,\"y\":20}\"}"));
        }
    }
}
