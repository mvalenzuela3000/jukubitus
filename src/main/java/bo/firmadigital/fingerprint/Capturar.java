/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.fingerprint;

import com.digitalpersona.onetouch.DPFPGlobal;
import com.digitalpersona.onetouch.capture.DPFPCapture;
import com.digitalpersona.onetouch.capture.DPFPCapturePriority;
import com.digitalpersona.onetouch.capture.event.DPFPDataEvent;
import com.digitalpersona.onetouch.readers.DPFPReadersCollection;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jnbis.Bitmap;
import org.jnbis.WSQEncoder;

/**
 *
 * @author ADSIB
 */
public class Capturar {
    public static void capturar(Fingerprint fingerprint) {
        DPFPReadersCollection readers = DPFPGlobal.getReadersFactory().getReaders();
        if (readers.size() != 1) {
            throw new RuntimeException("No se econtró el lector de huellas.");
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
                    throw new RuntimeException(ex.getMessage());
                }
            }
        });
        capture.startCapture();
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
