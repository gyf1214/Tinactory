package org.shsts.tinactory.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Network {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final Level world;
    protected final BlockPos center;

    protected class BFSContext {
        private final Queue<BlockPos> queue = new ArrayDeque<>();
        public final Map<BlockPos, BlockState> visited = new HashMap<>();

        public void clear() {
            queue.clear();
            visited.clear();

            if (world.isLoaded(center)) {
                queue.add(center);
                visited.put(center, world.getBlockState(center));
            }
        }

        private boolean connected(BlockPos pos, BlockState blockState, Direction dir) {
            return blockState.getBlock() instanceof IConnector connector &&
                    connector.isConnected(world, pos, blockState, dir);
        }

        public Optional<BlockPos> next() {
            if (queue.isEmpty()) {
                return Optional.empty();
            }

            var pos = queue.remove();
            var blockState = visited.get(pos);
            assert blockState != null;
            for (var dir : Direction.values()) {
                var pos1 = pos.relative(dir);
                if (visited.containsKey(pos1) || !world.isLoaded(pos1) || !connected(pos, blockState, dir)) {
                    continue;
                }
                var blockState1 = world.getBlockState(pos1);
                if (!connected(pos1, blockState1, dir.getOpposite())) {
                    continue;
                }
                queue.add(pos1);
                visited.put(pos1, blockState1);
            }

            return Optional.of(pos);
        }
    }

    public Network(Level world, BlockPos center) {
        this.world = world;
        this.center = center;
    }

    @MethodsReturnNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class Ref {
        @Nullable
        private Network network;

        public Ref(Network network) {
            this.network = network;
        }

        public Optional<Network> get() {
            return Optional.ofNullable(this.network);
        }

        public boolean valid() {
            return this.network != null;
        }
    }

    protected Ref ref = new Ref(this);

    protected void invalidate() {
        this.ref.network = null;
        this.ref = new Ref(this);
    }

    public Ref ref() {
        return ref;
    }
}
