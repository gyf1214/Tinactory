package org.shsts.tinactory.api.logistics;

public interface IContainer {
    boolean hasPort(int port);

    IPort getPort(int port, boolean internal);
}
