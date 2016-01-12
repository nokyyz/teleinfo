package org.openhab.binding.teleinfo.reader;

public class FrameOptionTempo extends Frame {

    private Integer indexHeuresCreusesJoursBleus; // BBR HC JB : Index heures creuses jours bleus si option = tempo (en
                                                  // Wh)

    private Integer indexHeuresPleinesJoursBleus; // BBR HP JB : Index heures pleines jours bleus si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresCreusesJoursBlancs;// BBR HC JW : Index heures creuses jours blancs si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresPleinesJoursBlancs;// BBR HC JW : Index heures pleines jours blancs si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresCreusesJoursRouges;// BBR HC JR : Index heures creuses jours rouges si option = tempo (en
                                                  // Wh)
    private Integer indexHeuresPleinesJoursRouges;// BBR HP JR : Index heures pleines jours rouges si option = tempo (en
                                                  // Wh)
    private String couleurLendemain; // DEMAIN : Couleur du lendemain si option = tempo
    private String groupeHoraire; // HHPHC : Groupe horaire si option = heures creuses ou tempo

    public FrameOptionTempo() {
        // default constructor
    }

}
