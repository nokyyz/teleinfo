package org.openhab.binding.teleinfo.reader.dsl;

public class FrameOptionBase extends Frame {

    private static final long serialVersionUID = 5560141193379363335L;

    private Integer indexBase; // BASE : Index si option = base (en Wh)

    public FrameOptionBase() {
        // default constructor
    }

    public Integer getIndexBase() {
        return indexBase;
    }

    public void setIndexBase(Integer indexBase) {
        this.indexBase = indexBase;
    }

}
