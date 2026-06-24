package bo.firmadigital.jacobitus.escritorio.comun;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jettison.json.JSONArray;

import bo.firmadigital.jacobitus.token.Slot;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenSelected {
    private Slot[] slots;
    private Slot slot;
    private String alias;
    private String pin;
    private String CI;
    private JSONArray files;
    private JSONArray filesJson;
    @Setter(AccessLevel.NONE)
    private boolean shown;

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
