package org.shsts.tinactory.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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

    public static ResourceLocation extend(ResourceLocation loc, String suffix) {
        return new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
    }

    public static <U extends Item> Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    basicItem(Function<RegistryDataContext<Item, U, ItemModelProvider>, ResourceLocation> texture) {
        return ctx -> ctx.provider
                .withExistingParent(ctx.id, "item/generated")
                .texture("layer0", texture.apply(ctx));
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
        return $ -> $.blockState(model::blockState)
                .translucent();
    }

    public static <U extends Block>
    Consumer<RegistryDataContext<Block, U, BlockStateProvider>> primitive(ResourceLocation tex) {
        return ctx -> {
            var model = ctx.provider.models().withExistingParent(ctx.id, "block/cube");
            for (var entry : DIR_TEX_KEYS.entrySet()) {
                var faceTex = extend(tex, entry.getValue());
                model = model.texture(entry.getKey().getName(), faceTex);
            }
            ctx.provider.horizontalBlock(ctx.object, model);
        };
    }

    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static void init() {
        REGISTRATE.blockState(CableModel::genBlockModels);
        REGISTRATE.blockState(MachineModel::genBlockModels);

        REGISTRATE.itemModel(CableModel::genItemModels);
    }
}
