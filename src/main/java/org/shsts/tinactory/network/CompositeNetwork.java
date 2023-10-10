package org.shsts.tinactory.network;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompositeNetwork extends Network {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ComponentType<?>, Component> components = new IdentityHashMap<>();
    private final List<Machine> machines = new ArrayList<>();

    public CompositeNetwork(Level world, BlockPos center) {
        super(world, center);
    }

    public <T extends Component> T getComponent(ComponentType<T> type) {
        return type.componentClass.cast(components.get(type));
    }

    public <T extends Component> void attachComponent(ComponentType<T> type) {
        assert !this.components.containsKey(type);
        var component = type.create(this);
        this.components.put(type, component);
    }

    protected void forEachComponent(Consumer<Component> cons) {
        this.components.values().forEach(cons);
    }

    protected void forEachMachine(Consumer<Machine> cons) {
        this.machines.forEach(cons);
    }

    protected void putMachine(Machine be) {
        LOGGER.debug("network {}: put machine {}", this, be);
        this.machines.add(be);
    }

    @Override
    protected void putBlock(BlockPos pos, BlockState state) {
        super.putBlock(pos, state);
        this.forEachComponent(component -> component.putBlock(pos, state));
        if (state.getBlock() instanceof SmartEntityBlock<?> entityBlock) {
            entityBlock.getBlockEntity(this.world, pos, Machine.class).ifPresent(this::putMachine);
        }
    }

    @Override
    protected void connectFinish() {
        super.connectFinish();
        this.forEachComponent(Component::onConnect);
        this.forEachMachine(machine -> machine.onConnectToNetwork(this));
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        this.forEachMachine(Machine::onDisconnectFromNetwork);
        this.forEachComponent(Component::onDisconnect);
    }
}
