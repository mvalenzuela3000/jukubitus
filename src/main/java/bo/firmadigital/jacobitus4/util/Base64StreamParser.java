/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus4.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 *
 * @author ADSIB
 */
public final class Base64StreamParser {
    byte[] file = null;
    byte[] remanent = null;
    ByteArrayOutputStream baos;
    final int size = 4096;

    public Base64StreamParser(InputStream is, byte[] buff) throws IOException {
        byte fileContent[] = new byte[size];
        baos = new ByteArrayOutputStream();
        try {
            int pos = 0;
            int len = buff.length;
            while (len > 0) {
                if (pos + len > size) {
                    System.arraycopy(buff, 0, fileContent, pos, size - pos);
                    try {
                        baos.write(Base64.getDecoder().decode(fileContent));
                        System.arraycopy(buff, size - pos, fileContent, 0, len - size + pos);
                        pos = len - size + pos;
                    } catch (IllegalArgumentException ignore) {
                        fileContent = last(fileContent, fileContent.length);
                        remanent = new byte[fileContent.length + len];
                        System.arraycopy(fileContent, 0, remanent, 0, fileContent.length);
                        System.arraycopy(buff, 0, remanent, fileContent.length, len);
                        pos = 0;
                        break;
                    }
                } else {
                    System.arraycopy(buff, 0, fileContent, pos, len);
                    pos = pos + len;
                }
                len = is.read(buff);
            }
            if (pos > 0) {
                remanent = last(fileContent, pos);
            }
            file = baos.toByteArray();
        } finally {
            baos.close();
        }
    }

    public byte[] last(byte[] content, int length) throws IOException {
        byte[] res;
        byte[] buff = new byte[length];
        System.arraycopy(content, 0, buff, 0, length);
        String[] last = new String(buff).split(",");
        if (last.length > 1) {
            buff = new byte[last[0].length() - 1];
            System.arraycopy(last[0].getBytes(), 0, buff, 0, buff.length);
            last[0] = "";
            byte[] lastJson = String.join(",", last).getBytes();
            lastJson[0] = '{';
            res = lastJson;
        } else {
            buff = new byte[last[0].length() - 2];
            System.arraycopy(last[0].getBytes(), 0, buff, 0, buff.length);
            res = "{}".getBytes();
        }
        baos.write(Base64.getDecoder().decode(buff));
        return res;
    }

    public byte[] getFile() {
        return file;
    }

    public byte[] getRemanent() {
        return remanent;
    }
}
