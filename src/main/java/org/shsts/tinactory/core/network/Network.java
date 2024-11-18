package org.shsts.tinactory.core.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.util.BiKeyHashMap;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Network extends NetworkBase {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Map<ComponentType<?>, NetworkComponent> components = new HashMap<>();
    private final Multimap<BlockPos, Machine> subnetMachines = ArrayListMultimap.create();
    private final Map<BlockPos, BlockPos> blockSubnets = new HashMap<>();
    private final BiKeyHashMap<IScheduling, ComponentType<?>, NetworkComponent.Ticker> componentSchedulings =
        new BiKeyHashMap<>();
    private final Multimap<IScheduling, NetworkComponent.Ticker> machineSchedulings = ArrayListMultimap.create();

    public Network(Level world, BlockPos center, TeamProfile team) {
        super(world, center, team);
        attachComponents();
    }

    public <T extends NetworkComponent> T getComponent(Supplier<ComponentType<T>> typeSupp) {
        var type = typeSupp.get();
        return type.cast(components.get(type));
    }

    protected void attachComponent(ComponentType<?> type) {
        assert !components.containsKey(type);
        var component = type.create(this);
        components.put(type, component);
        component.buildSchedulings(((scheduling, ticker) ->
            componentSchedulings.put(scheduling.get(), type, ticker)));
    }

    protected void attachComponents() {
        ComponentType.getComponentTypes().forEach(this::attachComponent);
    }

    /**
     * @return map subnet -> machine
     */
    public Multimap<BlockPos, Machine> getAllMachines() {
        return subnetMachines;
    }

    public BlockPos getSubnet(BlockPos pos) {
        return blockSubnets.get(pos);
    }

    /**
     * @return entrySet subnet -> block
     */
    public Collection<Map.Entry<BlockPos, BlockPos>> getAllBlocks() {
        return blockSubnets.entrySet();
    }

    protected void putMachine(BlockPos subnet, Machine machine) {
        LOGGER.trace("{}: put machine {}", this, machine);
        subnetMachines.put(subnet, machine);
    }

    @Override
    protected void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
        super.putBlock(pos, state, subnet);
        blockSubnets.put(pos, subnet);
        for (var component : components.values()) {
            component.putBlock(pos, state, subnet);
        }
        if (state.getBlock() instanceof SmartEntityBlock<?> entityBlock) {
            entityBlock.getBlockEntity(world, pos)
                .flatMap(AllCapabilities.MACHINE::tryGet)
                .ifPresent(machine -> putMachine(subnet, machine));
        }
    }

    @Override
    protected void connectFinish() {
        super.connectFinish();
        for (var component : components.values()) {
            component.onConnect();
        }
        for (var machine : subnetMachines.values()) {
            machine.onConnectToNetwork(this);
        }
        for (var machine : subnetMachines.values()) {
            machine.buildSchedulings((scheduling, ticker) ->
                machineSchedulings.put(scheduling.get(), ticker));
        }
    }

    @Override
    protected void onDisconnect() {
        for (var machine : subnetMachines.values()) {
            machine.onDisconnectFromNetwork();
        }
        for (var component : components.values()) {
            component.onDisconnect();
        }
        subnetMachines.clear();
        blockSubnets.clear();
        machineSchedulings.clear();
        super.onDisconnect();
    }

    @Override
    protected void doTick() {
        super.doTick();
        for (var scheduling : SchedulingManager.getSortedSchedulings()) {
            for (var entry : componentSchedulings.getPrimary(scheduling)) {
                entry.getValue().tick(world, this);
            }
            for (var ticker : machineSchedulings.get(scheduling)) {
                ticker.tick(world, this);
            }
        }
    }
}
