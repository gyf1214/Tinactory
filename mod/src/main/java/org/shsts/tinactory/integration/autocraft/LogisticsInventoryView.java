package org.shsts.tinactory.integration.autocraft;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsInventoryView implements IInventoryView {
    private final Map<CraftKey.Type, CraftPortChannel<?>> channels;

    public LogisticsInventoryView(IPort<ItemStack> itemPort, IPort<FluidStack> fluidPort) {
        channels = new EnumMap<>(CraftKey.Type.class);
        channels.put(CraftKey.Type.ITEM, new CraftPortChannel<>(
            ItemPortAdapter.INSTANCE,
            itemPort,
            LogisticsInventoryView::toItemStack,
            LogisticsInventoryView::fromItemStack));
        channels.put(CraftKey.Type.FLUID, new CraftPortChannel<>(
            FluidPortAdapter.INSTANCE,
            fluidPort,
            LogisticsInventoryView::toFluidStack,
            LogisticsInventoryView::fromFluidStack));
    }

    @Override
    public long amountOf(CraftKey key) {
        return channel(key.type()).amountOf(key);
    }

    @Override
    public long extract(CraftKey key, long amount, boolean simulate) {
        return channel(key.type()).extract(key, amount, simulate);
    }

    @Override
    public long insert(CraftKey key, long amount, boolean simulate) {
        return channel(key.type()).insert(key, amount, simulate);
    }

    public List<CraftAmount> snapshotAvailable() {
        var ret = new ArrayList<CraftAmount>();
        for (var channel : channels.values()) {
            ret.addAll(channel.snapshot());
        }
        return ret;
    }

    private CraftPortChannel<?> channel(CraftKey.Type type) {
        return Objects.requireNonNull(channels.get(type), "missing channel for craft key type " + type);
    }

    public static CraftKey fromItemStack(ItemStack stack) {
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            throw new IllegalArgumentException("unknown item stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.item(itemId.toString(), nbt);
    }

    public static CraftKey fromFluidStack(FluidStack stack) {
        var fluidId = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (fluidId == null) {
            throw new IllegalArgumentException("unknown fluid stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.fluid(fluidId.toString(), nbt);
    }

    public static ItemStack toItemStack(CraftKey key, int amount) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.id()));
        if (item == null) {
            throw new IllegalArgumentException("unknown item id: " + key.id());
        }
        var stack = new ItemStack(item, amount);
        var tag = parseTag(key.nbt());
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        return stack;
    }

    public static FluidStack toFluidStack(CraftKey key, int amount) {
        var fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(key.id()));
        if (fluid == null) {
            throw new IllegalArgumentException("unknown fluid id: " + key.id());
        }
        var stack = new FluidStack(fluid, amount);
        var tag = parseTag(key.nbt());
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        return stack;
    }

    private static CompoundTag parseTag(String nbt) {
        if (nbt.isBlank()) {
            return new CompoundTag();
        }
        try {
            return TagParser.parseTag(nbt);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("invalid nbt: " + nbt, ex);
        }
    }
}
