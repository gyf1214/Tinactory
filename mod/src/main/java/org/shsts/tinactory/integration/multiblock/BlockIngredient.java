package org.shsts.tinactory.integration.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record BlockIngredient(List<Value> values) {
    public BlockIngredient {
        values = List.copyOf(values);
    }

    public static BlockIngredient of(Block block) {
        return new BlockIngredient(List.of(new BlockValue(block)));
    }

    public static BlockIngredient of(TagKey<Block> tag) {
        return new BlockIngredient(List.of(new TagValue(tag)));
    }

    public static BlockIngredient of(Value... values) {
        return new BlockIngredient(Arrays.asList(values));
    }

    public static BlockIngredient of(Collection<Value> values) {
        return new BlockIngredient(values.stream().toList());
    }

    public List<Block> expand() {
        var ret = new LinkedHashSet<Block>();
        for (var value : values) {
            value.expand(ret);
        }
        return List.copyOf(ret);
    }

    public interface Value {
        void expand(Collection<Block> ret);
    }

    public record BlockValue(Block block) implements Value {
        @Override
        public void expand(Collection<Block> ret) {
            ret.add(block);
        }
    }

    public record TagValue(TagKey<Block> tag) implements Value {
        @Override
        public void expand(Collection<Block> ret) {
            for (var block : ForgeRegistries.BLOCKS.tags().getTag(tag)) {
                ret.add(block);
            }
        }
    }
}
