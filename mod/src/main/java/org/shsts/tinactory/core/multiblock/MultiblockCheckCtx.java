package org.shsts.tinactory.core.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MultiblockCheckCtx<S> implements IMultiblockCheckCtx<S> {
    private boolean failed = false;
    private final BlockPos center;
    private final List<BlockPos> blocks = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();

    protected MultiblockCheckCtx(BlockPos center) {
        this.center = center;
    }

    @Override
    public boolean isFailed() {
        return failed;
    }

    @Override
    public void setFailed(boolean val) {
        failed = val;
    }

    @Override
    public BlockPos getCenter() {
        return center;
    }

    @Override
    public void addBlock(BlockPos pos) {
        blocks.add(pos);
    }

    public List<BlockPos> blocks() {
        return List.copyOf(blocks);
    }

    @Override
    public Object getProperty(String key) {
        var val = properties.get(key);
        assert val != null;
        return val;
    }

    @Override
    public void setProperty(String key, Object val) {
        properties.put(key, val);
    }

    @Override
    public void deleteProperty(String key) {
        properties.remove(key);
    }

    @Override
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
}
