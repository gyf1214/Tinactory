package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TestMachineLease implements IMachineLease {
    private final UUID machineId = UUID.randomUUID();
    private final Map<IStackKey, Long> inputCapacity;
    private final Map<IStackKey, Long> outputCapacity;
    private boolean valid;
    private int releaseCalls;

    public TestMachineLease(Map<IStackKey, Long> inputCapacity, Map<IStackKey, Long> outputCapacity) {
        this(inputCapacity, outputCapacity, true);
    }

    public TestMachineLease(Map<IStackKey, Long> inputCapacity, Map<IStackKey, Long> outputCapacity, boolean valid) {
        this.inputCapacity = new HashMap<>(inputCapacity);
        this.outputCapacity = new HashMap<>(outputCapacity);
        this.valid = valid;
    }

    @Override
    public UUID machineId() {
        return machineId;
    }

    @Override
    public List<IMachineRoute> inputRoutes() {
        return inputCapacity.keySet().stream()
            .map(key -> route(inputCapacity, key, PortDirection.INPUT, false))
            .toList();
    }

    @Override
    public List<IMachineRoute> outputRoutes() {
        return outputCapacity.keySet().stream()
            .map(key -> route(outputCapacity, key, PortDirection.OUTPUT, true))
            .toList();
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void release() {
        releaseCalls++;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int releaseCalls() {
        return releaseCalls;
    }

    public long outputCapacity(IStackKey key) {
        return outputCapacity.getOrDefault(key, 0L);
    }

    private static IMachineRoute route(
        Map<IStackKey, Long> capacity,
        IStackKey key,
        PortDirection direction,
        boolean consume) {

        return new IMachineRoute() {
            @Override
            public IStackKey key() {
                return key;
            }

            @Override
            public PortDirection direction() {
                return direction;
            }

            @Override
            public long transfer(long amount, boolean simulate) {
                var available = capacity.getOrDefault(key, 0L);
                var moved = Math.min(Math.max(0L, amount), available);
                if (consume && !simulate && moved > 0L) {
                    capacity.put(key, available - moved);
                }
                return moved;
            }
        };
    }
}
