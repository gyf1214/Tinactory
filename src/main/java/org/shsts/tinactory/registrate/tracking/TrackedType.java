package org.shsts.tinactory.registrate.tracking;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TrackedType<V>(String name) {
    public static final Set<TrackedType<?>> ALL_TYPES = new HashSet<>();

    public static final TrackedType<Block> BLOCK = create("block");
    public static final TrackedType<Item> ITEM = create("item");
    public static final TrackedType<ResourceLocation> TECH = create("tech");
    public static final TrackedType<String> LANG = create("lang");

    private static <V> TrackedType<V> create(String name) {
        var ret = new TrackedType<V>(name);
        ALL_TYPES.add(ret);
        return ret;
    }

    @Override
    public String toString() {
        return name;
    }
}
