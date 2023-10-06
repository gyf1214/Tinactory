package org.shsts.tinactory.model;

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
import org.shsts.tinactory.content.network.CableSetting;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class CableModel {
    private static final Set<Direction> OPEN_FACES = Arrays.stream(Direction.values())
            .filter(dir -> dir != ModelGen.FRONT_FACING.getOpposite())
            .collect(Collectors.toUnmodifiableSet());

    private static final String OPEN_MODEL = "block/network/cable/%s_open";
    private static final String CLOSED_MODEL = "block/network/cable/%s_closed";
    private static final String ITEM_MODEL = "item/network/cable/%s_item";
    private static final String VENDOR = "gregtech";
    private static final String TEX_PREFIX = "blocks/cable/";
    private static final String INSULATION_TEX_PREFIX = TEX_PREFIX + "insulation";
    private static final String INSULATION_TEX = INSULATION_TEX_PREFIX + "_5";
    private static final String WIRE_TEX = TEX_PREFIX + "wire";

    private static BlockModelBuilder genOpenEnd(BlockModelProvider prov, CableSetting setting) {
        var name = OPEN_MODEL.formatted(setting.asId());
        var model = prov.withExistingParent(name, prov.mcLoc("block/block"))
                .texture("particle", "#insulation");
        var radius = setting.radius;
        var element = model.element()
                .from(8 - radius, 8 - radius, 0)
                .to(8 + radius, 8 + radius, 8 - radius);
        for (var dir : OPEN_FACES) {
            var face = element.face(dir);
            if (dir == ModelGen.FRONT_FACING) {
                face.cullface(dir).texture("#wire").tintindex(2);
            } else {
                face.texture("#insulation").tintindex(1);
            }
        }
        model.element()
                .from(8 - radius, 8 - radius, 0)
                .to(8 + radius, 8 + radius, 8 - radius)
                .face(ModelGen.FRONT_FACING)
                .cullface(ModelGen.FRONT_FACING).texture("#insulationOpen").tintindex(1);
        return model;
    }

    private static BlockModelBuilder genClosedEnd(BlockModelProvider prov, CableSetting setting) {
        var name = CLOSED_MODEL.formatted(setting.asId());
        var radius = setting.radius;
        return prov.withExistingParent(name, prov.mcLoc("block/block"))
                .texture("particle", "#insulation")
                .element()
                .from(8 - radius, 8 - radius, 8 - radius)
                .to(8 + radius, 8 + radius, 8 - radius)
                .face(ModelGen.FRONT_FACING).texture("#insulation").tintindex(1).end()
                .end();
    }

    private static ItemModelBuilder genItem(ItemModelProvider prov, CableSetting setting) {
        var name = ITEM_MODEL.formatted(setting.asId());
        var radius = setting.radius;
        return prov.withExistingParent(name, prov.mcLoc("block/block"))
                .element()
                .from(0, 8 - radius, 8 - radius)
                .to(16, 8 + radius, 8 + radius)
                .allFaces((dir, face) -> {
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.texture("#wire").cullface(dir).tintindex(2);
                    } else {
                        face.texture("#insulation").tintindex(1);
                    }
                }).end()
                .element()
                .from(0, 8 - radius, 8 - radius)
                .to(16, 8 + radius, 8 + radius)
                .face(Direction.EAST).end().face(Direction.WEST).end()
                .faces((dir, face) -> face.texture("#insulationOpen").tintindex(1).cullface(dir))
                .end();
    }

    public static void genBlockModels(DataContext<BlockStateProvider> ctx) {
        for (var setting : CableSetting.values()) {
            genOpenEnd(ctx.provider.models(), setting)
                    .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX))
                    .texture("insulationOpen", ctx.vendorLoc(VENDOR, INSULATION_TEX_PREFIX + "_" + setting.texId))
                    .texture("wire", ctx.vendorLoc(VENDOR, WIRE_TEX));
            genClosedEnd(ctx.provider.models(), setting)
                    .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX));
        }
    }

    public static void genItemModels(DataContext<ItemModelProvider> ctx) {
        for (var setting : CableSetting.values()) {
            genItem(ctx.provider, setting)
                    .texture("insulation", ctx.vendorLoc(VENDOR, INSULATION_TEX))
                    .texture("insulationOpen", ctx.vendorLoc(VENDOR, INSULATION_TEX_PREFIX + "_" + setting.texId))
                    .texture("wire", ctx.vendorLoc(VENDOR, WIRE_TEX));
        }
    }

    public static void blockState(RegistryDataContext<Block, ? extends CableBlock, BlockStateProvider> ctx) {
        var prov = ctx.provider;
        var models = prov.models();
        var multipart = prov.getMultipartBuilder(ctx.object);
        var setting = ctx.object.setting;

        for (var dir : Direction.values()) {
            var xRot = ModelGen.xRotation(dir);
            var yRot = ModelGen.yRotation(dir);
            var property = CableBlock.PROPERTY_BY_DIRECTION.get(dir);
            multipart.part()
                    // open end
                    .modelFile(models.getExistingFile(prov.modLoc(OPEN_MODEL.formatted(setting.asId()))))
                    .rotationX(xRot).rotationY(yRot).addModel()
                    .condition(property, true)
                    .end().part()
                    // closed end
                    .modelFile(models.getExistingFile(prov.modLoc(CLOSED_MODEL.formatted(setting.asId()))))
                    .rotationX(xRot).rotationY(yRot).addModel()
                    .condition(property, false)
                    .end();
        }
    }

    public static void itemModel(RegistryDataContext<Item, BlockItem, ItemModelProvider> ctx) {
        var setting = ((CableBlock) ctx.object.getBlock()).setting;
        ctx.provider.withExistingParent(ctx.id, ctx.modLoc(ITEM_MODEL.formatted(setting.asId())));
    }
}
