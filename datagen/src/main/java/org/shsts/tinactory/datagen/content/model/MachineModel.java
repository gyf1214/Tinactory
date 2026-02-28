package org.shsts.tinactory.datagen.content.model;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.network.FixedMachineBlock;
import org.shsts.tinactory.content.network.StaticMachineBlock;
import org.shsts.tinactory.content.network.SubnetBlock;
import org.shsts.tinactory.core.builder.SimpleBuilder;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinactory.integration.network.SidedMachineBlock;
import org.shsts.tinycorelib.datagen.api.builder.IBlockDataBuilder;
import org.shsts.tinycorelib.datagen.api.context.IDataContext;
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.extend;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.content.Models.DIR_TEX_KEYS;
import static org.shsts.tinactory.datagen.content.Models.FRONT_FACING;
import static org.shsts.tinactory.datagen.content.Models.TEXTURE_TYPE;
import static org.shsts.tinactory.datagen.content.Models.VOID_TEX;
import static org.shsts.tinactory.datagen.content.Models.rotateModel;
import static org.shsts.tinactory.datagen.content.Models.xRotation;
import static org.shsts.tinactory.datagen.content.Models.yRotation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineModel {
    public static final String CASING_MODEL = "block/machine/casing";
    public static final String IO_MODEL = "block/machine/io";
    public static final ResourceLocation PRIMITIVE_TEX = gregtech("blocks/casings/wood_wall");
    public static final String IO_TEX = "overlay/machine/overlay_energy_in_multi";
    public static final String IO_OUT_TEX = "overlay/machine/overlay_energy_out_multi";
    public static final String ME_BUS = "overlay/appeng/me_output_bus";

    @Nullable
    private final ResourceLocation casing;
    @Nullable
    private final ResourceLocation overlay;
    private final ResourceLocation ioTex;
    private final Map<Direction, ResourceLocation> dirOverlay;

    private MachineModel(@Nullable ResourceLocation casing,
        @Nullable ResourceLocation overlay,
        ResourceLocation ioTex,
        Map<Direction, ResourceLocation> dirOverlay) {
        this.casing = casing;
        this.overlay = overlay;
        this.ioTex = ioTex;
        this.dirOverlay = dirOverlay;
    }

    public ResourceLocation getCasing(Block block) {
        if (casing != null) {
            return casing;
        }
        if (block instanceof PrimitiveBlock) {
            return PRIMITIVE_TEX;
        } else if (block instanceof MachineBlock machineBlock) {
            return casingTex(machineBlock.voltage);
        } else if (block instanceof SubnetBlock subnetBlock) {
            return casingTex(subnetBlock.voltage);
        }
        throw new IllegalArgumentException();
    }

    public static <B extends ModelBuilder<B>> B applyCasing(B model, ResourceLocation tex,
        ExistingFileHelper existingHelper) {
        if (existingHelper.exists(tex, TEXTURE_TYPE)) {
            return model.texture("top", tex)
                .texture("bottom", tex)
                .texture("side", tex);
        } else {
            return model.texture("top", extend(tex, "top"))
                .texture("bottom", extend(tex, "bottom"))
                .texture("side", extend(tex, "side"));
        }
    }

    private Optional<ResourceLocation> getOverlay(Direction dir, String suffix,
        ExistingFileHelper existingHelper) {
        if (dirOverlay.containsKey(dir)) {
            var tex = suffix(dirOverlay.get(dir), suffix);
            return existingHelper.exists(tex, TEXTURE_TYPE) ? Optional.of(tex) : Optional.empty();
        }
        if (overlay == null) {
            return Optional.empty();
        }
        var tex = suffix(overlay, suffix);
        if (existingHelper.exists(tex, TEXTURE_TYPE)) {
            return dir == Direction.NORTH ? Optional.of(tex) : Optional.empty();
        }
        var loc = extend(overlay, "overlay_" + DIR_TEX_KEYS.get(dir) + suffix);
        if (existingHelper.exists(loc, TEXTURE_TYPE)) {
            return Optional.of(loc);
        }
        var side = extend(overlay, "overlay_side" + suffix);
        if (existingHelper.exists(side, TEXTURE_TYPE) && dir.getAxis() == Direction.Axis.X) {
            return Optional.of(side);
        }
        return Optional.empty();
    }

    private <B extends ModelBuilder<B>> B applyOverlay(B model, boolean working,
        ExistingFileHelper existingHelper) {
        for (var e : DIR_TEX_KEYS.entrySet()) {
            var tex = working ? getOverlay(e.getKey(), "_active", existingHelper) :
                Optional.<ResourceLocation>empty();
            tex = tex.or(() -> getOverlay(e.getKey(), "", existingHelper));
            if (tex.isPresent()) {
                model = model.texture(e.getValue() + "_overlay", tex.get());
            }
        }
        return model;
    }

    private <B extends ModelBuilder<B>> B applyTextures(B model, Block block, boolean working,
        ExistingFileHelper existingHelper) {
        var casingTex = getCasing(block);
        model = applyCasing(model, casingTex, existingHelper);
        return applyOverlay(model, working, existingHelper);
    }

    private <B extends ModelBuilder<B>> void applyTextures(B model, ExistingFileHelper existingHelper) {
        assert casing != null;
        model = applyCasing(model, casing, existingHelper);
        applyOverlay(model, false, existingHelper);
    }

    public BlockModelBuilder blockModel(String id, Block block, boolean working, BlockModelProvider prov) {
        var model = prov.withExistingParent(id, modLoc(CASING_MODEL));
        return applyTextures(model, block, working, prov.existingFileHelper);
    }

    public BlockModelBuilder ioModel(String id, BlockModelProvider prov) {
        return prov.withExistingParent(id + "_io", modLoc(IO_MODEL))
            .texture("io_overlay", ioTex);
    }

    private void primitive(IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var prov = ctx.provider().models();
        var model = blockModel(ctx.id(), ctx.object(), false, prov);
        var workingModel = blockModel(ctx.id() + "_active", ctx.object(), true, prov);

        ctx.provider().getVariantBuilder(ctx.object())
            .forAllStates(state -> {
                var dir = state.getValue(MachineBlock.FACING);
                var working = state.getValue(MachineBlock.WORKING);
                return rotateModel(working ? workingModel : model, dir);
            });
    }

    private void sided(IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var model = blockModel(ctx.id(), ctx.object(), false, ctx.provider().models());
        ctx.provider().getVariantBuilder(ctx.object())
            .forAllStates(state -> {
                var dir = state.getValue(MachineBlock.IO_FACING);
                return rotateModel(model, dir);
            });
    }

    private void ioState(MultiPartBlockStateBuilder multipart, ModelFile io) {
        for (var dir : Direction.values()) {
            var builder = multipart.part().modelFile(io);
            if (dir.getAxis() == Direction.Axis.Y) {
                builder.rotationX(xRotation(dir))
                    .rotationY(yRotation(dir)).addModel()
                    .condition(MachineBlock.IO_FACING, dir);
            } else {
                var otherDir = Arrays.stream(Direction.values())
                    .filter(d -> d.getAxis() != Direction.Axis.Y && d != dir)
                    .toArray(Direction[]::new);
                builder.rotationY(yRotation(dir)).addModel()
                    .condition(MachineBlock.FACING, otherDir)
                    .condition(MachineBlock.IO_FACING, dir);
            }
        }
    }

    private void machine(IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var prov = ctx.provider().models();
        var base = blockModel(ctx.id(), ctx.object(), false, prov);
        var working = blockModel(ctx.id() + "_active", ctx.object(), true, prov);
        var io = ioModel(ctx.id(), prov);
        var multipart = ctx.provider().getMultipartBuilder(ctx.object());

        for (var dir : Direction.values()) {
            if (dir.getAxis() != Direction.Axis.Y) {
                multipart.part().modelFile(base)
                    .rotationY(yRotation(dir)).addModel()
                    .condition(MachineBlock.FACING, dir)
                    .condition(MachineBlock.WORKING, false)
                    .end()
                    .part().modelFile(working)
                    .rotationY(yRotation(dir)).addModel()
                    .condition(MachineBlock.FACING, dir)
                    .condition(MachineBlock.WORKING, true)
                    .end();
            }
        }

        ioState(multipart, io);
    }

    private void fixed(IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var prov = ctx.provider().models();
        var model = blockModel(ctx.id(), ctx.object(), false, prov);
        var workingModel = blockModel(ctx.id() + "_active", ctx.object(), true, prov);

        ctx.provider().getVariantBuilder(ctx.object())
            .forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(MachineBlock.WORKING) ? workingModel : model)
                .build());
    }

    private void staticMachine(IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var prov = ctx.provider().models();
        var base = blockModel(ctx.id(), ctx.object(), false, prov);
        var io = ioModel(ctx.id(), prov);
        var multipart = ctx.provider().getMultipartBuilder(ctx.object());

        for (var dir : Direction.values()) {
            if (dir.getAxis() != Direction.Axis.Y) {
                multipart.part().modelFile(base)
                    .rotationY(yRotation(dir)).addModel()
                    .condition(MachineBlock.FACING, dir)
                    .end();
            }
        }

        ioState(multipart, io);
    }

    public <U extends Block> Consumer<IEntryDataContext<Block, U, BlockStateProvider>> blockState() {
        return ctx -> {
            if (ctx.object() instanceof PrimitiveBlock) {
                primitive(ctx);
            } else if (ctx.object() instanceof SidedMachineBlock ||
                ctx.object() instanceof SubnetBlock) {
                sided(ctx);
            } else if (ctx.object() instanceof StaticMachineBlock) {
                staticMachine(ctx);
            } else if (ctx.object() instanceof MachineBlock) {
                machine(ctx);
            } else if (ctx.object() instanceof FixedMachineBlock) {
                fixed(ctx);
            } else {
                throw new IllegalArgumentException();
            }
        };
    }

    public void itemModel(IEntryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        var model = ctx.provider().withExistingParent(ctx.id(), modLoc(CASING_MODEL));
        applyTextures(model, ctx.provider().existingFileHelper);
    }

    public static class Builder<P> extends SimpleBuilder<MachineModel, P, Builder<P>> {
        @Nullable
        private ResourceLocation casing = null;
        @Nullable
        private ResourceLocation overlay = null;
        private ResourceLocation ioTex = tex(IO_TEX);
        private final Map<Direction, ResourceLocation> dirOverlay = new HashMap<>();

        private Builder(P parent) {
            super(parent);
        }

        private static ResourceLocation tex(String val) {
            return gregtech("blocks/" + val);
        }

        public Builder<P> casing(ResourceLocation val) {
            casing = val;
            return this;
        }

        public Builder<P> casing(Voltage voltage) {
            return casing(casingTex(voltage));
        }

        public Builder<P> casing(String val) {
            return casing(tex(val));
        }

        public Builder<P> overlay(ResourceLocation val) {
            overlay = val;
            return this;
        }

        public Builder<P> overlay(String val) {
            return overlay(tex(val));
        }

        public Builder<P> overlay(Direction dir, ResourceLocation val) {
            dirOverlay.put(dir, val);
            return this;
        }

        public Builder<P> overlay(Direction dir, String val) {
            return overlay(dir, tex(val));
        }

        public Builder<P> ioTex(ResourceLocation val) {
            ioTex = val;
            return this;
        }

        public Builder<P> ioTex(String val) {
            return ioTex(tex(val));
        }

        @Override
        protected MachineModel createObject() {
            return new MachineModel(casing, overlay, ioTex, dirOverlay);
        }
    }

    public static void genBlockModels(IDataContext<BlockModelProvider> ctx) {
        genCasingModel(ctx);
        genIOModel(ctx);
    }

    public static Builder<?> builder() {
        return new Builder<>(Unit.INSTANCE);
    }

    public static <U extends Block, P> Builder<IBlockDataBuilder<U, P>> builder(
        IBlockDataBuilder<U, P> parent) {
        return new Builder<>(parent)
            .onCreateObject(model -> parent.blockState(model.blockState()));
    }

    private static void genCasingModel(IDataContext<BlockModelProvider> ctx) {
        var model = ctx.provider().withExistingParent(CASING_MODEL, mcLoc("block/block"))
            .texture("particle", "#side")
            .element().from(0, 0, 0).to(16, 16, 16)
            .allFaces((d, f) -> f.cullface(d).texture(switch (d) {
                case UP -> "#top";
                case DOWN -> "#bottom";
                default -> "#side";
            })).end()
            .element().from(0, 0, 0).to(16, 16, 16)
            .allFaces((d, f) -> {
                f.cullface(d).texture("#" + DIR_TEX_KEYS.get(d) + "_overlay");
                if (d == Direction.NORTH) {
                    f.tintindex(0);
                } else if (d == Direction.SOUTH) {
                    f.tintindex(1);
                }
            })
            .end();
        for (var texKey : DIR_TEX_KEYS.values()) {
            model.texture(texKey + "_overlay", VOID_TEX);
        }
    }

    private static void genIOModel(IDataContext<BlockModelProvider> ctx) {
        ctx.provider().withExistingParent(IO_MODEL, mcLoc("block/block"))
            .element().from(0, 0, 0).to(16, 16, 16)
            .face(FRONT_FACING)
            .cullface(FRONT_FACING)
            .texture("#io_overlay")
            .tintindex(2)
            .end().end();
    }

    private static ResourceLocation casingTex(Voltage voltage) {
        if (voltage == Voltage.PRIMITIVE) {
            return PRIMITIVE_TEX;
        }
        return gregtech("blocks/casings/voltage/" + voltage.name().toLowerCase());
    }
}
