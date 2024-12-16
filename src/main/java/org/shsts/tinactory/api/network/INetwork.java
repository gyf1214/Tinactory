package org.shsts.tinactory.api.network;

import com.google.common.collect.Multimap;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import org.shsts.tinactory.api.machine.IMachine;

import java.util.Collection;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INetwork {
    <T extends INetworkComponent> T getComponent(IComponentType<T> type);

    BlockPos getSubnet(BlockPos pos);

    /**
     * @return multi map subnet -> machine.
     */
    Multimap<BlockPos, IMachine> allMachines();

    /**
     * @return entry collection (block, subnet).
     */
    Collection<Map.Entry<BlockPos, BlockPos>> allBlocks();
}
