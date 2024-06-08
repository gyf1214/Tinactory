package org.shsts.tinactory.core.multiblock;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.multiblock.MultiBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.NetworkBase;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiBlockInterface extends SmartBlockEntity implements IContainer {
    private Machine machine;
    @Nullable
    private IContainer container = null;

    public MultiBlockInterface(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract IContainer createContainer(MultiBlock multiBlock);

    @Override
    protected void onServerLoad(Level world) {
        machine = AllCapabilities.MACHINE.get(this);
        super.onServerLoad(world);
    }

    public void setMultiBlock(MultiBlock multiBlock) {
        container = createContainer(multiBlock);
        machine.getNetwork().ifPresent(NetworkBase::invalidate);
    }

    public void resetMultiBlock() {
        container = null;
        machine.getNetwork().ifPresent(NetworkBase::invalidate);
    }

    @Override
    public Optional<? extends ITeamProfile> getOwnerTeam() {
        var world = getLevel();
        assert world != null;
        if (world.isClientSide) {
            return TechManager.localTeam();
        } else {
            return machine.getOwnerTeam();
        }
    }

    @Override
    public int portSize() {
        return container == null ? 0 : container.portSize();
    }

    @Override
    public boolean hasPort(int port) {
        return container != null && container.hasPort(port);
    }

    @Override
    public PortDirection portDirection(int port) {
        return container == null ? PortDirection.NONE : container.portDirection(port);
    }

    @Override
    public IPort getPort(int port, boolean internal) {
        return container == null ? IPort.EMPTY : container.getPort(port, internal);
    }
}
