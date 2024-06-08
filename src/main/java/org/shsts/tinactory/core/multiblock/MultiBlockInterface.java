package org.shsts.tinactory.core.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;

import java.util.List;

public abstract class MultiBlockInterface extends SmartBlockEntity {
    public MultiBlockInterface(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract IContainer createContainer(List<Layout.PortInfo> ports);
}
