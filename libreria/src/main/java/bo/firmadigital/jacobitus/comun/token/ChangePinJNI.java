/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.comun.token;

/**
 *
 * @author ADSIB
 */
public class ChangePinJNI {
    static {
        System.loadLibrary("changepin");
    }

    public native String changePin(String lib, int slot, String oldPin, String newPin);

    public native String changeSoPin(String lib, int slot, String soPin, String newPin);

    public native String unlock(String lib, int slot, String soPin, String newPin);

    public native String test(String lib, int slot, String soPin);
}
