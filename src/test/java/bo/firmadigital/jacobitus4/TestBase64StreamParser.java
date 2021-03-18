/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4;

import bo.firmadigital.jacobitus4.util.Base64StreamParser;
import java.io.ByteArrayInputStream;
import java.util.Base64;
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
}
