package org.shsts.tinactory.content.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.StringUtils;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

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

    public static final ResourceLocation VOID_TEX = new ResourceLocation(Tinactory.ID, "void");
    public static final String VENDOR_PATH = "vendor/%s/%s";

    public static ResourceLocation modLoc(String id) {
        return new ResourceLocation(Tinactory.ID, id);
    }

    public static ResourceLocation vendorLoc(String vendor, String id) {
        return modLoc(ModelGen.VENDOR_PATH.formatted(vendor, id));
    }

    public static ResourceLocation gregtech(String id) {
        return vendorLoc("gregtech", id);
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
        return $ -> $.blockState(CableModel::blockState)
                .itemModel(CableModel::itemModel)
                .translucent();
    }

    public static <S extends BlockBuilder<? extends MachineBlock<?>, ?, S>>
    Transformer<S> machine(ResourceLocation casing, ResourceLocation front) {
        var model = new MachineModel(casing, front);
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

    public static <S extends BlockBuilder<? extends Block, ?, S>>
    Transformer<S> primitiveMachine(ResourceLocation overlay) {
        return $ -> $.blockState(ctx -> {
            var existingHelper = ctx.provider.models().existingFileHelper;
            var model = ctx.provider.models()
                    .withExistingParent(ctx.id, modLoc("block/machine/casing"));
            MachineModel.casing(model, gregtech("blocks/casings/wood_wall"));
            for (var dir : DIR_TEX_KEYS.values()) {
                var faceTex = new ResourceLocation(overlay.getNamespace(), overlay.getPath() + "_" + dir);
                if (existingHelper.exists(faceTex, TEXTURE_TYPE)) {
                    model.texture(dir + "_overlay", faceTex);
                }
            }
            ctx.provider.horizontalBlock(ctx.object, model);
        }).translucent();
    }

    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

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