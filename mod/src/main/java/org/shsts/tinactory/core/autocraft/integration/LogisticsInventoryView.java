package org.shsts.tinactory.core.autocraft.integration;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsInventoryView implements IInventoryView {
    private final IItemPort itemPort;
    private final IFluidPort fluidPort;

    public LogisticsInventoryView(IItemPort itemPort, IFluidPort fluidPort) {
        this.itemPort = itemPort;
        this.fluidPort = fluidPort;
    }

    @Override
    public long amountOf(CraftKey key) {
        return switch (key.type()) {
            case ITEM -> itemPort.getItemCount(toItemStack(key, 1));
            case FLUID -> fluidPort.getFluidAmount(toFluidStack(key, 1));
        };
    }

    @Override
    public boolean consume(CraftKey key, long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (amountOf(key) < amount) {
            return false;
        }
        var left = amount;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            switch (key.type()) {
                case ITEM -> {
                    var expected = toItemStack(key, chunk);
                    var extracted = itemPort.extractItem(expected, false);
                    if (extracted.getCount() < chunk) {
                        return false;
                    }
                }
                case FLUID -> {
                    var expected = toFluidStack(key, chunk);
                    var extracted = fluidPort.drain(expected, false);
                    if (extracted.getAmount() < chunk) {
                        return false;
                    }
                }
            }
            left -= chunk;
        }
        return true;
    }

    @Override
    public void produce(CraftKey key, long amount) {
        if (amount <= 0L) {
            return;
        }
        var left = amount;
        while (left > 0L) {
            var chunk = (int) Math.min(left, Integer.MAX_VALUE);
            switch (key.type()) {
                case ITEM -> {
                    var remaining = itemPort.insertItem(toItemStack(key, chunk), false);
                    if (!remaining.isEmpty()) {
                        throw new IllegalStateException("cannot insert item output: " + key);
                    }
                }
                case FLUID -> {
                    var inserted = fluidPort.fill(toFluidStack(key, chunk), false);
                    if (inserted < chunk) {
                        throw new IllegalStateException("cannot insert fluid output: " + key);
                    }
                }
            }
            left -= chunk;
        }
    }

    public List<CraftAmount> snapshotAvailable() {
        var ret = new ArrayList<CraftAmount>();
        for (var stack : itemPort.getAllItems()) {
            if (!stack.isEmpty()) {
                ret.add(new CraftAmount(fromItemStack(stack), stack.getCount()));
            }
        }
        for (var stack : fluidPort.getAllFluids()) {
            if (!stack.isEmpty()) {
                ret.add(new CraftAmount(fromFluidStack(stack), stack.getAmount()));
            }
        }
        return ret;
    }

    static CraftKey fromItemStack(ItemStack stack) {
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            throw new IllegalArgumentException("unknown item stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.item(itemId.toString(), nbt);
    }

    static CraftKey fromFluidStack(FluidStack stack) {
        var fluidId = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (fluidId == null) {
            throw new IllegalArgumentException("unknown fluid stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.fluid(fluidId.toString(), nbt);
    }

    private static ItemStack toItemStack(CraftKey key, int amount) {
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

    private static FluidStack toFluidStack(CraftKey key, int amount) {
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
