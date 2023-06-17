/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.comun.token.hsm;

import java.security.Provider;

/**
 *
 * @author ADSIB
 */
public class HsmProvider extends Provider {
    public HsmProvider() {
        super("HSM Cloud", 1.0, "HSM CLoud Provider v1.0");
        put("Signature.SHA256withRSA", HsmSignature.class.getName());
    }
}
