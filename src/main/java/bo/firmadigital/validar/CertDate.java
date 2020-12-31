package bo.firmadigital.validar;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

public class CertDate {
    private final Certificate certificate;
    private final Calendar signDate;
    private final Calendar timeStamp;
    private final boolean bloquea;

    private DatosCertificado datos;

    private boolean valid = false;
    private boolean pki = false;
    private boolean ocsp = false;

    public CertDate(Certificate certificate, Calendar signDate, Calendar timeStamp, boolean bloquea) {
        this.certificate = certificate;
        this.signDate = signDate;
        this.timeStamp = timeStamp;
        this.bloquea = bloquea;

        try {
            datos = new DatosCertificado((X509Certificate) certificate);
        } catch (CertificateEncodingException ignore) {
        }
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public DatosCertificado getDatos() {
        return datos;
    }

    public Date getSignDate() {
        return signDate.getTime();
    }

    public Date getTimeStamp() {
        if (timeStamp == null) {
            return null;
        } else {
            return timeStamp.getTime();
        }
    }

    public boolean isBloquea() {
        return bloquea;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setPKI(boolean pki) {
        this.pki = pki;
    }

    public boolean isPKI() {
        return pki;
    }

    public boolean isActive() {
        if (getTimeStamp() == null) {
            return ((X509Certificate) certificate).getNotBefore().compareTo(getSignDate()) < 0 && ((X509Certificate) certificate).getNotAfter().compareTo(getSignDate()) > 0;
        } else {
            return ((X509Certificate) certificate).getNotBefore().compareTo(getTimeStamp()) < 0 && ((X509Certificate) certificate).getNotAfter().compareTo(getTimeStamp()) > 0;
        }
    }

    public void setOCSP(boolean ocsp) {
        this.ocsp = ocsp;
    }

    public boolean isOCSP() {
        return ocsp;
    }

    public boolean isOk() {
        return isValid() && isPKI() && isActive() && isOCSP();
    }
}
