package org.openhab.binding.teleinfo.reader.io.serialport;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Test;

public class TeleinfoInputStreamTest2 {

    final static int MAXIMUM_TIMEOUT_JUNIT = 2000; // to prevent failure behavior in Teleinfo timeouts

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in timeout condition.
     * This test verifies the timeout between two frames.
     */
    @Test(expected = TimeoutException.class, timeout = MAXIMUM_TIMEOUT_JUNIT)
    public void testTimeout1ReadNextFrame()
            throws InvalidFrameException, TimeoutException, IOException, InterruptedException, ExecutionException {
        try (TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new InvalidHeaderFrameTeleinfoInputStream());) {

            teleinfoIn.readNextFrame();
            Assert.fail();
        }
    }

    /**
     * Check {@link TeleinfoInputStream#readNextFrame()} function in timeout condition.
     * This test verifies the timeout during the read of frame.
     */
    @Test(expected = TimeoutException.class, timeout = MAXIMUM_TIMEOUT_JUNIT)
    public void testTimeout2ReadNextFrame()
            throws InvalidFrameException, TimeoutException, IOException, InterruptedException, ExecutionException {
        try {
            TeleinfoInputStream teleinfoIn = new TeleinfoInputStream(new OutOfTimeTeleinfoInputStream());

            teleinfoIn.readNextFrame();
            Assert.fail("Timeout expected");
        } finally {
            // don't close the stream because this operation is blocked by infinite loop in
            // OutOfTimeTeleinfoInputStream#read()
        }
    }

    private class InvalidHeaderFrameTeleinfoInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return 1;
        }

    }

    private class OutOfTimeTeleinfoInputStream extends InputStream {
        private char[] headerFrame = { (byte) 3, (byte) 2, '\r', 'A', 'D', 'C', 'O', '0', '3', '1', '3', '2', '8', '1',
                '8', '7', '2', '3', '5', ' ', 'B', '\r', '\n' };
        private int currentPosition = 0;

        @Override
        public int read() throws IOException {
            if (currentPosition >= headerFrame.length) {
                while (true) {
                    ;
                }
            } else {
                return headerFrame[currentPosition++];
            }
        }
    }
}
