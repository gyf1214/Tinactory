package org.shsts.tinactory.content.electric;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public enum BatteryBoxMode implements StringRepresentable {
    BUFFER("buffer"), CHARGE("charge"), DISCHARGE("discharge");

    private static final Codec<BatteryBoxMode> STRING_CODEC = StringRepresentable.fromEnum(BatteryBoxMode::values);
    public static final Codec<BatteryBoxMode> CODEC = Codec.either(Codec.BOOL, STRING_CODEC).xmap(
        value -> value.map(discharge -> discharge ? DISCHARGE : BUFFER, mode -> mode),
        Either::right);

    private final String name;

    BatteryBoxMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public BatteryBoxMode next() {
        return switch (this) {
            case BUFFER -> CHARGE;
            case CHARGE -> DISCHARGE;
            case DISCHARGE -> BUFFER;
        };
    }
}
