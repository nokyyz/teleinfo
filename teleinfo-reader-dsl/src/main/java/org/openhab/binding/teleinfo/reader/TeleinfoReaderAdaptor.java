package org.openhab.binding.teleinfo.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.teleinfo.reader.dsl.Frame;

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

    protected void fireOnOpeningEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onOpening(this);
        }
    }

    protected void fireOnOpenedEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onOpened(this);
        }
    }

    protected void fireOnClosingEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onClosing(this);
        }
    }

    protected void fireOnClosedEvent() {
        for (TeleinfoReaderListener listener : listeners) {
            listener.onClosed(this);
        }
    }

    public List<TeleinfoReaderListener> getListeners() {
        return Collections.unmodifiableList(listeners);
    }

}
