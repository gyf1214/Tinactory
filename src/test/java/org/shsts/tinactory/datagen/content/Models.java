package org.shsts.tinactory.datagen.content;

import com.google.common.collect.ImmutableMap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.multiblock.TurbineBlock;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.material.OreVariant;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.datagen.content.model.CableModel;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext;

import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.content.multiblock.TurbineBlock.CENTER_BLADE;
import static org.shsts.tinactory.core.util.LocHelper.extend;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.core.util.LocHelper.name;
import static org.shsts.tinactory.core.util.LocHelper.prepend;
import static org.shsts.tinactory.datagen.content.model.MachineModel.CASING_MODEL;
import static org.shsts.tinactory.datagen.content.model.MachineModel.applyCasing;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Models {
    public static final ExistingFileHelper.IResourceType TEXTURE_TYPE =
        new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");

    public static final Direction FRONT_FACING = Direction.NORTH;
    public static final Map<Direction, String> DIR_TEX_KEYS = ImmutableMap.of(
        Direction.UP, "top",
        Direction.DOWN, "bottom",
        Direction.SOUTH, "back",
        Direction.NORTH, "front",
        Direction.WEST, "left",
        Direction.EAST, "right");
    public static final ResourceLocation VOID_TEX = modLoc("void");
    public static final ResourceLocation WHITE_TEX = modLoc("white");

    public static int xRotation(Direction dir) {
        return switch (dir) {
            case DOWN -> 90;
            case UP -> 270;
            default -> 0;
        };
    }

    public static int yRotation(Direction dir) {
        if (dir.getAxis() == Direction.Axis.Y) {
            return 0;
        }
        return 90 * ((2 + dir.get2DDataValue()) % 4);
    }

    public static ConfiguredModel[] rotateModel(ModelFile model, Direction dir) {
        return ConfiguredModel.builder()
            .modelFile(model)
            .rotationX(xRotation(dir))
            .rotationY(yRotation(dir))
            .build();
    }

    public static <U extends Item> void basicItem(IEntryDataContext<Item, U, ItemModelProvider> ctx,
        ResourceLocation... layers) {
        Models.<U>basicItem(layers).accept(ctx);
    }

    public static <U extends Item> Consumer<IEntryDataContext<Item, U, ItemModelProvider>> basicItem(
        ResourceLocation... layers) {
        return ctx -> {
            var provider = ctx.provider().withExistingParent(ctx.id(), "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, layers[i]);
            }
        };
    }

    public static <U extends Item> Consumer<IEntryDataContext<Item, U, ItemModelProvider>> basicItem(
        String... layers) {
        return ctx -> {
            var provider = ctx.provider().withExistingParent(ctx.id(), "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, gregtech("items/" + layers[i]));
            }
        };
    }

    public static <U extends Item> void componentItem(
        IEntryDataContext<Item, U, ItemModelProvider> ctx) {
        var tex = "items/metaitems/" + name(ctx.id(), -1).replace('_', '.') + "." + name(ctx.id(), -2);
        ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", gregtech(tex));
    }

    public static <U extends Item> void simpleItem(
        IEntryDataContext<Item, U, ItemModelProvider> ctx) {
        var tex = "items/metaitems/" + name(ctx.id(), -1);
        ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", gregtech(tex));
    }

    public static <U extends BatteryItem> void batteryItem(
        IEntryDataContext<Item, U, ItemModelProvider> ctx) {
        var voltage = ctx.object().voltage;
        var base = gregtech("items/metaitems/battery.re." + voltage.id + ".lithium");
        var model = ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", extend(base, "1"));
        for (var i = 2; i <= 8; i++) {
            var override = ctx.provider().withExistingParent(ctx.id() + "_" + i, "item/generated")
                .texture("layer0", extend(base, Integer.toString(i)));
            model.override()
                .model(override)
                .predicate(BatteryItem.ITEM_PROPERTY, (float) (i - 1) / 8f);
        }
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> oreBlock(OreVariant variant) {
        return ctx -> {
            var models = ctx.provider().models();
            var loc = variant.baseBlock.getRegistryName();
            assert loc != null;
            var baseModel = new ConfiguredModel(models.getExistingFile(prepend(loc, "block")));
            var overlay = new ConfiguredModel(models.getExistingFile(modLoc("block/material/ore_overlay")));
            ctx.provider().simpleBlock(ctx.object(), baseModel, overlay);
        };
    }

    public static <U extends Block> void oreBlock(IEntryDataContext<Block,
        U, BlockStateProvider> ctx, OreVariant variant) {
        Models.<U>oreBlock(variant).accept(ctx);
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> cubeBlock(String tex) {
        return ctx -> {
            var model = ctx.provider().models()
                .withExistingParent(ctx.id(), "block/cube")
                .texture("particle", "#north");
            for (var entry : DIR_TEX_KEYS.entrySet()) {
                var faceTex = extend(gregtech("blocks/" + tex), entry.getValue());
                model.texture(entry.getKey().getName(), faceTex);
            }
            ctx.provider().horizontalBlock(ctx.object(), model);
        };
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> cubeColumn(String side, String end) {
        return ctx -> {
            var provider = ctx.provider();
            provider.simpleBlock(ctx.object(), provider.models().cubeColumn(
                ctx.id(),
                gregtech("blocks/" + side),
                gregtech("blocks/" + end)));
        };
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> cubeColumn(String tex) {
        return cubeColumn(tex + "/side", tex + "/top");
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> cubeTint(String tex) {
        return ctx -> {
            var model = ctx.provider().models()
                .withExistingParent(ctx.id(), modLoc("block/cube_tint"))
                .texture("all", gregtech("blocks/" + tex));
            ctx.provider().simpleBlock(ctx.object(), model);
        };
    }

    public static <U extends Block> void solidBlock(IEntryDataContext<Block,
        U, BlockStateProvider> ctx, String tex) {
        var model = ctx.provider().models()
            .cubeAll(ctx.id(), gregtech("blocks/" + tex));
        ctx.provider().simpleBlock(ctx.object(), model);
    }

    public static <U extends Block> Consumer<IEntryDataContext<Block,
        U, BlockStateProvider>> solidBlock(String tex) {
        return ctx -> solidBlock(ctx, tex);
    }

    public static void cableBlock(IEntryDataContext<Block,
        ? extends CableBlock, BlockStateProvider> ctx) {
        var voltage = ctx.object().voltage;
        CableModel.blockState(ctx, voltage == Voltage.ULV);
    }

    public static void cableItem(IEntryDataContext<Item,
        BlockItem, ItemModelProvider> ctx) {
        var voltage = ((CableBlock) ctx.object().getBlock()).voltage;
        if (voltage == Voltage.ULV) {
            CableModel.ulvCable(ctx);
        } else {
            CableModel.cable(ctx);
        }
    }

    public static void wireItem(IEntryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.wire(ctx);
    }

    public static void pipeItem(IEntryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.pipe(ctx);
    }

    public static void multiblockInterface(
        IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx, String ioTex) {
        var models = ctx.provider().models();
        var model = MachineModel.builder()
            .overlay(ioTex).ioTex(ioTex)
            .buildObject();
        var fullModel = model.blockModel(ctx.id(), ctx.object(), false, models);
        var ioModel = model.ioModel(ctx.id(), models)
            .texture("particle", extend(model.getCasing(ctx.object()), "side"));
        ctx.provider().getVariantBuilder(ctx.object())
            .forAllStates(state -> {
                var dir = state.getValue(MachineBlock.IO_FACING);
                var baseModel = state.getValue(MultiblockInterfaceBlock.JOINED) ?
                    ioModel : fullModel;
                return rotateModel(baseModel, dir);
            });
    }

    public static void turbineBlock(
        IEntryDataContext<Block, ? extends Block, BlockStateProvider> ctx,
        String casing, ResourceLocation idle, ResourceLocation spin) {
        var prov = ctx.provider();
        var models = prov.models();
        var existingHelper = ctx.provider().models().existingFileHelper;
        var blades = TurbineBlock.BLADES;
        var idles = new BlockModelBuilder[blades];
        var spins = new BlockModelBuilder[blades];
        for (var i = 0; i < blades; i++) {
            var id = ctx.id() + "_" + i;
            var id1 = id + "_active";
            var casingModel = modLoc(CASING_MODEL);
            var casingTex = gregtech("blocks/" + casing);

            idles[i] = applyCasing(models.withExistingParent(id, casingModel), casingTex, existingHelper)
                .texture("front_overlay", extend(idle, Integer.toString(i)));
            spins[i] = applyCasing(models.withExistingParent(id1, casingModel), casingTex, existingHelper)
                .texture("front_overlay", extend(spin, Integer.toString(i)));
        }

        prov.getVariantBuilder(ctx.object())
            .forAllStates(state -> {
                var dir = state.getValue(TurbineBlock.FACING);
                var i = state.getValue(TurbineBlock.BLADE);
                var baseModel = state.getValue(TurbineBlock.WORKING) ? spins[i] : idles[i];
                return rotateModel(baseModel, dir);
            });
    }

    public static void turbineItem(IEntryDataContext<Item, BlockItem, ItemModelProvider> ctx) {
        var id = ctx.id();
        var blockModel = new ResourceLocation(ctx.modid(), "block/" + id + "_" + CENTER_BLADE);
        ctx.provider().withExistingParent(id, blockModel);
    }

    public static <U extends Item> Consumer<IEntryDataContext<Item,
        U, ItemModelProvider>> machineItem(
        Voltage voltage, String overlay) {
        var model = MachineModel.builder()
            .casing(voltage)
            .overlay(overlay)
            .buildObject();
        return model::itemModel;
    }

    public static void init() {
        DATA_GEN.blockModel(ctx -> ctx.provider()
                .withExistingParent("cube_tint", mcLoc("block/block"))
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face
                    .texture("#all").cullface(dir).tintindex(0)
                    .end())
                .end()
                .texture("particle", "#all"))
            .blockModel(CableModel::genBlockModels)
            .itemModel(CableModel::genItemModels)
            .blockModel(MachineModel::genBlockModels)
            .blockModel(ctx -> IconSet.DULL.blockOverlay(ctx.provider(),
                "material/ore", "ore"));
    }
}
