package org.shsts.tinactory.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelFile;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MachineModel {
    private static final String CASING_MODEL = "block/machine/casing";
    private static final Map<Direction, String> CASING_OVERLAY_TEX_KEYS = ImmutableMap.of(
            Direction.UP, "top_overlay",
            Direction.DOWN, "bottom_overlay",
            Direction.SOUTH, "back_overlay",
            Direction.NORTH, "front_overlay",
            Direction.WEST, "left_overlay",
            Direction.EAST, "right_overlay");

    private static final String IO_MODEL = "block/machine/io";
    private static final String IO_TEX_KEY = "io_overlay";
    private static final String IO_TEX = "blocks/overlay/appeng/me_output_bus";

    private static void genCasingModel(DataContext<BlockStateProvider> ctx) {
        var model = ctx.provider.models().withExistingParent(CASING_MODEL, DataContext.mcLoc("block/block"))
                .texture("particle", "#side")
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture(switch (d) {
                    case UP -> "#top";
                    case DOWN -> "#bottom";
                    default -> "#side";
                })).end()
                .element().from(0, 0, 0).to(16, 16, 16)
                .allFaces((d, f) -> f.cullface(d).texture("#" + CASING_OVERLAY_TEX_KEYS.get(d)))
                .end();
        for (var texKey : CASING_OVERLAY_TEX_KEYS.values()) {
            model.texture(texKey, ModelGen.VOID_TEX);
        }
    }

    private static void genIOModel(DataContext<BlockStateProvider> ctx) {
        ctx.provider.models().withExistingParent(IO_MODEL, DataContext.mcLoc("block/block"))
                .element().from(0, 0, 0).to(16, 16, 16)
                .face(ModelGen.FRONT_FACING)
                .cullface(ModelGen.FRONT_FACING)
                .texture("#" + IO_TEX_KEY)
                .end().end()
                .texture(IO_TEX_KEY, ctx.vendorLoc("gregtech", IO_TEX));
    }

    public static void genBlockModels(DataContext<BlockStateProvider> ctx) {
        genCasingModel(ctx);
        genIOModel(ctx);
    }

    private final ResourceLocation casing;
    private final ResourceLocation front;

    public MachineModel(ResourceLocation casing, ResourceLocation front) {
        this.casing = casing;
        this.front = front;
    }

    private static ResourceLocation suffix(ResourceLocation loc, String suffix) {
        return new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
    }

    private <B extends ModelBuilder<B>> B applyTextures(B model) {
        return model.texture("top", suffix(this.casing, "top"))
                .texture("bottom", suffix(this.casing, "bottom"))
                .texture("side", suffix(this.casing, "side"))
                .texture("front_overlay", this.front);
    }

    private ModelFile genModel(String id, BlockModelProvider prov) {
        return this.applyTextures(prov.withExistingParent(id, prov.modLoc(CASING_MODEL)));
    }

    public void blockState(RegistryDataContext<Block, ? extends MachineBlock<?>, BlockStateProvider> ctx) {
        var casing = this.genModel(ctx.id, ctx.provider.models());
        var io = ctx.provider.models().getExistingFile(ctx.modLoc(IO_MODEL));
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
}
