package org.shsts.tinactory.integration.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinycorelib.api.core.Transformer;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockIngredient implements IBlockIngredient, IBlockIngredient.Value {
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

    @Override
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

    @Override
    public void expand(HolderLookup.Provider provider, Consumer<Block> consumer) {
        for (var block : expand(provider)) {
            consumer.accept(block);
        }
    }

    @Override
    public BlockState display(HolderLookup.Provider provider) {
        var expanded = expand(provider);
        return (expanded.isEmpty() ? Blocks.AIR : expanded.getFirst()).defaultBlockState();
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

    public record Display(IBlockIngredient ingredient, IBlockIngredient displayBlock,
        Transformer<BlockState> displayState)
        implements IBlockIngredient {
        @Override
        public List<Block> expand(HolderLookup.Provider provider) {
            return ingredient.expand(provider);
        }

        @Override
        public BlockState display(HolderLookup.Provider provider) {
            return displayState.apply(displayBlock.display(provider));
        }

        @Override
        public boolean test(BlockState blockState) {
            return ingredient.test(blockState);
        }
    }

    public static Value blockValue(Supplier<Block> block) {
        return new BlockValue(block);
    }

    public static Value tagValue(TagKey<Block> tag) {
        return new TagValue(tag);
    }

    public static IBlockIngredient withDisplay(IBlockIngredient ingredient,
        IBlockIngredient displayBlock, Transformer<BlockState> displayState) {
        return new Display(ingredient, displayBlock, displayState);
    }
}
