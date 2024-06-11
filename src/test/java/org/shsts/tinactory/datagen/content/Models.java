package org.shsts.tinactory.datagen.content;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

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
}
