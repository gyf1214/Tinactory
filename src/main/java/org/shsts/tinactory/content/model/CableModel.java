package org.shsts.tinactory.content.model;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.shsts.tinactory.content.network.CableBlock.RADIUS;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class CableModel {
    private static final Set<Direction> OPEN_FACES = Arrays.stream(Direction.values())
            .filter(dir -> dir != ModelGen.FRONT_FACING.getOpposite())
            .collect(Collectors.toUnmodifiableSet());

    private static final String OPEN_MODEL = "block/network/cable/open";
    private static final String CLOSED_MODEL = "block/network/cable/closed";
    private static final String ITEM_MODEL = "item/network/cable/item";
    private static final String VENDOR = "gregtech";
    private static final String TEX_PREFIX = "blocks/cable/";
    private static final String INSULATION_OPEN_TEX = TEX_PREFIX + "insulation_1";
    private static final String INSULATION_TEX = TEX_PREFIX + "insulation_5";
    private static final String WIRE_TEX = TEX_PREFIX + "wire";

    private static BlockModelBuilder genOpenEnd(BlockModelProvider prov) {
        var model = prov.withExistingParent(OPEN_MODEL, prov.mcLoc("block/block"))
                .texture("particle", "#insulation");
        var element = model.element()
                .from(8 - RADIUS, 8 - RADIUS, 0)
                .to(8 + RADIUS, 8 + RADIUS, 8 - RADIUS);
        for (var dir : OPEN_FACES) {
            var face = element.face(dir);
            if (dir == ModelGen.FRONT_FACING) {
                face.cullface(dir).texture("#wire").tintindex(1);
            } else {
                face.texture("#insulation").tintindex(0);
            }
        }
        model.element()
                .from(8 - RADIUS, 8 - RADIUS, 0)
                .to(8 + RADIUS, 8 + RADIUS, 8 - RADIUS)
                .face(ModelGen.FRONT_FACING)
                .cullface(ModelGen.FRONT_FACING).texture("#insulationOpen").tintindex(0);
        return model;
    }

    private static BlockModelBuilder genClosedEnd(BlockModelProvider prov) {
        return prov.withExistingParent(CLOSED_MODEL, prov.mcLoc("block/block"))
                .texture("particle", "#insulation")
                .element()
                .from(8 - RADIUS, 8 - RADIUS, 8 - RADIUS)
                .to(8 + RADIUS, 8 + RADIUS, 8 - RADIUS)
                .face(ModelGen.FRONT_FACING).texture("#insulation").tintindex(0).end()
                .end();
    }

    private static ItemModelBuilder genItem(ItemModelProvider prov) {
        return prov.withExistingParent(ITEM_MODEL, prov.mcLoc("block/block"))
                .element()
                .from(0, 8 - RADIUS, 8 - RADIUS)
                .to(16, 8 + RADIUS, 8 + RADIUS)
                .allFaces((dir, face) -> {
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.texture("#wire").cullface(dir).tintindex(1);
                    } else {
                        face.texture("#insulation").tintindex(0);
                    }
                }).end()
                .element()
                .from(0, 8 - RADIUS, 8 - RADIUS)
                .to(16, 8 + RADIUS, 8 + RADIUS)
                .face(Direction.EAST).end().face(Direction.WEST).end()
                .faces((dir, face) -> face.texture("#insulationOpen").tintindex(0).cullface(dir))
                .end();
    }

    public static void genBlockModels(DataContext<BlockStateProvider> ctx) {
        genOpenEnd(ctx.provider.models())
                .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX))
                .texture("insulationOpen", ctx.vendorLoc(VENDOR, INSULATION_OPEN_TEX))
                .texture("wire", ctx.vendorLoc(VENDOR, WIRE_TEX));
        genClosedEnd(ctx.provider.models())
                .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX));
    }

    public static void genItemModels(DataContext<ItemModelProvider> ctx) {
        genItem(ctx.provider)
                .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX))
                .texture("insulationOpen", ctx.vendorLoc(VENDOR, INSULATION_OPEN_TEX))
                .texture("wire", ctx.vendorLoc(VENDOR, WIRE_TEX));
    }

    public static void blockState(RegistryDataContext<Block, ? extends CableBlock, BlockStateProvider> ctx) {
        var prov = ctx.provider;
        var models = prov.models();
        var multipart = prov.getMultipartBuilder(ctx.object);

        for (var dir : Direction.values()) {
            var xRot = ModelGen.xRotation(dir);
            var yRot = ModelGen.yRotation(dir);
            var property = CableBlock.PROPERTY_BY_DIRECTION.get(dir);
            multipart.part()
                    // open end
                    .modelFile(models.getExistingFile(prov.modLoc(OPEN_MODEL)))
                    .rotationX(xRot).rotationY(yRot).addModel()
                    .condition(property, true)
                    .end().part()
                    // closed end
                    .modelFile(models.getExistingFile(prov.modLoc(CLOSED_MODEL)))
                    .rotationX(xRot).rotationY(yRot).addModel()
                    .condition(property, false)
                    .end();
        }
    }

    public static void itemModel(RegistryDataContext<Item, BlockItem, ItemModelProvider> ctx) {
        ctx.provider.withExistingParent(ctx.id, ctx.modLoc(ITEM_MODEL));
    }
}
