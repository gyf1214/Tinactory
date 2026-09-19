package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ISetMachineConfigPacket extends IPacket {
    List<? extends IMachineConfig.Entry<?>> getSets();

    List<IMachineConfigType<?>> getResets();

    interface Builder extends Supplier<ISetMachineConfigPacket> {
        Builder reset(IMachineConfigType<?> type);

        default Builder reset(IEntry<? extends IMachineConfigType<?>> type) {
            return reset(type.get());
        }

        Builder reset(HolderLookup.Provider provider, ResourceLocation loc);

        <T> Builder set(IEntry<IMachineConfigType<T>> type, T val);

        <T> Builder set(HolderLookup.Provider provider, ResourceLocation loc, T val);

        Builder reset(String key);

        Builder set(String key, Tag tag);
    }
}
