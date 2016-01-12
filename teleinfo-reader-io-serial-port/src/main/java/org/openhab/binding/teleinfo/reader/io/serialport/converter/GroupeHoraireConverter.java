package org.openhab.binding.teleinfo.reader.io.serialport.converter;

import org.openhab.binding.teleinfo.reader.FrameOptionHeuresCreuses.GroupeHoraire;
import org.openhab.binding.teleinfo.reader.io.serialport.ConvertionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroupeHoraireConverter implements Converter {

    private static Logger LOGGER = LoggerFactory.getLogger(GroupeHoraireConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        LOGGER.debug("convert(String) [start]");
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("value = " + value);
        }

        GroupeHoraire convertedValue = null;
        switch (value) {
            case "A":
                convertedValue = GroupeHoraire.A;
                break;
            case "C":
                convertedValue = GroupeHoraire.C;
                break;
            case "D":
                convertedValue = GroupeHoraire.D;
                break;
            case "E":
                convertedValue = GroupeHoraire.E;
                break;
            case "Y":
                convertedValue = GroupeHoraire.Y;
                break;
            default:
                throw new ConvertionException(value);
        }

        LOGGER.debug("convert(String) [end]");
        return convertedValue;
    }

}
