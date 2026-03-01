package org.shsts.tinactory.api.logistics;

public interface IContainer {
    int portSize();

    boolean hasPort(int port);

    PortDirection portDirection(int port);

    IPort<?> getPort(int port, ContainerAccess access);
}
