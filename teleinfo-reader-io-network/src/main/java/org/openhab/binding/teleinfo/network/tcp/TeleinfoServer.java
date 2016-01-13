package org.openhab.binding.teleinfo.network.tcp;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.openhab.binding.teleinfo.reader.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoServer.class);

    private InetAddress serverAddress;
    private int serverPort;
    private IoAcceptor acceptor;

    public TeleinfoServer(final int serverPort) {
        this(null, serverPort);
    }

    public TeleinfoServer(final InetAddress serverAddress, final int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void start() throws Exception {
        LOGGER.debug("start() [start]");

        LOGGER.info("Server starting...");
        long startTime = new Date().getTime();

        acceptor = new NioSocketAcceptor();

        LoggingFilter loggerFilter = new LoggingFilter();
        loggerFilter.setSessionCreatedLogLevel(LogLevel.DEBUG);
        loggerFilter.setSessionOpenedLogLevel(LogLevel.DEBUG);
        acceptor.getFilterChain().addLast("logger", loggerFilter);
        acceptor.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TeleinfoFrameProtocolEncoder(), new TeleinfoFrameProtocolDecoder()));

        acceptor.setHandler(new TeleinfoServerHandler());

        acceptor.getSessionConfig().setReadBufferSize(2048 * 1000);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);

        acceptor.bind(new InetSocketAddress(serverAddress, serverPort));
        acceptor.setCloseOnDeactivation(true);

        long endTime = new Date().getTime();
        LOGGER.info("Server started (in {} ms)", endTime - startTime);
        LOGGER.info("Listening on port " + serverPort);

        LOGGER.debug("start() [end]");
    }

    public void stop() {
        LOGGER.debug("stop() [start]");

        LOGGER.info("Server stopping...");
        acceptor.unbind();
        acceptor.dispose(true);
        acceptor = null;
        LOGGER.info("Server stopped");

        LOGGER.debug("stop() [end]");
    }

    public boolean isOnline() {
        return acceptor != null && acceptor.isActive();
    }

    /**
     * Send the given frame to all connected clients.
     * Remarks: the frame is not sent if no client is connected
     *
     * @param frameToBroadcast teleinfo frame to send to the clients
     */
    public void broadcast(final Frame frameToBroadcast) {
        if (frameToBroadcast == null) {
            throw new IllegalArgumentException("frame to broadcast can't be null");
        }

        if (acceptor.getManagedSessionCount() == 0) {
            LOGGER.warn("Teleinfo frame not sent because no client connected");
            return;
        }

        LOGGER.debug("Teleinfo frame sending...");
        acceptor.broadcast(frameToBroadcast);
        LOGGER.debug("Teleinfo frame sent");
    }
}
