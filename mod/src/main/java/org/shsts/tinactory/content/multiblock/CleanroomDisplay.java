package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import org.shsts.tinactory.AllTags;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.multiblock.BlockIngredient;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.core.Transformer;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanroomDisplay implements IMultiblockDisplay {
    private final IBlockIngredient base;
    private final IBlockIngredient baseWithInterface;
    private final IBlockIngredient ceiling;
    private final IBlockIngredient wall;
    private final IBlockIngredient doorLower;
    private final IBlockIngredient doorUpper;
    private final IBlockIngredient connector;
    private final int displaySize;
    private final int displayHeight;
    private final BlockPos controllerPos;
    private final int maxSize;
    private final int maxHeight;
    private final int maxDoors;
    private final int maxConnectors;

    private static <T extends Comparable<T>> Transformer<BlockState> setState(Property<T> property, T value) {
        return $ -> $.hasProperty(property) ? $.setValue(property, value) : $;
    }

    public CleanroomDisplay(BlockIngredient base, IBlockIngredient ceiling, IBlockIngredient.Value wall,
        TagKey<Block> door, TagKey<Block> connector, int displaySize, int displayHeight,
        int maxSize, int maxHeight, int maxDoors, int maxConnectors) {
        this.base = base;
        this.baseWithInterface = BlockIngredient.of(base,
            BlockIngredient.tagValue(AllTags.MULTIBLOCK_INTERFACE));
        this.ceiling = ceiling;
        this.wall = BlockIngredient.of(wall, BlockIngredient.tagValue(door),
            BlockIngredient.tagValue(connector),
            BlockIngredient.tagValue(AllTags.MULTIBLOCK_INTERFACE));

        var doorDisplay = BlockIngredient.of(door);
        this.doorLower = BlockIngredient.withDisplay(this.wall, doorDisplay,
            setState(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        this.doorUpper = BlockIngredient.withDisplay(this.wall, doorDisplay,
            setState(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        this.connector = BlockIngredient.withDisplay(this.wall, BlockIngredient.of(connector),
            setState(MachineBlock.IO_FACING, Direction.EAST));

        this.displaySize = displaySize;
        this.displayHeight = displayHeight;
        this.controllerPos = new BlockPos((displaySize - 1) / 2, displayHeight - 1, (displaySize - 1) / 2);
        this.maxSize = maxSize;
        this.maxHeight = maxHeight;
        this.maxDoors = maxDoors;
        this.maxConnectors = maxConnectors;
    }

    @Override
    public int width() {
        return displaySize;
    }

    @Override
    public int depth() {
        return displaySize;
    }

    @Override
    public int height() {
        return displayHeight;
    }

    @Override
    public BlockPos controllerPosition() {
        return controllerPos;
    }

    private boolean atWall(int x, int z) {
        return x == 0 || x == displaySize - 1 || z == 0 || z == displaySize - 1;
    }

    @Override
    public Optional<IBlockIngredient> getIngredient(int x, int y, int z) {
        if (x < 0 || x >= displaySize || y < 0 || y >= displayHeight || z < 0 || z >= displaySize ||
            controllerPos.equals(new BlockPos(x, y, z))) {
            return Optional.empty();
        }

        if (y == 0 ||
            (y == displayHeight - 1 && atWall(x, z)) ||
            ((x == 0 || x == displaySize - 1) && (z == 0 || z == displaySize - 1))) {
            return Optional.of(base);
        }

        if (y == displayHeight - 1) {
            return Optional.of(ceiling);
        }

        if (!atWall(x, z)) {
            return Optional.empty();
        }

        var mid = (displaySize - 1) / 2;
        if (x == mid && z == displaySize - 1) {
            if (y == 1) {
                return Optional.of(doorLower);
            } else if (y == 2) {
                return Optional.of(doorUpper);
            }
        } else if (x == displaySize - 1 && y == 1 && z == mid) {
            return Optional.of(connector);
        }

        return Optional.of(wall);
    }

    @Override
    public List<StructureIngredient> getStructureIngredients() {
        return List.of(
            new StructureIngredient(base, 4L * (displaySize - 1)),
            new StructureIngredient(baseWithInterface, (long) displaySize * displaySize + 4L * (displayHeight - 2)),
            new StructureIngredient(ceiling, (long) (displaySize - 2) * (displaySize - 2) - 1),
            new StructureIngredient(wall, 4L * (displaySize - 2) * (displayHeight - 2)));
    }

    @Override
    public List<Component> getDetailLines() {
        var line1 = maxSize == 3 && maxHeight == 3 ?
            I18n.tr("tinactory.jei.multiblock.sizeFixed", 3, 3, 3) :
            I18n.tr("tinactory.jei.multiblock.size", 3, 3, 3, maxSize, maxHeight, maxSize);
        return List.of(line1, I18n.tr("tinactory.jei.multiblock.cleanroom.optional", maxDoors, maxConnectors));
    }
}
