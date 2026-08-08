package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanroomDisplay implements IMultiblockDisplay {
    private static final int SIZE = 5;
    private static final BlockPos CONTROLLER = new BlockPos(2, 4, 2);
    private final IBlockIngredient base;
    private final IBlockIngredient ceiling;
    private final IBlockIngredient wall;
    @Nullable
    private final IBlockIngredient door;
    @Nullable
    private final IBlockIngredient connector;
    private final int maxSize;
    private final int maxHeight;
    private final int maxDoors;
    private final int maxConnectors;

    public CleanroomDisplay(IBlockIngredient base, IBlockIngredient ceiling, IBlockIngredient wall,
        @Nullable IBlockIngredient door, @Nullable IBlockIngredient connector, int maxSize, int maxHeight,
        int maxDoors, int maxConnectors) {
        this.base = base;
        this.ceiling = ceiling;
        this.wall = wall;
        this.door = door;
        this.connector = connector;
        this.maxSize = maxSize;
        this.maxHeight = maxHeight;
        this.maxDoors = maxDoors;
        this.maxConnectors = maxConnectors;
    }

    @Override
    public int width() {
        return SIZE;
    }

    @Override
    public int depth() {
        return SIZE;
    }

    @Override
    public int height() {
        return SIZE;
    }

    @Override
    public BlockPos controllerPosition() {
        return CONTROLLER;
    }

    @Override
    public Optional<IBlockIngredient> getIngredient(int x, int y, int z) {
        if (x < 0 || x >= SIZE || y < 0 || y >= SIZE || z < 0 || z >= SIZE ||
            CONTROLLER.equals(new BlockPos(x, y, z))) {
            return Optional.empty();
        }
        if (y == 0 || y == 4 && (x == 0 || x == 4 || z == 0 || z == 4)) {
            return Optional.of(base);
        }
        if (y == 4) {
            return Optional.of(ceiling);
        }
        if (x == 0 || x == 4 || z == 0 || z == 4) {
            if (x == 2 && y == 2 && z == 0 && door != null) {
                return Optional.of(door);
            }
            if (x == 2 && y == 2 && z == 4 && connector != null) {
                return Optional.of(connector);
            }
            return Optional.of((x == 0 || x == 4) && (z == 0 || z == 4) ? base : wall);
        }
        return Optional.empty();
    }

    public int maxSize() {
        return maxSize;
    }

    public int maxHeight() {
        return maxHeight;
    }

    public int maxDoors() {
        return maxDoors;
    }

    public int maxConnectors() {
        return maxConnectors;
    }
}
