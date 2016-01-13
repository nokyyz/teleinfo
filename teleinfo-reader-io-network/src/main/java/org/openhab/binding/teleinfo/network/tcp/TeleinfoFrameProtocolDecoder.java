package org.openhab.binding.teleinfo.network.tcp;

import java.nio.charset.CharsetDecoder;
import java.util.Date;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.openhab.binding.teleinfo.network.tcp.ConstantsCodec.FrameType;
import org.openhab.binding.teleinfo.reader.Frame;
import org.openhab.binding.teleinfo.reader.Frame.PeriodeTarifaire;
import org.openhab.binding.teleinfo.reader.FrameOptionBase;
import org.openhab.binding.teleinfo.reader.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.reader.FrameOptionHeuresCreuses.GroupeHoraire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeleinfoFrameProtocolDecoder extends CumulativeProtocolDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeleinfoFrameProtocolDecoder.class);

    private static final CharsetDecoder CHARSET_DECODER = ConstantsCodec.CHARSET_CODEC.newDecoder();

    private Integer bodySize = null;

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
        LOGGER.debug("doDecode(IoSession, IoBuffer, ProtocolDecoderOutput) [start]");
        boolean doDecode = false;

        if (bodySize == null) {
            bodySize = in.getInt();
        }
        LOGGER.trace("in.remaining() = " + in.remaining());
        LOGGER.trace("decodedBodySize = " + bodySize);

        if (in.remaining() >= bodySize) {
            FrameType frameType = in.getEnum(FrameType.class);
            LOGGER.trace("frameType = " + frameType);

            Frame decodedFrame = null;
            switch (frameType) {
                case FrameOptionHeuresCreuses:
                    decodedFrame = decodeFrameOptionHeuresCreuses(in);
                    break;
                case FrameOptionBase:
                    decodedFrame = decodeFrameOptionBase(in);
                    break;
                default:
                    throw new IllegalStateException("not yet implemented"); // FIXME
            }

            LOGGER.trace("decodedFrame = " + decodedFrame);
            out.write(decodedFrame);
            if (in.remaining() > 0) {
                in.skip(in.remaining());
            }

            bodySize = null;
            doDecode = true;
        }

        LOGGER.debug("doDecode(IoSession, IoBuffer, ProtocolDecoderOutput) [end]");
        return doDecode;
    }

    private FrameOptionHeuresCreuses decodeFrameOptionHeuresCreuses(IoBuffer in) throws Exception {
        FrameOptionHeuresCreuses frame = new FrameOptionHeuresCreuses();
        decodeCommonsFields(in, frame);
        frame.setGroupeHoraire(in.getEnum(GroupeHoraire.class));
        frame.setIndexHeuresCreuses(in.getInt());
        frame.setIndexHeuresPleines(in.getInt());

        return frame;
    }

    private FrameOptionBase decodeFrameOptionBase(IoBuffer in) throws Exception {
        FrameOptionBase frame = new FrameOptionBase();
        decodeCommonsFields(in, frame);
        frame.setIndexBase(in.getInt());

        return frame;
    }

    private void decodeCommonsFields(IoBuffer buffer, Frame frame) throws Exception {
        frame.setTimestamp(decodeDate(buffer));
        frame.setADCO(decodeVariableString(buffer));
        frame.setIntensiteInstantanee(buffer.getInt());
        frame.setIntensiteSouscrite(buffer.getInt());
        frame.setIntensiteMaximale(buffer.getInt());
        frame.setPuissanceApparente(buffer.getInt());
        frame.setPeriodeTarifaireEnCours(buffer.getEnum(PeriodeTarifaire.class));
        frame.setAvertissementDepassementPuissanceSouscrite(decodeNullableInteger(buffer));
        frame.setMotEtat(decodeVariableString(buffer));
    }

    private Integer decodeNullableInteger(IoBuffer buffer) throws Exception {
        char isNull = buffer.getChar();
        if (isNull == '1') {
            return buffer.getInt();
        } else {
            return null;
        }
    }

    private String decodeVariableString(IoBuffer buffer) throws Exception {
        int stringSize = buffer.getInt();
        return buffer.getString(stringSize, CHARSET_DECODER);
    }

    private Date decodeDate(IoBuffer buffer) throws Exception {
        char isNull = buffer.getChar();
        if (isNull == '1') {
            return new Date(buffer.getLong());
        } else {
            return null;
        }
    }
}
