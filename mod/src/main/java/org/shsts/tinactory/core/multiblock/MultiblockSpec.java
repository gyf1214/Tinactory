package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockSpec<S> implements Consumer<IMultiblockCheckCtx<S>> {
    public static final char IGNORED_CHAR = ' ';
    public static final char CENTER_CHAR = '$';

    public static class Layer {
        private final List<String> rows;
        private final int minHeight;
        private final int maxHeight;

        public Layer(LayerBuilder<?, ?> builder) {
            this.rows = builder.rows;
            this.minHeight = builder.minHeight;
            this.maxHeight = builder.maxHeight;
        }

        public char get(int w, int d) {
            return rows.get(d).charAt(w);
        }
    }

    private final List<Layer> layers;
    private final Map<Character, BiConsumer<IMultiblockCheckCtx<S>, BlockPos>> checkers;
    private final Layer centerLayer;
    private final int centerLayerIdx;
    private final int centerW;
    private final int centerD;
    private final int width;
    private final int depth;

    private MultiblockSpec(Builder<S, ?> builder) {
        this.layers = builder.layers;
        this.checkers = builder.checkers;
        this.centerLayerIdx = builder.centerLayerIdx;
        this.centerLayer = layers.get(centerLayerIdx);
        this.centerW = builder.centerW;
        this.centerD = builder.centerD;
        this.width = builder.width;
        this.depth = builder.depth;
    }

    private boolean getDirections(IMultiblockCheckCtx<S> ctx) {
        Direction dirW;
        Direction dirD;
        var facing = ctx.getFacing();
        if (facing.isEmpty()) {
            dirW = Direction.EAST;
            dirD = Direction.SOUTH;
        } else {
            dirD = facing.get();
            dirW = switch (dirD) {
                case SOUTH -> Direction.EAST;
                case EAST -> Direction.NORTH;
                case NORTH -> Direction.WEST;
                case WEST -> Direction.SOUTH;
                default -> throw new IllegalStateException();
            };
        }
        ctx.setProperty("dirW", dirW);
        ctx.setProperty("dirD", dirD);
        ctx.setProperty("base", ctx.getCenter()
            .relative(dirW, -centerW)
            .relative(dirD, -centerD));
        return true;
    }

    private Optional<List<BlockPos>> checkLayer(IMultiblockCheckCtx<S> ctx, Layer layer, BlockPos base,
        int y, Direction dirW, Direction dirD) {
        var blocks = new ArrayList<BlockPos>();
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
                if (checker == null) {
                    ctx.setFailed();
                    return Optional.empty();
                }
                checker.accept(ctx, pos);
                if (ctx.isFailed()) {
                    return Optional.empty();
                }
                blocks.add(pos);
            }
        }
        return Optional.of(blocks);
    }

    private boolean checkLayer(IMultiblockCheckCtx<S> ctx, Layer layer, boolean reverse) {
        var dirW = (Direction) ctx.getProperty("dirW");
        var dirD = (Direction) ctx.getProperty("dirD");
        var base = (BlockPos) ctx.getProperty("base");
        var y = (int) ctx.getProperty("y");
        var h = 0;
        for (; h < layer.maxHeight; h++) {
            var y1 = reverse ? y - h - 1 : y + h;
            var result = checkLayer(ctx, layer, base, y1, dirW, dirD);
            if (result.isPresent()) {
                for (var pos : result.get()) {
                    ctx.addBlock(pos);
                }
            } else {
                if (h < layer.minHeight) {
                    ctx.setFailed();
                    return false;
                } else {
                    ctx.setFailed(false);
                    break;
                }
            }
        }
        ctx.setProperty("y", reverse ? y - h : y + h);
        return true;
    }

    @Override
    public void accept(IMultiblockCheckCtx<S> ctx) {
        if (!getDirections(ctx)) {
            return;
        }
        ctx.setProperty("y", 0);
        for (var i = centerLayerIdx; i < layers.size(); i++) {
            if (!checkLayer(ctx, layers.get(i), false)) {
                return;
            }
        }
        var h1 = (int) ctx.getProperty("y");
        ctx.setProperty("y", 0);
        for (var i = centerLayerIdx - 1; i >= 0; i--) {
            if (!checkLayer(ctx, layers.get(i), true)) {
                return;
            }
        }
        var h2 = (int) ctx.getProperty("y");
        ctx.setProperty("height", h1 - h2);
    }

    public static <S> boolean checkInterface(IMultiblockCheckCtx<S> ctx, BlockPos pos) {
        var machine = ctx.getMachine(pos).filter(IMachine::isMultiblock);
        if (machine.isEmpty()) {
            return false;
        }
        if (ctx.hasProperty("interface")) {
            ctx.setFailed();
        } else {
            ctx.setProperty("interface", machine.get());
        }
        return true;
    }

    public static class Builder<S, P> extends SimpleBuilder<MultiblockSpec<S>, P, Builder<S, P>> {
        private final List<Layer> layers = new ArrayList<>();
        private final Map<Character, BiConsumer<IMultiblockCheckCtx<S>, BlockPos>> checkers = new HashMap<>();
        private int centerLayerIdx = -1;
        private int centerW;
        private int centerD;
        private int width = 0;
        private int depth = 0;

        private Builder(P parent) {
            super(parent);
        }

        public LayerBuilder<S, P> layer() {
            return new LayerBuilder<>(this)
                .onCreateObject(l -> {
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
        }

        public Builder<S, P> check(char ch, BiConsumer<IMultiblockCheckCtx<S>, BlockPos> checker) {
            checkers.put(ch, checker);
            return this;
        }

        public Builder<S, P> checkBlock(char ch, Predicate<S> pred) {
            return check(ch, (ctx, pos) -> {
                var block = ctx.getBlock(pos);
                if (block.isEmpty() || !pred.test(block.get())) {
                    ctx.setFailed();
                }
            });
        }

        public Builder<S, P> interfaceSlot(char ch) {
            return check(ch, (ctx, pos) -> {
                if (!checkInterface(ctx, pos)) {
                    ctx.setFailed();
                }
            });
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
        protected MultiblockSpec<S> createObject() {
            validate();
            return new MultiblockSpec(this);
        }
    }

    public static class LayerBuilder<S, P> extends SimpleBuilder<Layer, Builder<S, P>, LayerBuilder<S, P>> {
        private final List<String> rows = new ArrayList<>();
        private int minHeight = 1;
        private int maxHeight = 1;

        private LayerBuilder(Builder<S, P> parent) {
            super(parent);
        }

        public LayerBuilder<S, P> height(int val) {
            minHeight = val;
            maxHeight = val;
            return this;
        }

        // TODO: deal with the problem that the "try" test will modify property
        public LayerBuilder<S, P> height(int min, int max) {
            minHeight = min;
            maxHeight = max;
            return this;
        }

        public LayerBuilder<S, P> row(String str) {
            rows.add(str);
            return this;
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

    public static <S, P> Builder<S, P> builder(P parent) {
        return new Builder<>(parent);
    }
}
