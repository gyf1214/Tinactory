package org.shsts.tinactory.api.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetwork {
    ITeamProfile owner();

    <T extends INetworkComponent> T getComponent(IComponentType<T> type);

    BlockPos getSubnet(BlockPos pos, ISubnetLabel label);

    Collection<IMachine> allMachines();

    Collection<BlockPos> allBlocks();
}
