package bo.firmadigital.utiles.fingerprint;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.jnbis.Bitmap;
import org.jnbis.WSQEncoder;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;

import bo.firmadigital.jacobitus.comun.JacobitusException;

public class Capturar {
    public static void capturar(Fingerprint fingerprint) {
        DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
        if (readers.size() != 1) {
            throw new JacobitusException("No se econtró el lector de huellas.");
        }
        DPFPCapture capture = DPFPGlobal.getCaptureFactory().createCapture();
        capture.setReaderSerialNumber(readers.get(0).getSerialNumber());
        capture.setPriority(DPFPCapturePriority.CAPTURE_PRIORITY_LOW);
        capture.addDataListener((DPFPDataEvent e) -> {
            if (e != null && e.getSample() != null) {
                Image img = DPFPGlobal.getSampleConversionFactory().createImage(e.getSample());
                capture.stopCapture();
                try {
                    BufferedImage ajusted = ((BufferedImage)img).getSubimage(65, 0, 420, 550);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(ajusted, "bmp", baos);
                    fingerprint.fingerprintCaptured(baos.toByteArray());
                } catch (IOException ex) {
                    throw new JacobitusException(ex.getMessage());
                }
            }
        });
        capture.startCapture();
    }

    @SuppressWarnings("resource")
    public static void capturarLinux(Fingerprint fingerprint) {
        try {
            File file = new File("captured.bmp");
            if (file.exists()) {
                file.delete();
            }
            String lectorCaptura = "/usr/local/dermalog/x64/bin/VC3Console.64";
            Process p;
            p = Runtime.getRuntime().exec(lectorCaptura);
            try (BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                while (in.readLine() != null) { }
                if (file.exists()) {
                    try {
                        p.waitFor();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Capturar.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    InputStream is = new FileInputStream(file);
                    long length = file.length();
                    if (length > Integer.MAX_VALUE) {
                        throw new JacobitusException("La longitud del archivo es demasiado largo");
                    }
                    byte[] bytes = new byte[(int) length];
                    int offset = 0;
                    int numRead = 0;
                    while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                        offset += numRead;
                    }
                    if (offset < bytes.length) {
                        throw new JacobitusException("No se pudo completar la lectura del archivo " + file.getName());
                    }
                    is.close();
                    fingerprint.fingerprintCaptured(bytes);
                } else {
                    throw new JacobitusException("No se pudo capturar la huella");
                }
            }
        } catch (IOException ex) {
            throw new JacobitusException(ex.getMessage());
        }
    }

    public static byte[] toWSQ(byte[] bmp) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bmp));
            DataBufferByte pixels = (DataBufferByte)image.getData().getDataBuffer();
            Bitmap bitmap = new Bitmap(pixels.getData(), image.getWidth(), image.getHeight(), 500, 8, 1);
            ByteArrayOutputStream wsq = new ByteArrayOutputStream();
            float bitrate = 2f;
            WSQEncoder.encode(wsq, bitmap, bitrate, "");
            return wsq.toByteArray();
        }   catch (IOException ignore) {
            return null;
        }
    }

    public interface Fingerprint {
        public void fingerprintCaptured(byte[] image);
    }
}
