package org.shsts.tinactory.content.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.multiblock.MultiBlockCheckCtx;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockSpec implements Consumer<MultiBlockCheckCtx> {
    public static final char IGNORED_CHAR = ' ';
    public static final char CENTER_CHAR = '$';

    public static class Layer {
        private final List<String> rows;
        private final int height;

        public Layer(LayerBuilder<?> builder) {
            this.rows = builder.rows;
            this.height = builder.height;
        }

        public char get(int w, int d) {
            return rows.get(d).charAt(w);
        }
    }

    private final List<Layer> layers;
    private final Map<Character, BiConsumer<MultiBlockCheckCtx, BlockPos>> checkers;
    private final Layer centerLayer;
    private final int centerLayerIdx;
    private final int centerW;
    private final int centerD;
    private final int width;
    private final int depth;

    private MultiBlockSpec(Builder<?> builder) {
        this.layers = builder.layers;
        this.checkers = builder.checkers;
        this.centerLayerIdx = builder.centerLayerIdx;
        this.centerLayer = layers.get(centerLayerIdx);
        this.centerW = builder.centerW;
        this.centerD = builder.centerD;
        this.width = builder.width;
        this.depth = builder.depth;
    }

    private boolean getDirections(MultiBlockCheckCtx ctx) {
        var blockState = ctx.getBlock(ctx.getCenter());
        if (blockState.isEmpty()) {
            ctx.setFailed();
            return false;
        }

        if (!blockState.get().hasProperty(PrimitiveBlock.FACING)) {
            ctx.setProperty("dirW", Direction.EAST);
            ctx.setProperty("dirD", Direction.SOUTH);
        } else {
            var dir = blockState.get().getValue(PrimitiveBlock.FACING);
            var dir1 = switch (dir) {
                case SOUTH -> Direction.EAST;
                case EAST -> Direction.NORTH;
                case NORTH -> Direction.WEST;
                case WEST -> Direction.SOUTH;
                default -> throw new IllegalStateException();
            };
            ctx.setProperty("dirW", dir1);
            ctx.setProperty("dirD", dir);
        }
        return true;
    }

    private boolean checkLayer(MultiBlockCheckCtx ctx, Layer layer, BlockPos base,
                               int y, Direction dirW, Direction dirD) {
        for (var d = 0; d < depth; d++) {
            for (var w = 0; w < width; w++) {
                if (w == centerW && d == centerD && layer == centerLayer) {
                    continue;
                }
                var ch = layer.get(w, d);
                if (ch == IGNORED_CHAR) {
                    continue;
                }
                var pos = base.above(y).relative(dirW, w).relative(dirD, d);
                var checker = checkers.get(ch);
                checker.accept(ctx, pos);
                if (ctx.isFailed()) {
                    return false;
                }
                ctx.addBlock(pos);
            }
        }
        return true;
    }

    @Override
    public void accept(MultiBlockCheckCtx ctx) {
        if (!getDirections(ctx)) {
            return;
        }
        var dirW = (Direction) ctx.getProperty("dirW");
        var dirD = (Direction) ctx.getProperty("dirD");
        var base = ctx.getCenter().relative(dirW, -centerW).relative(dirD, -centerD);
        var y = 0;
        for (var i = centerLayerIdx; i < layers.size(); i++) {
            var layer = layers.get(i);
            for (var h = 0; h < layer.height; h++) {
                if (!checkLayer(ctx, layer, base, y + h, dirW, dirD)) {
                    return;
                }
            }
            y += layer.height;
        }
        y = 0;
        for (var i = centerLayerIdx - 1; i >= 0; i--) {
            var layer = layers.get(i);
            y -= layer.height;
            for (var h = 0; h < layer.height; h++) {
                if (!checkLayer(ctx, layer, base, y + h, dirW, dirD)) {
                    return;
                }
            }
        }
    }

    public static class Builder<P> extends SimpleBuilder<MultiBlockSpec, P, Builder<P>> {
        private final List<Layer> layers = new ArrayList<>();
        private final Map<Character, BiConsumer<MultiBlockCheckCtx, BlockPos>> checkers = new HashMap<>();
        private int centerLayerIdx = -1;
        private int centerW;
        private int centerD;
        private int width = 0;
        private int depth = 0;

        private Builder(P parent) {
            super(parent);
        }

        public LayerBuilder<P> layer() {
            var ret = new LayerBuilder<>(this);
            ret.onCreateObject(l -> {
                layers.add(l);
                var w = l.rows.get(0).length();
                var d = l.rows.size();
                if (width == 0) {
                    width = w;
                    depth = d;
                } else if (width != w || depth != d) {
                    throw new IllegalArgumentException("layer size not same");
                }
            });
            return ret;
        }

        public Builder<P> check(char ch, BiConsumer<MultiBlockCheckCtx, BlockPos> checker) {
            checkers.put(ch, checker);
            return this;
        }

        private static boolean checkInterface(MultiBlockCheckCtx ctx, BlockPos pos) {
            var be = ctx.getBlockEntity(pos);
            if (be.isEmpty()) {
                return false;
            }
            var machine = AllCapabilities.MACHINE.tryGet(be.get());
            if (machine.isEmpty() || !(machine.get() instanceof MultiBlockInterface inter)) {
                return false;
            }
            if (ctx.hasProperty("interface")) {
                ctx.setFailed();
            } else {
                ctx.setProperty("interface", inter);
            }
            return true;
        }

        public Builder<P> block(char ch, Supplier<Block> block, boolean allowInterface) {
            return check(ch, (ctx, pos) -> {
                var block1 = ctx.getBlock(pos);
                if (allowInterface && checkInterface(ctx, pos)) {
                    return;
                }
                if (block1.isEmpty() || !block1.get().is(block.get())) {
                    ctx.setFailed();
                }
            });
        }

        public Builder<P> block(char ch, Supplier<Block> block) {
            return block(ch, block, false);
        }

        public Builder<P> sameBlockWithTag(char ch, String key, TagKey<Block> tag) {
            return check(ch, (ctx, pos) -> {
                var block1 = ctx.getBlock(pos);
                if (block1.isEmpty() || !block1.get().is(tag)) {
                    ctx.setFailed();
                } else if (ctx.hasProperty(key)) {
                    if (!block1.get().is((Block) ctx.getProperty(key))) {
                        ctx.setFailed();
                    }
                } else {
                    ctx.setProperty(key, block1.get().getBlock());
                }
            });
        }

        public Builder<P> air(char ch) {
            return check(ch, (ctx, pos) -> {
                var block1 = ctx.getBlock(pos);
                if (block1.isEmpty() || !block1.get().isAir()) {
                    ctx.setFailed();
                }
            });
        }

        public Builder<P> blockOrInterface(char ch, Supplier<Block> block) {
            return block(ch, block, true);
        }

        private void validate() {
            for (var y = 0; y < layers.size(); y++) {
                for (var d = 0; d < depth; d++) {
                    for (var w = 0; w < width; w++) {
                        var ch = layers.get(y).get(w, d);
                        if (ch == CENTER_CHAR) {
                            if (centerLayerIdx != -1) {
                                throw new IllegalArgumentException("contains more than 1 center");
                            }
                            centerLayerIdx = y;
                            centerW = w;
                            centerD = d;
                        } else if (ch != IGNORED_CHAR && !checkers.containsKey(ch)) {
                            throw new IllegalArgumentException("invalid character in spec: '%c'".formatted(ch));
                        }
                    }
                }
            }
            if (centerLayerIdx == -1) {
                throw new IllegalArgumentException("contains no center");
            }
        }

        @Override
        protected MultiBlockSpec createObject() {
            validate();
            return new MultiBlockSpec(this);
        }
    }

    public static class LayerBuilder<P> extends SimpleBuilder<Layer, Builder<P>, LayerBuilder<P>> {
        private final List<String> rows = new ArrayList<>();
        private int height = 1;

        private LayerBuilder(Builder<P> parent) {
            super(parent);
        }

        public LayerBuilder<P> height(int val) {
            this.height = val;
            return this;
        }

        public LayerBuilder<P> row(String str) {
            rows.add(str);
            return this;
        }

        public LayerBuilder<P> empty() {
            return row("");
        }

        private int checkWidth() {
            var width = 0;
            for (var row : rows) {
                if (row.isEmpty()) {
                    continue;
                }
                if (width == 0) {
                    width = row.length();
                } else if (width != row.length()) {
                    throw new IllegalArgumentException("layer rows are not same size");
                }
            }
            if (width == 0) {
                throw new IllegalArgumentException("has no row with width");
            }
            return width;
        }

        @Override
        protected Layer createObject() {
            var width = checkWidth();
            var emptyRow = " ".repeat(width);
            for (var i = 0; i < rows.size(); i++) {
                if (rows.get(i).isEmpty()) {
                    rows.set(i, emptyRow);
                }
            }
            return new Layer(this);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(parent);
    }

    public static Builder<?> builder() {
        return new Builder<>(Unit.INSTANCE);
    }
}
