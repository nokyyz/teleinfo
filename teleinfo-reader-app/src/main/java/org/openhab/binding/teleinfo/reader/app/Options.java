package org.openhab.binding.teleinfo.reader.app;

import org.kohsuke.args4j.Option;

public class Options {

    @Option(name = "-serialPort", required = true, usage = "The serial port name of Teleinfo stream")
    private String serialPort;

    @Option(name = "-refreshInterval", required = true, usage = "The frequency between 2 data read (in millisecond)")
    private long refreshInterval;

    public String getSerialPort() {
        return serialPort;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

}
