package org.openhab.binding.teleinfo.reader.io.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.teleinfo.reader.dsl.Frame;
import org.openhab.binding.teleinfo.reader.dsl.Frame.PeriodeTarifaire;
import org.openhab.binding.teleinfo.reader.dsl.FrameOptionHeuresCreuses.GroupeHoraire;
import org.openhab.binding.teleinfo.reader.io.utils.TestUtils;

public class TeleinfoInputStreamTest {

    private final String adco = "031328187235";
    private final int intensiteSouscrite = 30;
    private final long waitNextHeaderFrameTimeoutInMs = TeleinfoInputStream.DEFAULT_TIMEOUT_WAIT_NEXT_HEADER_FRAME * 10;
    private final long readingFrameTimeoutInMs = TeleinfoInputStream.DEFAULT_TIMEOUT_READING_FRAME * 10;

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in normal condition.
     *
     * The stream 'teleinfo-2.raw' starts with an complete frame ; this frame must be read.
     */
    @Test
    public void test1ReadNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        File teleinfoStream2 = TestUtils.getTestFile("teleinfo-stream/teleinfo-2.raw");
        try (TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new FileInputStream(teleinfoStream2),
                waitNextHeaderFrameTimeoutInMs, readingFrameTimeoutInMs);) {

            Frame frame1 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617943, 5, 44, intensiteSouscrite, 1130, "000000", frame1);

            // skip frames until the latest (valid) frame
            for (int i = 1; i < 122; i++) {
                teleinfoIn.readNextFrame();
            }

            Frame frame124 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617982, 5, 44, intensiteSouscrite, 1120, "000000", frame124);

            // read 125th frame (incomplete frame)
            try {
                teleinfoIn.readNextFrame();
                Assert.fail("Frame 125 is incomplete (the last group line 'PA' is incomplete)");
            } catch (InvalidFrameException e) {
                // NOP
            }
        }
    }

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in normal condition.
     *
     * The stream 'teleinfo-3.raw' starts with an complete frame ; this frame must be read.
     */
    @Test
    public void test2ReadNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        File teleinfoStream3 = TestUtils.getTestFile("teleinfo-stream/teleinfo-3.raw");
        try (TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new FileInputStream(teleinfoStream3),
                waitNextHeaderFrameTimeoutInMs, readingFrameTimeoutInMs);) {

            Frame frame1 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617990, 5, 44, intensiteSouscrite, 1130, "000000", frame1);

            // skip frames until the latest (valid) frame
            for (int i = 1; i < 197; i++) {
                teleinfoIn.readNextFrame();
            }

            Frame frame198 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7618020, 1, 44, intensiteSouscrite, 180, "000000", frame198);

            // read 199th frame (incomplete frame)
            try {
                teleinfoIn.readNextFrame();
                Assert.fail("Frame 199 is incomplete");
            } catch (InvalidFrameException e) {
                // NOP
            }
        }
    }

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in normal condition.
     *
     * The stream 'teleinfo-1.raw' starts with an partial frame ; this frame must be ignored.
     */
    @Test
    public void test3ReadNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        File teleinfoStream1 = TestUtils.getTestFile("teleinfo-stream/teleinfo-1.raw");
        try (TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new FileInputStream(teleinfoStream1),
                waitNextHeaderFrameTimeoutInMs, readingFrameTimeoutInMs);) {
            Frame frame1 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617931, 3, 44, intensiteSouscrite, 680, "000000", frame1);

            // check frame 2
            Frame frame2 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617931, 1, 44, intensiteSouscrite, 290, "000000", frame2);

            // check frame 3
            Frame frame3 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617931, 1, 44, intensiteSouscrite, 280, "000000", frame3);

            // check frame 4
            Frame frame4 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617931, 1, 44, intensiteSouscrite, 280, "000000", frame4);

            // check frame 9
            for (int i = 5; i < 9; i++) {
                teleinfoIn.readNextFrame();
            }
            Frame frame9 = teleinfoIn.readNextFrame();
            TestUtils.assertFrameOptionHeuresCreusesEquals(adco, null, PeriodeTarifaire.HP, GroupeHoraire.A, 6906827,
                    7617932, 10, 44, intensiteSouscrite, 2490, "000000", frame9);
        }
    }

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in error condition.
     *
     * The latest frame in 'teleinfo-1.raw' stream is incomplete. An InvalidFrameException exception is expected.
     */
    @Test(expected = InvalidFrameException.class)
    public void test4ReadNextFrame() throws InvalidFrameException, TimeoutException, IOException {
        File teleinfoStream1 = TestUtils.getTestFile("teleinfo-stream/teleinfo-1.raw");
        try (TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new FileInputStream(teleinfoStream1),
                waitNextHeaderFrameTimeoutInMs, readingFrameTimeoutInMs);) {
            // skip 13 first frames
            for (int i = 0; i < 13; i++) {
                teleinfoIn.readNextFrame();
            }

            // read 14th frame
            teleinfoIn.readNextFrame();
            Assert.fail("Frame 14 is incomplete (the last group line 'IMAX 0' is corrupted)");
        }
    }
}
