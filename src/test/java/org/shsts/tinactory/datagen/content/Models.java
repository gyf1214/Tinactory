package org.shsts.tinactory.datagen.content;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.multiblock.MultiBlockInterfaceBlock;
import org.shsts.tinactory.datagen.content.model.CableModel;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinactory.datagen.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.extend;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.core.util.LocHelper.prepend;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;

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

    public static <U extends Item> Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    basicItem(ResourceLocation... layers) {
        return ctx -> {
            var provider = ctx.provider.withExistingParent(ctx.id, "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, layers[i]);
            }
        };
    }

    public static <U extends Item> Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    basicItem(String... layers) {
        return ctx -> {
            var provider = ctx.provider.withExistingParent(ctx.id, "item/generated");
            for (var i = 0; i < layers.length; i++) {
                provider.texture("layer" + i, gregtech("items/" + layers[i]));
            }
        };
    }

    public static <U extends Item> void
    componentItem(RegistryDataContext<Item, U, ItemModelProvider> ctx) {
        var names = ctx.id.split("/");
        var name = names[names.length - 1];
        var voltage = names[names.length - 2];

        var tex = "items/metaitems/" + name.replace('_', '.') + "." + voltage;
        ctx.provider.withExistingParent(ctx.id, "item/generated")
                .texture("layer0", gregtech(tex));
    }

    public static <U extends BatteryItem> void
    batteryItem(RegistryDataContext<Item, U, ItemModelProvider> ctx) {
        var voltage = ctx.object.voltage;
        var base = gregtech("items/metaitems/battery.re." + voltage.id + ".lithium");
        var model = ctx.provider.withExistingParent(ctx.id, "item/generated")
                .texture("layer0", extend(base, "1"));
        for (var i = 2; i <= 8; i++) {
            var override = ctx.provider.withExistingParent(ctx.id + "_" + i, "item/generated")
                    .texture("layer0", extend(base, Integer.toString(i)));
            model.override()
                    .model(override)
                    .predicate(BatteryItem.ITEM_PROPERTY, (float) (i - 1) / 8f);
        }
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> oreBlock(OreVariant variant) {
        return ctx -> {
            var models = ctx.provider.models();
            var loc = variant.baseBlock.getRegistryName();
            assert loc != null;
            var baseModel = new ConfiguredModel(models.getExistingFile(prepend(loc, "block")));
            var overlay = new ConfiguredModel(models.getExistingFile(modLoc("block/material/ore_overlay")));
            ctx.provider.simpleBlock(ctx.object, baseModel, overlay);
        };
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cubeBlock(String tex) {
        return ctx -> {
            var model = ctx.provider.models()
                    .withExistingParent(ctx.id, "block/cube")
                    .texture("particle", "#north");
            for (var entry : DIR_TEX_KEYS.entrySet()) {
                var faceTex = extend(gregtech("blocks/" + tex), entry.getValue());
                model.texture(entry.getKey().getName(), faceTex);
            }
            ctx.provider.horizontalBlock(ctx.object, model);
        };
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cubeTint(String tex) {
        return ctx -> {
            var model = ctx.provider.models()
                    .withExistingParent(ctx.id, modLoc("block/cube_tint"))
                    .texture("all", gregtech("blocks/" + tex));
            ctx.provider.simpleBlock(ctx.object, model);
        };
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> solidBlock(String tex) {
        return ctx -> {
            var model = ctx.provider.models()
                    .cubeAll(ctx.id, gregtech("blocks/" + tex));
            ctx.provider.simpleBlock(ctx.object, model);
        };
    }

    public static void cableBlock(RegistryDataContext<Block, ? extends CableBlock, BlockStateProvider> ctx) {
        var voltage = ctx.object.voltage;
        CableModel.blockState(ctx, voltage == Voltage.ULV);
    }

    public static void cableItem(RegistryDataContext<Item, BlockItem, ItemModelProvider> ctx) {
        var voltage = ((CableBlock) ctx.object.getBlock()).voltage;
        if (voltage == Voltage.ULV) {
            CableModel.ulvCable(ctx);
        } else {
            CableModel.cable(ctx);
        }
    }

    public static void wireItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.wire(ctx);
    }

    public static void pipeItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.pipe(ctx);
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    machineBlock(String overlay) {
        var model = MachineModel.builder()
                .overlay(overlay)
                .buildObject();
        return model.blockState();
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    machineBlock(String casing, String overlay) {
        var model = MachineModel.builder()
                .casing(casing)
                .overlay(overlay)
                .buildObject();
        return model.blockState();
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    machineBlock(Voltage voltage, String overlay) {
        var model = MachineModel.builder()
                .casing(voltage)
                .overlay(overlay)
                .buildObject();
        return model.blockState();
    }

    public static Consumer<RegistryDataContext<Block, MachineBlock<SmartBlockEntity>, BlockStateProvider>>
    multiBlockInterface(String ioTex) {
        return ctx -> {
            var models = ctx.provider.models();
            var model = MachineModel.builder()
                    .overlay(ioTex).ioTex(ioTex)
                    .buildObject();
            var fullModel = model.blockModel(ctx.id, ctx.object, false, models);
            var ioModel = model.ioModel(ctx.id, models)
                    .texture("particle", extend(model.getCasing(ctx.object), "side"));
            ctx.provider.getVariantBuilder(ctx.object)
                    .forAllStates(state -> {
                        var dir = state.getValue(MachineBlock.IO_FACING);
                        var baseModel = state.getValue(MultiBlockInterfaceBlock.JOINED) ?
                                ioModel : fullModel;
                        return rotateModel(baseModel, dir);
                    });
        };
    }

    public static <U extends Item>
    Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    machineItem(Voltage voltage, String overlay) {
        var model = MachineModel.builder()
                .casing(voltage)
                .overlay(overlay)
                .buildObject();
        return model::itemModel;
    }

    public static void init() {
        DATA_GEN.blockModel(ctx -> ctx.provider
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
                .blockModel(ctx -> IconSet.DULL.blockOverlay(ctx.provider,
                        "material/ore", "ore"));
    }
}
