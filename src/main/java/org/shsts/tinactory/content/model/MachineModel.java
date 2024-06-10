package org.shsts.tinactory.content.model;

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
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;

import static org.shsts.tinactory.content.model.ModelGen.TEXTURE_TYPE;
import static org.shsts.tinactory.content.model.ModelGen.extend;
import static org.shsts.tinactory.content.model.ModelGen.modLoc;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MachineModel {
    private static final String CASING_MODEL = "block/machine/casing";

    private static final String IO_MODEL = "block/machine/io";
    private static final String IO_TEX_KEY = "io_overlay";
    public static final String IO_TEX = "blocks/overlay/appeng/me_output_bus";

    private static void genCasingModel(DataContext<BlockStateProvider> ctx) {
        var model = ctx.provider.models().withExistingParent(CASING_MODEL, ModelGen.mcLoc("block/block"))
                .texture("particle", "#side")
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture(switch (d) {
                    case UP -> "#top";
                    case DOWN -> "#bottom";
                    default -> "#side";
                })).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture("#" + ModelGen.DIR_TEX_KEYS.get(d) + "_overlay"))
                .end();
        for (var texKey : ModelGen.DIR_TEX_KEYS.values()) {
            model.texture(texKey + "_overlay", ModelGen.VOID_TEX);
        }
    }

    private static void genIOModel(DataContext<BlockStateProvider> ctx) {
        ctx.provider.models().withExistingParent(IO_MODEL, ModelGen.mcLoc("block/block"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(ModelGen.FRONT_FACING)
                .cullface(ModelGen.FRONT_FACING)
                .texture("#" + IO_TEX_KEY)
                .end().end()
                .texture(IO_TEX_KEY, ModelGen.gregtech(IO_TEX));
    }

    public static void genBlockModels(DataContext<BlockStateProvider> ctx) {
        genCasingModel(ctx);
        genIOModel(ctx);
    }

    private final ResourceLocation casing;
    private final ResourceLocation overlay;

    public MachineModel(ResourceLocation casing, ResourceLocation overlay) {
        this.casing = casing;
        this.overlay = overlay;
    }

    public static <B extends ModelBuilder<B>>
    B casing(B model, ResourceLocation tex, ExistingFileHelper existingHelper) {
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

    private <B extends ModelBuilder<B>> B applyTextures(B model, ExistingFileHelper existingHelper) {
        model = casing(model, casing, existingHelper);
        if (existingHelper.exists(overlay, TEXTURE_TYPE)) {
            return model.texture("front_overlay", overlay);
        } else {
            for (var dir : ModelGen.DIR_TEX_KEYS.values()) {
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

    private ModelFile genModel(String id, BlockModelProvider prov) {
        return applyTextures(prov.withExistingParent(id, modLoc(CASING_MODEL)), prov.existingFileHelper);
    }

    public void primitiveBlockState(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var model = genModel(ctx.id, ctx.provider.models());
        ctx.provider.horizontalBlock(ctx.object, model);
    }

    public void sidedBlockState(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var model = genModel(ctx.id, ctx.provider.models());
        ctx.provider.getVariantBuilder(ctx.object)
                .forAllStates(state -> {
                    var dir = state.getValue(MachineBlock.IO_FACING);
                    return ConfiguredModel.builder()
                            .modelFile(model)
                            .rotationX(ModelGen.xRotation(dir))
                            .rotationY(ModelGen.yRotation(dir))
                            .build();
                });
    }

    public void blockState(RegistryDataContext<Block, ? extends Block, BlockStateProvider> ctx) {
        var casing = genModel(ctx.id, ctx.provider.models());
        var io = ctx.provider.models().getExistingFile(modLoc(IO_MODEL));
        var multipart = ctx.provider.getMultipartBuilder(ctx.object);

        for (var dir : Direction.values()) {
            if (dir.getAxis() != Direction.Axis.Y) {
                multipart.part().modelFile(casing)
                        .rotationY(ModelGen.yRotation(dir)).addModel()
                        .condition(MachineBlock.FACING, dir)
                        .end();
            }
        }

        for (var dir : Direction.values()) {
            var builder = multipart.part().modelFile(io);
            if (dir.getAxis() == Direction.Axis.Y) {
                builder.rotationX(ModelGen.xRotation(dir))
                        .rotationY(ModelGen.yRotation(dir)).addModel()
                        .condition(MachineBlock.IO_FACING, dir);
            } else {
                var otherDir = Arrays.stream(Direction.values())
                        .filter(d -> d.getAxis() != Direction.Axis.Y && d != dir)
                        .toArray(Direction[]::new);
                builder.rotationY(ModelGen.yRotation(dir)).addModel()
                        .condition(MachineBlock.FACING, otherDir)
                        .condition(MachineBlock.IO_FACING, dir);
            }
        }
    }

    public void itemModel(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        var model = ctx.provider.withExistingParent(ctx.id, modLoc(CASING_MODEL));
        applyTextures(model, ctx.provider.existingFileHelper);
    }
}
