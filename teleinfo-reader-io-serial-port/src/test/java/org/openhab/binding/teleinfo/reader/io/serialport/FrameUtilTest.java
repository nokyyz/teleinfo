package org.openhab.binding.teleinfo.reader.io.serialport;

import org.junit.Test;
import org.openhab.binding.teleinfo.reader.io.serialport.FrameUtil;

import junit.framework.Assert;

public class FrameUtilTest {

    @Test
    public void test1ComputeGroupLineChecksum() {
        Assert.assertEquals(',', FrameUtil.computeGroupLineChecksum("PAPP", "03170"));
        Assert.assertEquals('>', FrameUtil.computeGroupLineChecksum("HCHP", "096066754"));
        Assert.assertEquals('Y', FrameUtil.computeGroupLineChecksum("IINST", "002"));
        Assert.assertEquals('G', FrameUtil.computeGroupLineChecksum("IMAX", "044"));
        Assert.assertEquals('?', FrameUtil.computeGroupLineChecksum("ISOUSC", "45"));
        Assert.assertEquals('<', FrameUtil.computeGroupLineChecksum("OPTARIF", "HC.."));
        Assert.assertEquals('5', FrameUtil.computeGroupLineChecksum("ADCO", "040422040644"));
        Assert.assertEquals(' ', FrameUtil.computeGroupLineChecksum("PTEC", "HP.."));
        Assert.assertEquals('+', FrameUtil.computeGroupLineChecksum("PAPP", "00460"));
        Assert.assertEquals('0', FrameUtil.computeGroupLineChecksum("HHPHC", "E"));
        Assert.assertEquals('B', FrameUtil.computeGroupLineChecksum("MOTDETAT", "000000"));

        Assert.assertEquals('B', FrameUtil.computeGroupLineChecksum("ADCO", "031328187235"));
    }
}
