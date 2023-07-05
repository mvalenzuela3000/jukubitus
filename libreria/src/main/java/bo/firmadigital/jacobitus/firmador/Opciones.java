package bo.firmadigital.jacobitus.firmador;

import java.io.File;

public class Opciones {
    private File controlador;
    private File token;

    private Boolean selloTiempoHabilitado = false;
    private String apiSelloTiempo;
    private String jwtSelloTiempo;

    private Boolean hsmHabilitado = false;
    private String tipoHsm;
    private String apiHsm;
    private String jwtHsm;

    public Opciones() {
    }

    public File getControlador() {
        return this.controlador;
    }

    public void setControlador(File valor) {
        this.controlador = valor;
    }
    
    public File getToken() {
        return this.token;
    }

    public void setToken(File valor) {
        this.token = valor;
    }

    public Boolean getSelloTiempoHabilitado() {
        return this.selloTiempoHabilitado;
    }

    public void setSelloTiempoHabilitado(Boolean valor) {
        this.selloTiempoHabilitado = valor;
    }
    
    public String getApiSelloTiempo() {
        return this.apiSelloTiempo;
    }

    public void setApiSelloTiempo(String valor) {
        this.apiSelloTiempo = valor;
    }
    
    public String getJwtSelloTiempo() {
        return this.jwtSelloTiempo;
    }

    public void setJwtSelloTiempo(String valor) {
        this.jwtSelloTiempo = valor;
    }

    public Boolean getHsmHabilitado() {
        return this.hsmHabilitado;
    }

    public void setHsmHabilitado(Boolean valor) {
        this.hsmHabilitado = valor;
    }

    public String getTipoHsm() {
        return this.tipoHsm;
    }

    public void setTipoHsm(String valor) {
        this.tipoHsm = valor;
    }

    public String getApiHsm() {
        return this.apiHsm;
    }

    public void setApiHsm(String valor) {
        this.apiHsm = valor;
    }
    
    public String getJwtHsm() {
        return this.jwtHsm;
    }

    public void setJwtHsm(String valor) {
        this.jwtHsm = valor;
    }
}
