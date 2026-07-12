package org.shsts.tinactory.api.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum PortDirection implements StringRepresentable {
    NONE, OUTPUT, INPUT;

    public PortDirection invert() {
        return switch (this) {
            case NONE -> NONE;
            case INPUT -> OUTPUT;
            case OUTPUT -> INPUT;
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static PortDirection fromName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }
}
