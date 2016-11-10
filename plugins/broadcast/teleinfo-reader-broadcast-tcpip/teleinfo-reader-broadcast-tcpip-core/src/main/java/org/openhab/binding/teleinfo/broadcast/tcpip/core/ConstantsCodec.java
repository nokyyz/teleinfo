package org.openhab.binding.teleinfo.broadcast.tcpip.core;

import java.nio.charset.Charset;

public class ConstantsCodec {

    public static final Charset CHARSET_CODEC = Charset.forName("UTF-8");

    public static enum FrameType {
        FrameOptionHeuresCreuses,
        FrameOptionBase,
        FrameOptionEjp,
        FrameOptionTempo
    }

}
