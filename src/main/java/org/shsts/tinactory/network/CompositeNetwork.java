package org.shsts.tinactory.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.util.BiKeyHashMap;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompositeNetwork extends Network {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ComponentType<?>, Component> components = new HashMap<>();
    private final List<Machine> machines = new ArrayList<>();
    private final BiKeyHashMap<Scheduling, ComponentType<?>, Component.Ticker> componentSchedulings =
            new BiKeyHashMap<>();
    private final Multimap<Scheduling, Component.Ticker> machineSchedulings = ArrayListMultimap.create();

    public CompositeNetwork(Level world, BlockPos center) {
        super(world, center);
    }

    public <T extends Component> T getComponent(ComponentType<T> type) {
        return type.componentClass.cast(components.get(type));
    }

    public void attachComponent(ComponentType<?> type) {
        assert !this.components.containsKey(type);
        this.invalidate();
        var component = type.create(this);
        this.components.put(type, component);
        component.buildSchedulings(((scheduling, ticker) ->
                this.componentSchedulings.put(scheduling.get(), type, ticker)));
    }

    public void detachComponent(ComponentType<?> type) {
        assert this.components.containsKey(type);
        this.invalidate();
        this.components.remove(type);
        this.componentSchedulings.removeAllSecondary(type);
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
        this.forEachMachine(machine -> machine.buildSchedulings((scheduling, ticker) ->
                this.machineSchedulings.put(scheduling.get(), ticker)));
    }

    @Override
    protected void onDisconnect() {
        super.onDisconnect();
        this.forEachMachine(Machine::onDisconnectFromNetwork);
        this.forEachComponent(Component::onDisconnect);
        this.machines.clear();
    }


    @Override
    protected void doTick() {
        super.doTick();
        for (var scheduling : SchedulingManager.getSortedSchedulings()) {
            for (var entry : this.componentSchedulings.getPrimary(scheduling)) {
                entry.getValue().tick(this.world, this);
            }
            for (var ticker : this.machineSchedulings.get(scheduling)) {
                ticker.tick(this.world, this);
            }
        }
    }
}
