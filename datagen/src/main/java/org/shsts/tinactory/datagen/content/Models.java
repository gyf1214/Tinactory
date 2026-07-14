package org.shsts.tinactory.datagen.content;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.multiblock.TurbineBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.datagen.content.model.CableModel;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinactory.integration.material.OreVariant;
import org.shsts.tinactory.integration.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;
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
import static org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.model.MachineModel.CASING_MODEL;
import static org.shsts.tinactory.datagen.content.model.MachineModel.applyCasing;
import static org.shsts.tinactory.datagen.content.model.MachineModel.applyCompanion;

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
    public static final ResourceLocation BLOCK_VOID_TEX = modLoc("block/void");
    public static final ResourceLocation BLOCK_WHITE_TEX = modLoc("block/white");
    public static final ResourceLocation ITEM_VOID_TEX = modLoc("item/void");
    public static final ResourceLocation CUTOUT_RENDER_TYPE = mcLoc("cutout");
    public static final ResourceLocation TRANSLUCENT_RENDER_TYPE = mcLoc("translucent");

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

    public static <U extends Item> void basicItem(IEntryDataContext<U, ItemModelProvider> ctx,
        ResourceLocation... layers) {
        Models.<U>basicItem(layers).accept(ctx);
    }

    public static <U extends Item> Consumer<IEntryDataContext<U, ItemModelProvider>> basicItem(
        ResourceLocation... layers) {
        return ctx -> {
            var provider = ctx.provider().withExistingParent(ctx.id(), "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, layers[i]);
            }
        };
    }

    public static <U extends Item> Consumer<IEntryDataContext<U, ItemModelProvider>> basicItem(
        String... layers) {
        return ctx -> {
            var provider = ctx.provider().withExistingParent(ctx.id(), "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, gregtech("item/" + layers[i]));
            }
        };
    }

    public static <U extends Item> void componentItem(
        IEntryDataContext<U, ItemModelProvider> ctx) {
        var tex = "item/metaitems/" + name(ctx.id(), -1).replace('_', '.') + "." + name(ctx.id(), -2);
        ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", gregtech(tex));
    }

    public static <U extends Item> void simpleItem(
        IEntryDataContext<U, ItemModelProvider> ctx) {
        var tex = "item/metaitems/" + name(ctx.id(), -1);
        ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", gregtech(tex));
    }

    public static <U extends BatteryItem> void batteryItem(
        IEntryDataContext<U, ItemModelProvider> ctx) {
        var voltage = ctx.object().voltage;
        var base = gregtech("item/metaitems/battery.re." + voltage.id + ".lithium");
        var model = ctx.provider().withExistingParent(ctx.id(), "item/generated")
            .texture("layer0", extend(base, "1"));
        for (var i = 2; i <= 8; i++) {
            var override = ctx.provider().withExistingParent(ctx.id() + "_" + i, "item/generated")
                .texture("layer0", extend(base, Integer.toString(i)));
            model.override()
                .model(override)
                .predicate(modLoc(BatteryItem.ITEM_PROPERTY), (float) (i - 1) / 8f);
        }
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> oreBlock(
        OreVariant variant) {
        return ctx -> {
            var models = ctx.provider().models();
            var loc = BuiltInRegistries.BLOCK.getKey(variant.baseBlock);
            var baseModel = new ConfiguredModel(models.getExistingFile(prepend(loc, "block")));
            var overlay = new ConfiguredModel(models.getExistingFile(modLoc("block/material/ore_overlay")));
            ctx.provider().simpleBlock(ctx.object(), baseModel, overlay);
        };
    }

    public static <U extends Block> void oreBlock(IEntryDataContext<U, BlockStateProvider> ctx,
        OreVariant variant) {
        Models.<U>oreBlock(variant).accept(ctx);
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeBlock(String tex) {
        return ctx -> {
            var model = ctx.provider().models()
                .withExistingParent(ctx.id(), "block/cube")
                .texture("particle", "#north");
            for (var entry : DIR_TEX_KEYS.entrySet()) {
                var faceTex = extend(gregtech("block/" + tex), entry.getValue());
                model.texture(entry.getKey().getName(), faceTex);
            }
            ctx.provider().horizontalBlock(ctx.object(), model);
        };
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeColumn(
        ResourceLocation side, ResourceLocation end) {
        return ctx -> {
            var provider = ctx.provider();
            provider.simpleBlock(ctx.object(), provider.models().cubeColumn(
                ctx.id(), side, end));
        };
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeColumn(
        String side, String end) {
        return cubeColumn(gregtech("block/" + side), gregtech("block/" + end));
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeColumn(
        String tex) {
        return cubeColumn(tex + "/side", tex + "/top");
    }

    public static BlockModelBuilder cubeCasingModel(BlockModelProvider models, String id,
        String casing, String overlay) {
        var baseModel = modLoc(CASING_MODEL);
        var casingTex = gregtech("block/" + casing);
        var overlayTex = gregtech("block/" + overlay);
        var model = applyCasing(models.withExistingParent(id, baseModel), casingTex,
            models.existingFileHelper);
        for (var dir : new Direction[] { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }) {
            applyCompanion(model, DIR_TEX_KEYS.get(dir) + "_overlay", overlayTex,
                models.existingFileHelper);
        }
        return model;
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeCasing(
        String casing, String overlay) {
        return ctx -> {
            var provider = ctx.provider();
            provider.simpleBlock(ctx.object(), cubeCasingModel(
                provider.models(), ctx.id(), casing, overlay));
        };
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeTint(
        String tex) {
        return cubeTint(tex, null);
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> cubeTint(
        String tex, @Nullable ResourceLocation renderType) {
        return ctx -> {
            var model = ctx.provider().models()
                .withExistingParent(ctx.id(), modLoc("block/cube_tint"));
            applyCompanion(model, "all", gregtech("block/" + tex),
                ctx.provider().models().existingFileHelper);
            if (renderType != null) {
                model.renderType(renderType);
            }
            ctx.provider().simpleBlock(ctx.object(), model);
        };
    }

    public static <U extends Block> void solidBlock(IEntryDataContext<U, BlockStateProvider> ctx,
        ResourceLocation tex) {
        solidBlock(ctx, tex, null);
    }

    public static <U extends Block> void solidBlock(IEntryDataContext<U, BlockStateProvider> ctx,
        ResourceLocation tex, @Nullable ResourceLocation renderType) {
        var model = ctx.provider().models().cubeAll(ctx.id(), tex);
        if (renderType != null) {
            model.renderType(renderType);
        }
        ctx.provider().simpleBlock(ctx.object(), model);
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> solidBlock(
        String tex) {
        return ctx -> solidBlock(ctx, gregtech("block/" + tex));
    }

    public static <U extends Block> Consumer<IEntryDataContext<U, BlockStateProvider>> solidBlock(
        String tex, ResourceLocation renderType) {
        return ctx -> solidBlock(ctx, gregtech("block/" + tex), renderType);
    }

    public static void cableBlock(IEntryDataContext<? extends CableBlock, BlockStateProvider> ctx) {
        var voltage = ctx.object().voltage;
        CableModel.blockState(ctx, voltage == Voltage.ULV);
    }

    public static void cableItem(IEntryDataContext<BlockItem, ItemModelProvider> ctx) {
        var voltage = ((CableBlock) ctx.object().getBlock()).voltage;
        if (voltage == Voltage.ULV) {
            CableModel.ulvCable(ctx);
        } else {
            CableModel.cable(ctx);
        }
    }

    public static void wireItem(IEntryDataContext<? extends Item, ItemModelProvider> ctx) {
        CableModel.wire(ctx);
    }

    public static void pipeItem(IEntryDataContext<? extends Item, ItemModelProvider> ctx) {
        CableModel.pipe(ctx);
    }

    public static void multiblockInterface(IEntryDataContext<? extends Block, BlockStateProvider> ctx,
        String ioTex) {
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

    public static void turbineBlock(IEntryDataContext<? extends Block, BlockStateProvider> ctx,
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
            var casingTex = gregtech("block/" + casing);

            idles[i] = applyCasing(models.withExistingParent(id, casingModel), casingTex, existingHelper)
                .renderType(CUTOUT_RENDER_TYPE)
                .texture("front_overlay", extend(idle, Integer.toString(i)));
            spins[i] = applyCasing(models.withExistingParent(id1, casingModel), casingTex, existingHelper)
                .renderType(CUTOUT_RENDER_TYPE)
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

    public static void turbineItem(IEntryDataContext<BlockItem, ItemModelProvider> ctx) {
        var id = ctx.id();
        var blockModel = ResourceLocation.fromNamespaceAndPath(ctx.modid(),
            "block/" + id + "_" + CENTER_BLADE);
        ctx.provider().withExistingParent(id, blockModel);
    }

    public static <U extends Item> Consumer<IEntryDataContext<U, ItemModelProvider>> machineItem(
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
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face
                    .texture("#all_emissive").cullface(dir).tintindex(0)
                    .end())
                .end()
                .texture("all_emissive", BLOCK_VOID_TEX)
                .texture("particle", "#all"))
            .blockModel(CableModel::genBlockModels)
            .itemModel(CableModel::genItemModels)
            .blockModel(MachineModel::genBlockModels)
            .blockModel(ctx -> {
                var model = IconSet.DULL.blockOverlay(ctx.provider(), "material/ore", "ore")
                    .renderType(CUTOUT_RENDER_TYPE);
                applyCompanion(model, "all", gregtech("block/material_sets/dull/ore"),
                    ctx.provider().existingFileHelper);
            })
            .blockModel(ctx -> ctx.provider()
                .withExistingParent("cube_column_emissive", mcLoc("block/block"))
                .texture("particle", "#side")
                .texture("side_emissive", BLOCK_VOID_TEX)
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face
                    .texture(dir.getAxis() == Direction.Axis.Y ? "#end" : "#side")
                    .cullface(dir)
                    .end())
                .end()
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .face(Direction.NORTH).texture("#side_emissive").cullface(Direction.NORTH).end()
                .face(Direction.SOUTH).texture("#side_emissive").cullface(Direction.SOUTH).end()
                .face(Direction.WEST).texture("#side_emissive").cullface(Direction.WEST).end()
                .face(Direction.EAST).texture("#side_emissive").cullface(Direction.EAST).end()
                .end());
    }
}
