package org.shsts.tinactory.core.logistics;

public enum SlotType {
    NONE(false, false),
    ITEM_INPUT(false, true),
    ITEM_OUTPUT(true, true),
    FLUID_INPUT(false, false),
    FLUID_OUTPUT(true, false);

    public final boolean output;
    public final boolean isItem;

    SlotType(boolean output, boolean isItem) {
        this.output = output;
        this.isItem = isItem;
    }
}
