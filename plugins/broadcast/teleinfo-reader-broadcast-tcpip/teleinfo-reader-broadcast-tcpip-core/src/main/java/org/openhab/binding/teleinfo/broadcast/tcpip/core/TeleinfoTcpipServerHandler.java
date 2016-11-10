package org.openhab.binding.teleinfo.broadcast.tcpip.core;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoTcpipServerHandler implements IoHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoTcpipServerHandler.class);

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
        logTotalOpenedSession(session);
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
        logTotalOpenedSession(session);
    }

    private void logTotalOpenedSession(IoSession session) {
        LOGGER.info("Total opened session on server: {}", session.getService().getManagedSessionCount());
    }
}
