package org.shsts.tinactory.unit.fixture;

import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;

import java.util.HashMap;
import java.util.Map;

public final class TestContainer implements IContainer {
    private final Map<Integer, TestPort> ports = new HashMap<>();
    private final Map<Integer, PortDirection> directions = new HashMap<>();

    public TestContainer port(int index, PortDirection direction, TestPort port) {
        ports.put(index, port);
        directions.put(index, direction);
        return this;
    }

    @Override
    public int portSize() {
        return ports.size();
    }

    @Override
    public boolean hasPort(int port) {
        return ports.containsKey(port);
    }

    @Override
    public PortDirection portDirection(int port) {
        return directions.getOrDefault(port, PortDirection.NONE);
    }

    @Override
    public IPort<?> getPort(int port, ContainerAccess access) {
        return ports.containsKey(port) ? ports.get(port) : IPort.empty();
    }

    public TestPort getTestPort(int port) {
        return ports.get(port);
    }
}
