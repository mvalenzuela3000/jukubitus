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
