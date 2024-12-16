package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISetMachineConfigPacket extends IPacket {
    CompoundTag getSets();

    List<String> getResets();

    interface Builder extends Supplier<ISetMachineConfigPacket> {
        Builder reset(String key);

        Builder set(String key, boolean val);

        Builder set(String key, String value);

        default Builder set(String key, ResourceLocation val) {
            return set(key, val.toString());
        }

        Builder set(String key, CompoundTag tag);
    }
}
