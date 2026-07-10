package org.shsts.tinactory.integration.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class BlockIngredient implements Predicate<BlockState> {
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

    @Override
    public boolean test(BlockState blockState) {
        return values.stream().anyMatch($ -> $.test(blockState));
    }

    public List<Block> expand(HolderLookup.Provider provider) {
        if (expanded != null) {
            return expanded;
        }
        var ret = new LinkedHashSet<Block>();
        for (var value : values) {
            value.expand(provider, ret::add);
        }
        expanded = List.copyOf(ret);
        return expanded;
    }

    public interface Value extends Predicate<BlockState> {
        void expand(HolderLookup.Provider provider, Consumer<Block> consumer);
    }

    public record BlockValue(Supplier<? extends Block> block) implements Value {
        @Override
        public boolean test(BlockState blockState) {
            return blockState.is(block.get());
        }

        @Override
        public void expand(HolderLookup.Provider provider, Consumer<Block> consumer) {
            consumer.accept(block.get());
        }
    }

    public record TagValue(TagKey<Block> tag) implements Value {
        @Override
        public boolean test(BlockState blockState) {
            return blockState.is(tag);
        }

        @Override
        public void expand(HolderLookup.Provider provider, Consumer<Block> consumer) {
            provider.lookup(Registries.BLOCK)
                .flatMap($ -> $.get(tag))
                .stream().flatMap(HolderSet.ListBacked::stream)
                .map(Holder::value)
                .forEach(consumer);
        }
    }
}
