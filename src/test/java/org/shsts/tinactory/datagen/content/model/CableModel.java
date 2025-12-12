package org.shsts.tinactory.datagen.content.model;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.network.CableBlock;
import org.shsts.tinycorelib.datagen.api.context.IDataContext;
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.shsts.tinactory.core.network.CableBlock.PIPE_RADIUS;
import static org.shsts.tinactory.core.network.CableBlock.RADIUS;
import static org.shsts.tinactory.core.network.CableBlock.SMALL_WIRE_RADIUS;
import static org.shsts.tinactory.core.network.CableBlock.WIRE_RADIUS;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.Models.FRONT_FACING;
import static org.shsts.tinactory.datagen.content.Models.WHITE_TEX;
import static org.shsts.tinactory.datagen.content.Models.xRotation;
import static org.shsts.tinactory.datagen.content.Models.yRotation;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CableModel {
    private static final Set<Direction> OPEN_FACES = Arrays.stream(Direction.values())
        .filter(dir -> dir != FRONT_FACING.getOpposite())
        .collect(Collectors.toUnmodifiableSet());

    private static final String OPEN_MODEL = "block/network/cable/open";
    private static final String OPEN_WIRE_MODEL = "block/network/cable/open_wire";
    private static final String CLOSED_MODEL = "block/network/cable/closed";
    private static final String CLOSED_WIRE_MODEL = "block/network/cable/closed_wire";
    private static final String ITEM_MODEL = "item/network/cable";
    private static final String ITEM_WIRE_MODEL = "item/material/wire";
    private static final String ITEM_PIPE_MODEL = "item/material/pipe";
    private static final String TEX_PREFIX = "blocks/cable/";
    private static final ResourceLocation INSULATION_OPEN_TEX = gregtech(TEX_PREFIX + "insulation_1");
    private static final ResourceLocation INSULATION_TEX = gregtech(TEX_PREFIX + "insulation_5");
    private static final ResourceLocation WIRE_TEX = gregtech(TEX_PREFIX + "wire");
    private static final ResourceLocation PIPE_SIDE_TEX = gregtech("blocks/pipe/pipe_side");
    private static final ResourceLocation PIPE_IN_TEX = gregtech("blocks/pipe/pipe_normal_in");

    private static BlockModelBuilder genOpenEnd(BlockModelProvider prov, String id, int radius, boolean insulation) {
        var model = prov.withExistingParent(id, prov.mcLoc("block/block"))
            .texture("particle", "#base");
        var element = model.element()
            .from(8 - radius, 8 - radius, 0)
            .to(8 + radius, 8 + radius, 8 - radius);
        for (var dir : OPEN_FACES) {
            var face = element.face(dir);
            if (dir == FRONT_FACING) {
                face.cullface(dir).texture("#wire").tintindex(1);
            } else {
                face.texture("#base").tintindex(0);
            }
        }
        if (insulation) {
            return model.element()
                .from(8 - radius, 8 - radius, 0)
                .to(8 + radius, 8 + radius, 8 - radius)
                .face(FRONT_FACING)
                .cullface(FRONT_FACING).texture("#insulation").tintindex(0)
                .end().end();
        }
        return model;
    }

    private static BlockModelBuilder genClosedEnd(BlockModelProvider prov, String id, int radius) {
        return prov.withExistingParent(id, prov.mcLoc("block/block"))
            .texture("particle", "#base")
            .element()
            .from(8 - radius, 8 - radius, 8 - radius)
            .to(8 + radius, 8 + radius, 8 - radius)
            .face(FRONT_FACING).texture("#base").tintindex(0).end()
            .end();
    }

    private static ItemModelBuilder genItem(ItemModelProvider prov, String id,
        int radius, boolean insulation) {
        var model = prov.withExistingParent(id, prov.mcLoc("block/block"))
            .element()
            .from(0, 8 - radius, 8 - radius)
            .to(16, 8 + radius, 8 + radius)
            .allFaces((dir, face) -> {
                if (dir.getAxis() == Direction.Axis.X) {
                    face.texture("#wire").cullface(dir).tintindex(insulation ? 1 : 0);
                } else {
                    face.texture("#base").tintindex(0);
                }
            }).end();
        if (insulation) {
            return model.element()
                .from(0, 8 - radius, 8 - radius)
                .to(16, 8 + radius, 8 + radius)
                .face(Direction.EAST).end().face(Direction.WEST).end()
                .faces((dir, face) -> face.texture("#insulation").tintindex(0).cullface(dir))
                .end();
        }
        return model;
    }

    public static void genBlockModels(IDataContext<BlockModelProvider> ctx) {
        genOpenEnd(ctx.provider(), OPEN_MODEL, RADIUS, true)
            .texture("base", INSULATION_TEX)
            .texture("insulation", INSULATION_OPEN_TEX)
            .texture("wire", WIRE_TEX);
        genOpenEnd(ctx.provider(), OPEN_WIRE_MODEL, WIRE_RADIUS, false)
            .texture("base", WHITE_TEX)
            .texture("wire", WIRE_TEX);
        genClosedEnd(ctx.provider(), CLOSED_MODEL, RADIUS)
            .texture("base", INSULATION_TEX);
        genClosedEnd(ctx.provider(), CLOSED_WIRE_MODEL, WIRE_RADIUS)
            .texture("base", WHITE_TEX);
    }

    public static void genItemModels(IDataContext<ItemModelProvider> ctx) {
        genItem(ctx.provider(), ITEM_MODEL, RADIUS, true)
            .texture("base", INSULATION_TEX)
            .texture("insulation", INSULATION_OPEN_TEX)
            .texture("wire", WIRE_TEX);
        genItem(ctx.provider(), ITEM_WIRE_MODEL, SMALL_WIRE_RADIUS, false)
            .texture("base", WIRE_TEX)
            .texture("wire", WIRE_TEX);
        genItem(ctx.provider(), ITEM_PIPE_MODEL, PIPE_RADIUS, false)
            .texture("base", PIPE_SIDE_TEX)
            .texture("wire", PIPE_IN_TEX);
    }

    public static void blockState(IEntryDataContext<Block,
        ? extends CableBlock, BlockStateProvider> ctx, boolean wire) {
        var prov = ctx.provider();
        var models = prov.models();
        var multipart = prov.getMultipartBuilder(ctx.object());
        var openModel = wire ? OPEN_WIRE_MODEL : OPEN_MODEL;
        var closedModel = wire ? CLOSED_WIRE_MODEL : CLOSED_MODEL;

        for (var dir : Direction.values()) {
            var xRot = xRotation(dir);
            var yRot = yRotation(dir);
            var property = CableBlock.PROPERTY_BY_DIRECTION.get(dir);
            multipart.part()
                // open end
                .modelFile(models.getExistingFile(modLoc(openModel)))
                .rotationX(xRot).rotationY(yRot).addModel()
                .condition(property, true)
                .end().part()
                // closed end
                .modelFile(models.getExistingFile(modLoc(closedModel)))
                .rotationX(xRot).rotationY(yRot).addModel()
                .condition(property, false)
                .end();
        }
    }

    public static void cable(IEntryDataContext<Item,
        ? extends Item, ItemModelProvider> ctx) {
        ctx.provider().withExistingParent(ctx.id(), modLoc(ITEM_MODEL));
    }

    public static void wire(IEntryDataContext<Item,
        ? extends Item, ItemModelProvider> ctx) {
        ctx.provider().withExistingParent(ctx.id(), modLoc(ITEM_WIRE_MODEL));
    }

    public static void ulvCable(IEntryDataContext<Item,
        ? extends Item, ItemModelProvider> ctx) {
        genItem(ctx.provider(), ctx.id(), WIRE_RADIUS, false)
            .texture("base", WIRE_TEX)
            .texture("wire", "#base");
    }

    public static void pipe(IEntryDataContext<Item,
        ? extends Item, ItemModelProvider> ctx) {
        ctx.provider().withExistingParent(ctx.id(), modLoc(ITEM_PIPE_MODEL));
    }
}
