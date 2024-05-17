package org.shsts.tinactory.api.logistics;

public enum PortDirection {
    NONE, OUTPUT, INPUT;

    public PortDirection invert() {
        return switch (this) {
            case NONE -> NONE;
            case INPUT -> OUTPUT;
            case OUTPUT -> INPUT;
        };
    }
}
