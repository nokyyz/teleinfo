package org.openhab.binding.teleinfo.broadcast.tcpip.core;

import java.nio.charset.CharsetEncoder;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.openhab.binding.teleinfo.broadcast.tcpip.core.ConstantsCodec.FrameType;
import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionBase;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionEjp;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionTempo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoFrameProtocolEncoder implements ProtocolEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoFrameProtocolEncoder.class);

    private static final CharsetEncoder CHARSET_ENCODER = ConstantsCodec.CHARSET_CODEC.newEncoder();

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        LOGGER.debug("encode(IoSession, Object, ProtocolEncoderOutput) [start]");

        IoBuffer bodyBuffer = IoBuffer.allocate(10, false);
        bodyBuffer.setAutoExpand(true);
        if (message instanceof FrameOptionHeuresCreuses) {
            encode(bodyBuffer, (FrameOptionHeuresCreuses) message);
        } else if (message instanceof FrameOptionBase) {
            encode(bodyBuffer, (FrameOptionBase) message);
        } else if (message instanceof FrameOptionEjp) {
            // FIXME
            throw new IllegalStateException("Teleinfo frame not supported");
        } else if (message instanceof FrameOptionTempo) {
            // FIXME
            throw new IllegalStateException("Teleinfo frame not supported");
        } else {
            throw new IllegalStateException("Teleinfo frame not supported");
        }

        int bodySize = bodyBuffer.limit();
        LOGGER.trace("bodySize = " + bodySize);

        IoBuffer messageBuffer = IoBuffer.allocate(4 + bodySize);
        messageBuffer.setAutoExpand(true);
        messageBuffer.putInt(bodySize);
        messageBuffer.put(bodyBuffer.array());
        bodyBuffer.clear();
        bodyBuffer = null;

        messageBuffer.flip();

        LOGGER.trace("messageBuffer.limit() = " + messageBuffer.limit());

        out.write(messageBuffer);
        LOGGER.debug("encode(IoSession, Object, ProtocolEncoderOutput) [end]");

    }

    @Override
    public void dispose(IoSession session) throws Exception {
        // NOP
    }

    private void encode(IoBuffer buffer, FrameOptionHeuresCreuses frame) throws Exception {
        buffer.putEnum(FrameType.FrameOptionHeuresCreuses);
        encodeCommonsFields(buffer, frame);
        buffer.putEnum(frame.getGroupeHoraire());
        buffer.putInt(frame.getIndexHeuresCreuses());
        buffer.putInt(frame.getIndexHeuresPleines());
    }

    private void encode(IoBuffer buffer, FrameOptionBase frame) throws Exception {
        buffer.putEnum(FrameType.FrameOptionBase);
        encodeCommonsFields(buffer, frame);
        buffer.putInt(frame.getIndexBase());
    }

    private void encodeCommonsFields(IoBuffer buffer, Frame frame) throws Exception {
        encodeDate(buffer, frame.getTimestamp());
        encodeVariableString(buffer, frame.getId().toString());
        encodeVariableString(buffer, frame.getADCO());
        buffer.putInt(frame.getIntensiteInstantanee());
        buffer.putInt(frame.getIntensiteSouscrite());
        buffer.putInt(frame.getIntensiteMaximale());
        buffer.putInt(frame.getPuissanceApparente());
        buffer.putEnum(frame.getPeriodeTarifaireEnCours());
        encodeNullableInteger(buffer, frame.getAvertissementDepassementPuissanceSouscrite());
        encodeVariableString(buffer, frame.getMotEtat());
    }

    private void encodeNullableInteger(IoBuffer buffer, Integer value) throws Exception {
        buffer.putChar(value == null ? '0' : '1');
        if (value != null) {
            buffer.putInt(value);
        }
    }

    private void encodeVariableString(IoBuffer buffer, String value) throws Exception {
        int stringSize = value.length();
        buffer.putInt(stringSize);
        buffer.putString(value, stringSize, CHARSET_ENCODER);
    }

    private void encodeDate(IoBuffer buffer, Date value) throws Exception {
        buffer.putChar(value == null ? '0' : '1');
        if (value != null) {
            buffer.putLong(value.getTime());
        }
    }
}
