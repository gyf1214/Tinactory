package org.shsts.tinactory.content.model;

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

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class MachineModel {
    private static final String CASING_MODEL = "block/machine/casing";

    private static final String IO_MODEL = "block/machine/io";
    private static final String IO_TEX_KEY = "io_overlay";
    private static final String IO_TEX = "blocks/overlay/appeng/me_output_bus";

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
    private final ResourceLocation front;

    public MachineModel(ResourceLocation casing, ResourceLocation front) {
        this.casing = casing;
        this.front = front;
    }

    public static <B extends ModelBuilder<B>>
    B casing(B model, ResourceLocation tex) {
        return model.texture("top", ModelGen.extend(tex, "top"))
                .texture("bottom", ModelGen.extend(tex, "bottom"))
                .texture("side", ModelGen.extend(tex, "side"));
    }

    private <B extends ModelBuilder<B>> B applyTextures(B model) {
        return casing(model, this.casing)
                .texture("front_overlay", this.front);
    }

    private ModelFile genModel(String id, BlockModelProvider prov) {
        return this.applyTextures(prov.withExistingParent(id, ModelGen.modLoc(CASING_MODEL)));
    }

    public void blockState(RegistryDataContext<Block, ? extends MachineBlock<?>, BlockStateProvider> ctx) {
        var casing = this.genModel(ctx.id, ctx.provider.models());
        var io = ctx.provider.models().getExistingFile(ModelGen.modLoc(IO_MODEL));
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
