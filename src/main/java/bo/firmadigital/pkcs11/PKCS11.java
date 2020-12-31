/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.pkcs11;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;

/**
 *
 * @author ADSIB
 */
public class PKCS11 {
    private Provider prov;
    private Object p11;

    public PKCS11(Provider prov) throws RuntimeException {
        try {
            Field f = prov.getClass().getDeclaredField("p11");
            f.setAccessible(true);
            p11 = f.get(prov);
            this.prov = prov;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public long[] C_GetSlotList(boolean bln) {
        try {
            Method C_GetSlotList = p11.getClass().getDeclaredMethod("C_GetSlotList", boolean.class);
            return (long[])C_GetSlotList.invoke(p11, true);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public CK_TOKEN_INFO C_GetTokenInfo(long l) {
        try {
            Method C_GetTokenInfo = p11.getClass().getDeclaredMethod("C_GetTokenInfo", long.class);
            return new CK_TOKEN_INFO(l, C_GetTokenInfo.invoke(p11, l));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void logout() {
        try {
            Method logout = prov.getClass().getDeclaredMethod("logout");
            logout.invoke(prov);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
