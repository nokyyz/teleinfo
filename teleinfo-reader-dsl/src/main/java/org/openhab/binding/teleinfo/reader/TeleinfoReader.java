package org.openhab.binding.teleinfo.reader;

import java.io.Closeable;
import java.io.IOException;

public interface TeleinfoReader extends Closeable {

    void open() throws IOException;

    @Override
    void close() throws IOException;

    void addListener(final TeleinfoReaderListener listener);

    void removeListener(final TeleinfoReaderListener listener);

}
