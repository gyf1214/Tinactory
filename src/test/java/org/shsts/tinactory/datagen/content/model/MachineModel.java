package org.shsts.tinactory.datagen.content.model;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.SidedMachineBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.datagen.context.DataContext;
import org.shsts.tinactory.datagen.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

import static org.shsts.tinactory.core.util.LocHelper.extend;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.Models.DIR_TEX_KEYS;
import static org.shsts.tinactory.datagen.content.Models.FRONT_FACING;
import static org.shsts.tinactory.datagen.content.Models.TEXTURE_TYPE;
import static org.shsts.tinactory.datagen.content.Models.VOID_TEX;
import static org.shsts.tinactory.datagen.content.Models.xRotation;
import static org.shsts.tinactory.datagen.content.Models.yRotation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineModel {
    private static final String CASING_MODEL = "block/machine/casing";
    private static final String IO_MODEL = "block/machine/io";
    public static final ResourceLocation PRIMITIVE_TEX = gregtech("blocks/casings/wood_wall");
    public static final String IO_TEX = "overlay/appeng/me_output_bus";

    private static void genCasingModel(DataContext<BlockModelProvider> ctx) {
        var model = ctx.provider.withExistingParent(CASING_MODEL, mcLoc("block/block"))
                .texture("particle", "#side")
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture(switch (d) {
                    case UP -> "#top";
                    case DOWN -> "#bottom";
                    default -> "#side";
                })).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture("#" + DIR_TEX_KEYS.get(d) + "_overlay"))
                .end();
        for (var texKey : DIR_TEX_KEYS.values()) {
            model.texture(texKey + "_overlay", VOID_TEX);
        }
    }

    private static void genIOModel(DataContext<BlockModelProvider> ctx) {
        ctx.provider.withExistingParent(IO_MODEL, mcLoc("block/block"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(FRONT_FACING)
                .cullface(FRONT_FACING)
                .texture("#io_overlay")
                .end().end()
                .texture("io_overlay", gregtech("blocks/" + IO_TEX));
    }

    private static ResourceLocation casingTex(Voltage voltage) {
        if (voltage == Voltage.PRIMITIVE) {
            return PRIMITIVE_TEX;
        }
        return gregtech("blocks/casings/voltage/" + voltage.name().toLowerCase());
    }

    private static <B extends ModelBuilder<B>>
    B applyCasing(B model, ResourceLocation tex, ExistingFileHelper existingHelper) {
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

    @Nullable
    private final ResourceLocation casing;
    private final ResourceLocation overlay;

    public MachineModel(ResourceLocation overlay) {
        this.casing = null;
        this.overlay = overlay;
    }

    public MachineModel(ResourceLocation casing, ResourceLocation overlay) {
        this.casing = casing;
        this.overlay = overlay;
    }

    public MachineModel(Voltage voltage, ResourceLocation overlay) {
        this.casing = casingTex(voltage);
        this.overlay = overlay;
    }

    private <B extends ModelBuilder<B>> B
    applyTextures(B model, ResourceLocation casing, ExistingFileHelper existingHelper) {
        model = applyCasing(model, casing, existingHelper);
        if (existingHelper.exists(overlay, TEXTURE_TYPE)) {
            return model.texture("front_overlay", overlay);
        } else {
            for (var dir : DIR_TEX_KEYS.values()) {
                var loc = extend(overlay, "overlay_" + dir);
                if (existingHelper.exists(loc, TEXTURE_TYPE)) {
                    model = model.texture(dir + "_overlay", loc);
                }
            }
            var side = extend(overlay, "overlay_side");
            if (existingHelper.exists(side, TEXTURE_TYPE)) {
                model = model.texture("left_overlay", side).texture("right_overlay", side);
            }
            return model;
        }
    }

    private ResourceLocation getCasing(Block block) {
        if (casing != null) {
            return casing;
        }
        if (block instanceof PrimitiveBlock<?>) {
            return PRIMITIVE_TEX;
        } else if (block instanceof MachineBlock<?> machineBlock) {
            return casingTex(machineBlock.voltage);
        }
        throw new IllegalArgumentException();
    }

    private ModelFile genModel(String id, Block block, BlockModelProvider prov) {
        var model = prov.withExistingParent(id, modLoc(CASING_MODEL));
        return applyTextures(model, getCasing(block), prov.existingFileHelper);
    }

    private void primitive(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var model = genModel(ctx.id, ctx.object, ctx.provider.models());
        ctx.provider.horizontalBlock(ctx.object, model);
    }

    private void sided(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var model = genModel(ctx.id, ctx.object, ctx.provider.models());
        ctx.provider.getVariantBuilder(ctx.object)
                .forAllStates(state -> {
                    var dir = state.getValue(MachineBlock.IO_FACING);
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationX(xRotation(dir))
                            .rotationY(yRotation(dir))
                            .build();
                });
    }

    private void machine(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var casing = genModel(ctx.id, ctx.object, ctx.provider.models());
        var io = ctx.provider.models().getExistingFile(modLoc(IO_MODEL));
        var multipart = ctx.provider.getMultipartBuilder(ctx.object);

        for (var dir : Direction.values()) {
            if (dir.getAxis() != Direction.Axis.Y) {
                multipart.part().modelFile(casing)
                        .rotationY(yRotation(dir)).addModel()
                        .condition(MachineBlock.FACING, dir)
                        .end();
            }
        }

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

    public void blockState(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        if (ctx.object instanceof PrimitiveBlock<?>) {
            primitive(ctx);
        } else if (ctx.object instanceof SidedMachineBlock<?>) {
            sided(ctx);
        } else if (ctx.object instanceof MachineBlock<?>) {
            machine(ctx);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void itemModel(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        assert casing != null;
        var model = ctx.provider.withExistingParent(ctx.id, modLoc(CASING_MODEL));
        applyTextures(model, casing, ctx.provider.existingFileHelper);
    }

    public static void genBlockModels(DataContext<BlockModelProvider> ctx) {
        genCasingModel(ctx);
        genIOModel(ctx);
    }
}