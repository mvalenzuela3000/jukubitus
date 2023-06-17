/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bo.firmadigital.jacobitus.validador;

import java.util.Date;

/**
 *
 * @author ADSIB
 */
public class OCSPData {
    private final Validador.OCSPState state;
        private final Date date;

        public OCSPData(Validador.OCSPState state, Date date) {
            this.state = state;
            this.date = date;
        }

        public Validador.OCSPState getState() {
            return state;
        }

        public Date getDate() {
            return date;
        }
}
