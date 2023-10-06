package org.shsts.tinactory.content.network;

import net.minecraft.MethodsReturnNonnullByDefault;

@MethodsReturnNonnullByDefault
public enum CableSetting {
    NORMAL(3, 1), DENSE(6, 3);

    public final int radius;
    public final int texId;

    CableSetting(int radius, int texId) {
        this.radius = radius;
        this.texId = texId;
    }

    public String asId() {
        return this.name().toLowerCase();
    }
}
