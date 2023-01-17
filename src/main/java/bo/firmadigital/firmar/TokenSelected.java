/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.firmar;

import bo.firmadigital.token.Slot;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author ADSIB
 */
public class TokenSelected {
    private Slot slot;
    private String alias;
    private String pin;
    private String ci;
    private JSONArray files;
    private boolean shown;

    public Slot getSlot() {
        return slot;
    }

    public void setSlot(Slot slot) {
        this.slot = slot;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCI() {
        return ci;
    }

    public void setCI(String ci) {
        this.ci = ci;
    }

    public JSONArray getFiles() {
        return files;
    }

    public void setFiles(JSONArray files) {
        this.files = files;
    }

    public boolean isShown() {
        return shown;
    }

    public synchronized void showAndWait() {
        try {
            shown = true;
            wait();
            shown = false;
        } catch (InterruptedException ex) {
            Logger.getLogger(TokenSelected.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
