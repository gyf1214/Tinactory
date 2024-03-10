package org.shsts.tinactory.core.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.util.BiKeyHashMap;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CompositeNetwork extends Network {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ComponentType<?>, Component> components = new HashMap<>();
    private final List<Machine> machines = new ArrayList<>();
    private final BiKeyHashMap<IScheduling, ComponentType<?>, Component.Ticker> componentSchedulings =
            new BiKeyHashMap<>();
    private final Multimap<IScheduling, Component.Ticker> machineSchedulings = ArrayListMultimap.create();

    public CompositeNetwork(Level world, BlockPos center) {
        super(world, center);
        this.attachComponents();
    }

    public <T extends Component> T getComponent(Supplier<ComponentType<T>> typeSupp) {
        var type = typeSupp.get();
        return type.componentClass.cast(components.get(type));
    }

    protected void attachComponent(ComponentType<?> type) {
        assert !this.components.containsKey(type);
        var component = type.create(this);
        this.components.put(type, component);
        component.buildSchedulings(((scheduling, ticker) ->
                this.componentSchedulings.put(scheduling.get(), type, ticker)));
    }

    protected void attachComponents() {
        ComponentType.getComponentTypes().forEach(this::attachComponent);
    }

    public Collection<Machine> getMachines() {
        return this.machines;
    }

    protected void putMachine(Machine be) {
        LOGGER.debug("network {}: put machine {}", this, be);
        this.machines.add(be);
    }

    @Override
    protected void putBlock(BlockPos pos, BlockState state) {
        super.putBlock(pos, state);
        for (var component : this.components.values()) {
            component.putBlock(pos, state);
        }
        if (state.getBlock() instanceof SmartEntityBlock<?> entityBlock) {
            entityBlock.getBlockEntity(this.world, pos, Machine.class).ifPresent(this::putMachine);
        }
    }

    @Override
    protected void connectFinish() {
        super.connectFinish();
        for (var component : this.components.values()) {
            component.onConnect();
        }
        for (var machine : this.machines) {
            machine.onConnectToNetwork(this);
        }
        for (var machine : this.machines) {
            machine.buildSchedulings((scheduling, ticker) ->
                    this.machineSchedulings.put(scheduling.get(), ticker));
        }
    }

    @Override
    protected void onDisconnect() {
        for (var machine : this.machines) {
            machine.onDisconnectFromNetwork();
        }
        for (var component : this.components.values()) {
            component.onDisconnect();
        }
        this.machines.clear();
        this.machineSchedulings.clear();
        super.onDisconnect();
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
