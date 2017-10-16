package org.openhab.binding.teleinfo.reader.io.utils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.Assert;
import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.dsl.Frame.PeriodeTarifaire;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses.GroupeHoraire;

public class TestUtils {

    private TestUtils() {
        // private constructor
    }

    public static File getTestFile(String testResourceName) {
        URL url = TestUtils.class.getClassLoader().getResource(testResourceName);
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void assertFrameOptionHeuresCreusesEquals(String expectedADCO,
            Integer expectedAvertissementDepassementPuissanceSouscrite,
            PeriodeTarifaire expectedPeriodeTarifaireEnCours, GroupeHoraire expectedGroupeHoraire,
            Integer expectedIndexHeuresCreuses, Integer expectedIndexHeuresPleines, int expectedIntensiteInstantanee,
            Integer expectedIntensiteMaximale, int expectedIntensiteSouscrite, int expectedPuissanceApparente,
            String expectedMotEtat, Frame actualFrame) {

        Assert.assertNotNull(actualFrame);
        Assert.assertTrue(actualFrame instanceof FrameOptionHeuresCreuses);
        FrameOptionHeuresCreuses actualFrameOptionHC = (FrameOptionHeuresCreuses) actualFrame;
        Assert.assertEquals(expectedADCO, actualFrameOptionHC.getADCO());
        Assert.assertEquals(expectedAvertissementDepassementPuissanceSouscrite,
                actualFrameOptionHC.getAvertissementDepassementPuissanceSouscrite());
        Assert.assertEquals(PeriodeTarifaire.HP, expectedPeriodeTarifaireEnCours);
        Assert.assertEquals(GroupeHoraire.A, expectedGroupeHoraire);
        Assert.assertEquals(expectedIndexHeuresCreuses, actualFrameOptionHC.getIndexHeuresCreuses());
        Assert.assertEquals(expectedIndexHeuresPleines, actualFrameOptionHC.getIndexHeuresPleines());
        Assert.assertEquals(expectedIntensiteInstantanee, actualFrameOptionHC.getIntensiteInstantanee());
        Assert.assertEquals(expectedIntensiteMaximale, actualFrameOptionHC.getIntensiteMaximale());
        Assert.assertEquals(expectedIntensiteSouscrite, actualFrameOptionHC.getIntensiteSouscrite());
        Assert.assertEquals(expectedPuissanceApparente, actualFrameOptionHC.getPuissanceApparente());
        Assert.assertEquals(expectedMotEtat, actualFrameOptionHC.getMotEtat());
    }
}
