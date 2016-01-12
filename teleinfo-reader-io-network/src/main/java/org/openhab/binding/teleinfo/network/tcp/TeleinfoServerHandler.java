package org.openhab.binding.teleinfo.network.tcp;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.openhab.binding.teleinfo.reader.Frame;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReader;
import org.openhab.binding.teleinfo.reader.io.TeleinfoReaderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoServerHandler implements IoHandler, TeleinfoReaderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoServerHandler.class);

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        // NOP
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        // NOP
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        // NOP
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        // NOP
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {

    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        // NOP
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        // NOP
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        // NOP
        LOGGER.info("Total opened sessions on server: {}", session.getService().getManagedSessionCount());
    }

    @Override
    public void onFrameReceived(final TeleinfoReader reader, final Frame frame) {
        // NOP
    }
}
