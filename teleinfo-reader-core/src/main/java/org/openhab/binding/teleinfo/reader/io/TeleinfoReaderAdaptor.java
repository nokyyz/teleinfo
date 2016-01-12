package org.openhab.binding.teleinfo.reader.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.teleinfo.reader.Frame;

public abstract class TeleinfoReaderAdaptor implements TeleinfoReader {

    private List<TeleinfoReaderListener> listeners = new ArrayList<>();

    @Override
    public void addListener(final TeleinfoReaderListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(final TeleinfoReaderListener listener) {
        listeners.remove(listener);
    }

    protected void fireOnFrameReceivedEvent(final Frame frame) {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onFrameReceived(this, frame);
        }
    }

    public List<TeleinfoReaderListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

}
