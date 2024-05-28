package org.shsts.tinactory.content.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.StringUtils;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ModelGen {
    public static final Direction FRONT_FACING = Direction.NORTH;
    public static final Map<Direction, String> DIR_TEX_KEYS = ImmutableMap.of(
            Direction.UP, "top",
            Direction.DOWN, "bottom",
            Direction.SOUTH, "back",
            Direction.NORTH, "front",
            Direction.WEST, "left",
            Direction.EAST, "right");
    public static final ExistingFileHelper.IResourceType TEXTURE_TYPE =
            new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");

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

    public static final ResourceLocation VOID_TEX = modLoc("void");
    public static final ResourceLocation WHITE_TEX = modLoc("white");
    public static final ResourceLocation PRIMITIVE_CASING = gregtech("blocks/casings/wood_wall");

    public static ResourceLocation mcLoc(String id) {
        return new ResourceLocation(id);
    }

    public static ResourceLocation modLoc(String id) {
        return new ResourceLocation(Tinactory.ID, id);
    }

    public static ResourceLocation gregtech(String id) {
        return new ResourceLocation("gregtech", id);
    }

    public static ResourceLocation extend(ResourceLocation loc, String suffix) {
        if (StringUtils.isEmpty(suffix)) {
            return loc;
        }
        return new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
    }

    public static ResourceLocation prepend(ResourceLocation loc, String prefix) {
        if (StringUtils.isEmpty(prefix)) {
            return loc;
        }
        return new ResourceLocation(loc.getNamespace(), prefix + "/" + loc.getPath());
    }

    public static String translate(ResourceLocation id) {
        return id.getNamespace() + "." + id.getPath().replace('/', '.');
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

    public static <S extends BlockBuilder<? extends CableBlock, ?, S>>
    Transformer<S> cable() {
        return $ -> $.blockState(ctx -> CableModel.blockState(ctx, false))
                .itemModel(ctx -> CableModel.itemModel(ctx, false))
                .translucent();
    }

    public static <U extends Item>
    Consumer<RegistryDataContext<Item, U, ItemModelProvider>> wire() {
        return ctx -> CableModel.itemModel(ctx, true);
    }

    public static ResourceLocation casing(Voltage voltage) {
        return gregtech("blocks/casings/voltage/" + voltage.name().toLowerCase());
    }

    public static <S extends BlockBuilder<? extends MachineBlock<?>, ?, S>>
    Transformer<S> machine(Voltage voltage, ResourceLocation front) {
        var model = new MachineModel(casing(voltage), front);
        return $ -> $.blockState(model::blockState).translucent();
    }

    public static <S extends BlockBuilder<? extends Block, ?, S>>
    Transformer<S> primitive(ResourceLocation tex) {
        return $ -> $.blockState(ctx -> {
            var model = ctx.provider.models()
                    .withExistingParent(ctx.id, "block/cube")
                    .texture("particle", "#north");
            for (var entry : DIR_TEX_KEYS.entrySet()) {
                var faceTex = extend(tex, entry.getValue());
                model.texture(entry.getKey().getName(), faceTex);
            }
            ctx.provider.horizontalBlock(ctx.object, model);
        });
    }

    public static <S extends BlockBuilder<? extends PrimitiveBlock<?>, ?, S>>
    Transformer<S> primitiveMachine(ResourceLocation front) {
        var model = new MachineModel(PRIMITIVE_CASING, front);
        return $ -> $.blockState(model::primitiveBlockState).translucent();
    }

    public static <S extends BlockBuilder<? extends Block, ?, S>>
    Transformer<S> oreBlock(OreVariant variant) {
        return $ -> $.blockState(ctx -> {
            var models = ctx.provider.models();
            var loc = variant.baseBlock.getRegistryName();
            assert loc != null;
            var baseModel = new ConfiguredModel(models.getExistingFile(prepend(loc, "block")));
            var overlay = new ConfiguredModel(models.getExistingFile(modLoc("block/material/ore_overlay")));
            ctx.provider.simpleBlock(ctx.object, baseModel, overlay);
        }).translucent();
    }

    public static void init() {
        REGISTRATE.blockState(CableModel::genBlockModels);
        REGISTRATE.blockState(MachineModel::genBlockModels);

        REGISTRATE.itemModel(CableModel::genItemModels);

        REGISTRATE.blockState(ctx -> ctx.provider.models()
                .withExistingParent("cube_tint", ctx.provider.mcLoc("block/block"))
                .element()
                .from(0, 0, 0).to(16, 16, 16)
                .allFaces((dir, face) -> face
                        .texture("#all").cullface(dir).tintindex(0)
                        .end())
                .end()
                .texture("particle", "#all"));

        REGISTRATE.blockState(ctx -> IconSet.DULL.blockOverlay(ctx.provider.models(), "material/ore", "ore"));
    }
}
