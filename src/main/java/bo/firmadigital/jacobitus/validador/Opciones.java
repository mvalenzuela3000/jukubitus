package bo.firmadigital.jacobitus.validador;

public class Opciones {
    private Boolean proxyHabilitado;
    private String servidorProxy;
    private Integer puertoServidorProxy;

    public Opciones() {
    }

    public Boolean getProxyHablitado() {
        return this.proxyHabilitado;
    }

    public void setProxyHabilitado(Boolean valor) {
        this.proxyHabilitado = valor;
    }
    
    public String getServidorProxy() {
        return this.servidorProxy;
    }

    public void setServidorProxy(String valor) {
        this.servidorProxy = valor;
    }

    public Integer getPuertoServidorProxy() {
        return this.puertoServidorProxy;
    }

    public void setPuertoServidorProxy(Integer valor) {
        this.puertoServidorProxy = valor;
    }
}
