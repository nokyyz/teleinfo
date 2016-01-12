package org.openhab.binding.teleinfo.reader;

import java.io.Serializable;

public class FrameOptionHeuresCreuses extends Frame implements Serializable {

    public static enum GroupeHoraire {
        A,
        C,
        D,
        E,
        Y
    };

    private static final long serialVersionUID = -1934715078822532494L;

    private Integer indexHeuresCreuses; // HCHC : Index heures creuses si option = heures creuses (en Wh)
    private Integer indexHeuresPleines; // HCHP : Index heures pleines si option = heures creuses (en Wh)
    private GroupeHoraire groupeHoraire; // HHPHC : Groupe horaire si option = heures creuses ou tempo

    public FrameOptionHeuresCreuses() {
        // default constructor
    }

    public Integer getIndexHeuresCreuses() {
        return indexHeuresCreuses;
    }

    public void setIndexHeuresCreuses(Integer indexHeuresCreuses) {
        this.indexHeuresCreuses = indexHeuresCreuses;
    }

    public Integer getIndexHeuresPleines() {
        return indexHeuresPleines;
    }

    public void setIndexHeuresPleines(Integer indexHeuresPleines) {
        this.indexHeuresPleines = indexHeuresPleines;
    }

    public GroupeHoraire getGroupeHoraire() {
        return groupeHoraire;
    }

    public void setGroupeHoraire(GroupeHoraire groupeHoraire) {
        this.groupeHoraire = groupeHoraire;
    }

    @Override
    public String toString() {
        return "FrameOptionHeuresCreuses [timestamp=" + getTimestamp() + ", ADCO=" + getADCO() + ", indexHeuresCreuses="
                + indexHeuresCreuses + " Wh, indexHeuresPleines=" + indexHeuresPleines + " Wh, intensiteInstantanee="
                + getIntensiteInstantanee() + " A, puissanceApparente=" + getPuissanceApparente()
                + " VA, periodeTarifaireEnCours=" + getPeriodeTarifaireEnCours() + "]";
    }
}
