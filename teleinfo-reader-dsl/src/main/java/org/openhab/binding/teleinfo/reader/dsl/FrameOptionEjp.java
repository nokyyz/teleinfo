package org.openhab.binding.teleinfo.reader.dsl;

import java.io.Serializable;

public class FrameOptionEjp implements Serializable {

    private static final long serialVersionUID = -1934715078822532494L;

    // EJP HN : Index heures normales si option = EJP (en Wh)
    // EJP HPM : Index heures de pointe mobile si option = EJP (en Wh)
    // PEJP : Préavis EJP si option = EJP 30mn avant période EJP

    public FrameOptionEjp() {
        // default constructor
    }

}
