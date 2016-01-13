package org.openhab.binding.teleinfo.network.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.openhab.binding.teleinfo.reader.Frame;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderAdaptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoReaderClient extends TeleinfoReaderAdaptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoReaderClient.class);

    private InetAddress serverAddress;
    private int serverPort;
    private NioSocketConnector connector;
    private IoSession session;

    public TeleinfoReaderClient(final InetAddress serverAddress, final int serverPort) {
        if (serverAddress == null) {
            throw new IllegalArgumentException("serverAddress argument cannot be null");
        }

        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    @Override
    public void open() throws IOException {
        LOGGER.debug("open() [start]");

        if (session != null && session.isConnected()) {
            throw new IllegalStateException("Already connected. Disconnect first");
        }

        LOGGER.info("Client starting...");
        fireOnOpeningEvent();
        long startTime = new Date().getTime();

        connector = new NioSocketConnector();

        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new TeleinfoFrameProtocolEncoder(), new TeleinfoFrameProtocolDecoder()));

        connector.setHandler(new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message) throws Exception {
                fireOnFrameReceivedEvent((Frame) message);
            }

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                LOGGER.debug("sessionClosed...");
                close();
            }
        });
        ConnectFuture connection = connector.connect(new InetSocketAddress(serverAddress, serverPort));
        connection.awaitUninterruptibly();
        session = connection.getSession();

        long endTime = new Date().getTime();
        LOGGER.info("Client connected to {}:{}' server (in {} ms)", serverAddress.toString(), serverPort,
                endTime - startTime);
        fireOnOpenedEvent();

        LOGGER.debug("open() [end]");
    }

    @Override
    public void close() throws IOException {
        LOGGER.debug("close() [start]");
        fireOnClosingEvent();

        session.close(true);
        session = null;
        connector.dispose(true);
        connector = null;

        fireOnClosedEvent();
        LOGGER.debug("close() [end]");
    }
}
