/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import com.nimbusds.jose.JWSObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 *
 * @author ADSIB
 */
public enum MagicBytes {
    XML(0x3C),
    PNG(0x89, 0x50),
    JPG(0xFF, 0xD8),
    P7S(0x30, 0x80),
    PDF(0x25, 0x50);

    private static final String b64 = "+-./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz\r\n";

    private final int[] magicBytes;

    private MagicBytes(int...bytes) {
        magicBytes = bytes;
    }

    public boolean is(byte[] bytes) {
        if (bytes.length != magicBytes.length)
            throw new RuntimeException("I need the first " + magicBytes.length + " bytes of an input stream.");
        for (int i = 0; i < bytes.length; i++) {
            if (Byte.toUnsignedInt(bytes[i]) != magicBytes[i])
                return false;
        }
        return true;
    }

    // Extracts head bytes from any stream
    public static byte[] extract(InputStream is, int length) throws IOException {
        is.mark(0);
        byte[] buffer = new byte[length];
        is.read(buffer, 0, length);
        if (is.markSupported()) {
            is.reset();
        }
        return buffer;
    }

    /* Convenience methods */
    public boolean is(File file) throws IOException {
        boolean res;
        try (FileInputStream fis = new FileInputStream(file)) {
            res = is(new FileInputStream(file));
        }
        return res;
    }

    public boolean is(InputStream is) throws IOException {
        return is(extract(is, magicBytes.length));
    }

    public static boolean isJWS(File file) throws IOException {
        boolean res = true;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int l = fis.read(buffer);
            for (int i = 0; i < l; i++) {
                if (b64.indexOf((char)buffer[i]) == -1) {
                    res = false;
                    break;
                }
            }
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                os.write(buffer, 0, l);
                while ((l = fis.read(buffer)) > 0) {
                    os.write(buffer, 0, l);
                }
                try {
                    JWSObject.parse(new String(os.toByteArray()));
                } catch (ParseException ex) {
                    res = false;
                }
            }
        }
        return res;
    }
}
