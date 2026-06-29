package org.shsts.tinactory.integration.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockIngredient {
    private final List<Value> values;
    private List<Block> expanded = null;

    private BlockIngredient(Collection<Value> values) {
        this.values = List.copyOf(values);
    }

    public static BlockIngredient of(Block block) {
        return new BlockIngredient(List.of(new BlockValue(() -> block)));
    }

    public static BlockIngredient of(Supplier<? extends Block> block) {
        return new BlockIngredient(List.of(new BlockValue(block)));
    }

    public static BlockIngredient of(TagKey<Block> tag) {
        return new BlockIngredient(List.of(new TagValue(tag)));
    }

    public static BlockIngredient of(Value... values) {
        return new BlockIngredient(Arrays.asList(values));
    }

    public static BlockIngredient of(Collection<Value> values) {
        return new BlockIngredient(values);
    }

    public List<Value> values() {
        return values;
    }

    public List<Block> expand() {
        if (expanded != null) {
            return expanded;
        }
        var ret = new LinkedHashSet<Block>();
        for (var value : values) {
            value.expand(ret::add);
        }
        expanded = List.copyOf(ret);
        return expanded;
    }

    public interface Value {
        void expand(Consumer<Block> consumer);
    }

    public record BlockValue(Supplier<? extends Block> block) implements Value {
        @Override
        public void expand(Consumer<Block> consumer) {
            consumer.accept(block.get());
        }
    }

    public record TagValue(TagKey<Block> tag) implements Value {
        @Override
        public void expand(Consumer<Block> consumer) {
            for (var block : ForgeRegistries.BLOCKS.tags().getTag(tag)) {
                consumer.accept(block);
            }
        }
    }
}
