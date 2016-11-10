package org.openhab.binding.teleinfo.reader;

import org.openhab.binding.teleinfo.reader.dsl.Frame;

public abstract class TeleinfoReaderListenerAdaptor implements TeleinfoReaderListener {

    @Override
    public void onFrameReceived(TeleinfoReader reader, Frame frame) {
        // NOP
    }

    @Override
    public void onOpening(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onOpened(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onClosing(TeleinfoReader reader) {
        // NOP
    }

    @Override
    public void onClosed(TeleinfoReader reader) {
        // NOP
    }

}
