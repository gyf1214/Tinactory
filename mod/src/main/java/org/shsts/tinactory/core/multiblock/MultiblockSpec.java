package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.core.builder.SimpleBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiblockSpec<S> implements Consumer<IMultiblockCheckCtx<S>>, IMultiblockDisplay {
    public static final char IGNORED_CHAR = ' ';
    public static final char CENTER_CHAR = '$';

    private final List<MultiblockLayer> layers;
    private final Map<Character, BiConsumer<IMultiblockCheckCtx<S>, BlockPos>> checkers;
    private final Map<Character, IBlockIngredient> ingredients;
    private final MultiblockLayer centerLayer;
    private final int centerLayerIdx;
    private final int centerW;
    private final int centerD;
    private final int width;
    private final int depth;
    private final int height;
    private final BlockPos controllerPosition;
    private final List<RequiredIngredient> requiredIngredients;

    private MultiblockSpec(Builder<S, ?> builder) {
        this.layers = List.copyOf(builder.layers);
        this.checkers = Map.copyOf(builder.checkers);
        this.ingredients = Map.copyOf(builder.ingredients);
        this.centerLayerIdx = builder.centerLayerIdx;
        this.centerLayer = layers.get(centerLayerIdx);
        this.centerW = builder.centerW;
        this.centerD = builder.centerD;
        this.width = builder.width;
        this.depth = builder.depth;
        this.height = layers.stream().mapToInt(layer -> layer.minHeight).sum();
        this.controllerPosition = new BlockPos(centerW, layers.subList(0, centerLayerIdx).stream()
            .mapToInt(layer -> layer.minHeight)
            .sum(), centerD);
        this.requiredIngredients = createRequiredIngredients();
    }

    private List<RequiredIngredient> createRequiredIngredients() {
        var ingredientIndexes = new IdentityHashMap<IBlockIngredient, Integer>();
        var result = new ArrayList<RequiredIngredient>();
        for (var y = 0; y < height; y++) {
            for (var z = 0; z < depth; z++) {
                for (var x = 0; x < width; x++) {
                    var ingredient = getIngredient(x, y, z);
                    if (ingredient.isEmpty()) {
                        continue;
                    }
                    var value = ingredient.get();
                    var index = ingredientIndexes.get(value);
                    if (index == null) {
                        ingredientIndexes.put(value, result.size());
                        result.add(new RequiredIngredient(value, 1));
                    } else {
                        var required = result.get(index);
                        result.set(index, new RequiredIngredient(value, required.count() + 1));
                    }
                }
            }
        }
        return List.copyOf(result);
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

    private Optional<List<BlockPos>> checkLayer(IMultiblockCheckCtx<S> ctx, MultiblockLayer layer, BlockPos base,
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

    private boolean checkLayer(IMultiblockCheckCtx<S> ctx, MultiblockLayer layer, boolean reverse) {
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
                    ctx.addToStructure(pos);
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

    @Override
    public int width() {
        return width;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public BlockPos controllerPosition() {
        return controllerPosition;
    }

    @Override
    public Optional<IBlockIngredient> getIngredient(int x, int y, int z) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return Optional.empty();
        }
        var layerY = 0;
        for (var layer : layers) {
            if (y < layerY + layer.minHeight) {
                if (x == centerW && z == centerD && layer == centerLayer) {
                    return Optional.empty();
                }
                return Optional.ofNullable(ingredients.get(layer.get(x, z)));
            }
            layerY += layer.minHeight;
        }
        return Optional.empty();
    }

    @Override
    public List<RequiredIngredient> getRequiredIngredients() {
        return requiredIngredients;
    }

    public static class Builder<S, P> extends SimpleBuilder<MultiblockSpec<S>, P, Builder<S, P>> {
        private final List<MultiblockLayer> layers = new ArrayList<>();
        private final Map<Character, BiConsumer<IMultiblockCheckCtx<S>, BlockPos>> checkers = new HashMap<>();
        private final Map<Character, IBlockIngredient> ingredients = new HashMap<>();
        private int centerLayerIdx = -1;
        private int centerW;
        private int centerD;
        private int width = 0;
        private int depth = 0;

        private Builder(P parent) {
            super(parent);
        }

        public MultiblockLayer.Builder<Builder<S, P>> layer() {
            return new MultiblockLayer.Builder<>(this)
                .onCreateObject(l -> {
                    layers.add(l);
                    var w = l.width();
                    var d = l.depth();
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

        public Builder<S, P> check(char ch, BiConsumer<IMultiblockCheckCtx<S>, BlockPos> checker,
            IBlockIngredient ingredient) {
            checkers.put(ch, checker);
            ingredients.put(ch, ingredient);
            return this;
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
            return new MultiblockSpec<>(this);
        }
    }

    public static <S, P> Builder<S, P> builder(P parent) {
        return new Builder<>(parent);
    }
}
