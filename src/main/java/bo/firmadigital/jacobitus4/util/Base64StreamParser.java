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
        byte surp[] = new byte[size - buff.length];
        int len = is.read(surp);
        while (len > 0 && len < surp.length) {
            byte fixed[] = new byte[surp.length - len];
            int l = is.read(fixed);
            if (l == 0) {
                break;
            }
            System.arraycopy(fixed, 0, surp, len, l);
            len += l;
        }
        byte fileContent[] = new byte[size];
        System.arraycopy(buff, 0, fileContent, 0, buff.length);
        if (len > 0) {
            System.arraycopy(surp, 0, fileContent, buff.length, len);
            len = buff.length + len;
        } else {
            len = buff.length;
        }
        try {
            baos = new ByteArrayOutputStream();
            if (len == size) {
                try {
                    baos.write(Base64.getDecoder().decode(fileContent));
                    while ((len = is.read(fileContent)) > 0) {
                        if (len < size) {
                            do {
                                surp = new byte[size - len];
                                int l = is.read(surp);
                                if (l == -1) {
                                    surp = new byte[len];
                                    System.arraycopy(fileContent, 0, surp, 0, len);
                                    fileContent = surp;
                                    break;
                                } else {
                                    System.arraycopy(surp, 0, fileContent, len, l);
                                    len += l;
                                }
                            } while (len < size);
                        }
                        baos.write(Base64.getDecoder().decode(fileContent));
                    }
                } catch (IllegalArgumentException ignore) {
                    remanent = last(fileContent, fileContent.length);
                }
            } else {
                remanent = last(fileContent, len);
            }
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                os.write(remanent);
                while ((len = is.read(fileContent)) > 0) {
                    os.write(fileContent, 0, len);
                }
                remanent = os.toByteArray();
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
        String[] last = new String(buff).replace("\n", "").split(",");
        if (last.length > 1) {
            buff = new byte[last[0].length() - 1];
            System.arraycopy(last[0].getBytes(), 0, buff, 0, buff.length);
            last[0] = "";
            byte[] lastJson = String.join(",", last).getBytes();
            lastJson[0] = '{';
            res = lastJson;
        } else {
            buff = new byte[last[0].replace("\"", "").replace("}", "").trim().length()];
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
