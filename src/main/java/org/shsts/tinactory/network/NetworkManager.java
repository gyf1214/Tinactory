package org.shsts.tinactory.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkManager {
    protected final Level world;
    protected final Map<BlockPos, Network.Ref> networkMap = new HashMap<>();

    public NetworkManager(Level world) {
        this.world = world;
    }

    public boolean hasNetwork(BlockPos pos) {
        return networkMap.containsKey(pos) && networkMap.get(pos).valid();
    }

    public void putNetwork(BlockPos pos, Network network) {
        assert !this.hasNetwork(pos);
        this.networkMap.put(pos, network.ref());
    }
}
