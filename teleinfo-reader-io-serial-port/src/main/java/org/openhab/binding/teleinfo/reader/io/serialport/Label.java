package org.openhab.binding.teleinfo.reader.io.serialport;

import javax.validation.ConstraintValidator;

import org.openhab.binding.teleinfo.reader.Frame.PeriodeTarifaire;
import org.openhab.binding.teleinfo.reader.FrameOptionHeuresCreuses.GroupeHoraire;

public enum Label {

    ADCO(String.class, 12),
    OPTARIF(String.class, 4),
    BASE(String.class, 8),
    HCHC(Integer.class, 9),
    HCHP(Integer.class, 9),
    EJPHN(Integer.class, 8),
    EJPHPM(Integer.class, 8),
    GAZ(Integer.class, 7),
    AUTRE(Integer.class, 7),
    PTEC(PeriodeTarifaire.class, 4),
    MOTDETAT(String.class, 6),
    ISOUSC(Integer.class, 2),
    IINST(Integer.class, 3),
    ADPS(Integer.class, 3),
    IMAX(Integer.class, 3),
    HHPHC(GroupeHoraire.class, 1),
    PAPP(Integer.class, 5),
    BBRHCJB(Integer.class, 9),
    BBRHPJB(Integer.class, 9),
    BBRHCJW(Integer.class, 9),
    BBRHPJW(Integer.class, 9),
    BBRHCJR(Integer.class, 9),
    BBRHPJR(Integer.class, 9),
    PEJP(Integer.class, 2),
    DEMAIN(String.class, 4);

    private Class type;
    private int size;
    private ConstraintValidator validator; // FIXME

    Label(Class type, int size) {
        this.type = type;
        this.size = size;
    }

    public Class getType() {
        return type;
    }

}
