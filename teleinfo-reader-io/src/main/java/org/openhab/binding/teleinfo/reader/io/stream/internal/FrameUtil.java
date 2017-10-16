package org.openhab.binding.teleinfo.reader.io.stream.internal;

public class FrameUtil {

    private FrameUtil() {
        // private constructor (utility class)
    }

    /**
     * Compute the checksum of the given group line.
     *
     * @param groupLine group line ("etiquette" <SPACE> "valeur"). Note: the SPACE before the checksum of the group line
     *            must not include in checksum computation.
     * @return the checksum of the given group line.
     */
    public static char computeGroupLineChecksum(final String label, final String value) {
        final String groupLine = label + " " + value;
        int sum = 0;
        for (int i = 0; i < groupLine.length(); i++) {
            sum = sum + groupLine.codePointAt(i);
        }
        sum = (sum & 0x3F) + 0x20;

        return (char) sum;
    }
}
