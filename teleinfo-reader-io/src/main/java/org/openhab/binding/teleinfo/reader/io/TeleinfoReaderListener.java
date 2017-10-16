package org.openhab.binding.teleinfo.reader.io;

import org.openhab.binding.teleinfo.reader.dsl.Frame;

public interface TeleinfoReaderListener {

    void onFrameReceived(final TeleinfoReader reader, final Frame frame);

    void onOpening(final TeleinfoReader reader);

    void onOpened(final TeleinfoReader reader);

    void onClosing(final TeleinfoReader reader);

    void onClosed(final TeleinfoReader reader);

}
