package org.openhab.binding.teleinfo.reader.io.stream.internal.converter;

import org.openhab.binding.teleinfo.reader.dsl.Frame.PeriodeTarifaire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodeTarifaireConverter implements Converter {

    private static Logger LOGGER = LoggerFactory.getLogger(PeriodeTarifaireConverter.class);

    @Override
    public Object convert(String value) throws ConvertionException {
        LOGGER.debug("convert(String) [start]");
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("value = " + value);
        }

        PeriodeTarifaire convertedValue = null;
        switch (value) {
            case "TH..":
                convertedValue = PeriodeTarifaire.TH;
                break;
            case "HC..":
                convertedValue = PeriodeTarifaire.HC;
                break;
            case "HP..":
                convertedValue = PeriodeTarifaire.HP;
                break;
            case "HN..":
                convertedValue = PeriodeTarifaire.HN;
                break;
            case "PM..":
                convertedValue = PeriodeTarifaire.PM;
                break;
            default:
                throw new ConvertionException(value);
        }

        LOGGER.debug("convert(String) [end]");
        return convertedValue;
    }

}
