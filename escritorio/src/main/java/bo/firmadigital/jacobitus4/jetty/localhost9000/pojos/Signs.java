package bo.firmadigital.jacobitus4.jetty.localhost9000.pojos;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author MEFP
 */
public class Signs {
    private String protect;
    private Map<String, Object> header;
    private String signature;

    /**
     * @return the protect
     */
    @JsonProperty("protected")
    public String getProtect() {
        return protect;
    }

    /**
     * @param protect the protect to set
     */
    public void setProtect(String protect) {
        this.protect = protect;
    }

    /**
     * @return the header
     */
    public Map<String, Object> getHeader() {
        return header;
    }

    /**
     * @param header the header to set
     */
    public void setHeader(Map<String, Object> header) {
        this.header = header;
    }

    /**
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @param signature the signature to set
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
