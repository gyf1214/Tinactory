package org.shsts.tinactory.content.logistics;

public enum LogisticsDirection {
    PUSH, PULL;

    public LogisticsDirection invert() {
        return this == PUSH ? PULL : PUSH;
    }
}
