package org.openhab.binding.teleinfo.reader.app;

import org.kohsuke.args4j.Option;

public class Options {

    @Option(name = "-serialPort", required = true, usage = "The serial port name of Teleinfo stream")
    private String serialPort;

    @Option(name = "-refreshInterval", required = true, usage = "The frequency between 2 data read (in millisecond)")
    private long refreshInterval;

    @Option(name = "-installAsService", required = false, usage = "Install app as a service (only Linux os)", depends = "-serviceRunAs")
    private boolean installAsService;

    @Option(name = "-serviceRunAs", usage = "User used by Service app (only Linux os)")
    private String serviceRunAs;

    @Option(name = "-useSerialPortSymbolicLink", required = false, usage = "Enable the serial port symbolic link creation", depends = "-serialPortSymbolicLinkTarget")
    private boolean useSerialPortSymbolicLink;

    @Option(name = "-serialPortSymbolicLinkTarget", usage = "Path of target serial port")
    private String serialPortSymbolicLinkTarget;

    public String getSerialPort() {
        return serialPort;
    }

    public long getRefreshInterval() {
        return refreshInterval;
    }

    public boolean isInstallAsService() {
        return installAsService;
    }

    public String getServiceRunAs() {
        return serviceRunAs;
    }

    public boolean isUseSerialPortSymbolicLink() {
        return useSerialPortSymbolicLink;
    }

    public String getSerialPortSymbolicLinkTarget() {
        return serialPortSymbolicLinkTarget;
    }
}
