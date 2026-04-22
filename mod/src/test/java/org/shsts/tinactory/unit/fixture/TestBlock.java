package org.shsts.tinactory.unit.fixture;

public record TestBlock(String id) {
    public static final TestBlock AIR = new TestBlock("air");
    public static final TestBlock BASE = new TestBlock("base");
    public static final TestBlock CASING = new TestBlock("casing");
    public static final TestBlock COIL = new TestBlock("coil");
    public static final TestBlock GLASS = new TestBlock("glass");
}
