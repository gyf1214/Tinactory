package org.shsts.tinactory.datagen.content;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.network.CableBlock;
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
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> solidBlock(String tex) {
        return ctx -> {
            var model = ctx.provider.models()
                    .cubeAll(ctx.id, gregtech("blocks/" + tex));
            ctx.provider.simpleBlock(ctx.object, model);
        };
    }

    public static void cableBlock(RegistryDataContext<Block, ? extends CableBlock, BlockStateProvider> ctx) {
        CableModel.blockState(ctx, false);
    }

    public static void ulvCableBlock(RegistryDataContext<Block, ? extends CableBlock, BlockStateProvider> ctx) {
        CableModel.blockState(ctx, true);
    }

    public static void cableItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.cable(ctx);
    }

    public static void wireItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.wire(ctx);
    }

    public static void ulvCableItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.ulvCable(ctx);
    }

    public static void pipeItem(RegistryDataContext<Item, ? extends Item, ItemModelProvider> ctx) {
        CableModel.pipe(ctx);
    }

    private static MachineModel getMachineModel(Voltage voltage, String overlay) {
        return new MachineModel(voltage, gregtech("blocks/" + overlay));
    }

    private static MachineModel getMachineModel(String casing, String overlay) {
        return new MachineModel(gregtech("blocks/" + casing), gregtech("blocks/" + overlay));
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    machineBlock(Voltage voltage, String overlay) {
        return getMachineModel(voltage, overlay)::blockState;
    }

    public static <U extends Item>
    Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    machineItem(Voltage voltage, String overlay) {
        return getMachineModel(voltage, overlay)::itemModel;
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    primitiveBlock(String casing, String overlay) {
        return getMachineModel(casing, overlay)::primitiveBlockState;
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    primitiveBlock(String overlay) {
        return getMachineModel(Voltage.PRIMITIVE, overlay)::primitiveBlockState;
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>>
    sidedMachine(String casing, String overlay) {
        return getMachineModel(casing, overlay)::sidedBlockState;
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
