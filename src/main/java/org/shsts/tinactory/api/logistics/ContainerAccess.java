package org.shsts.tinactory.api.logistics;

/**
 * Container access for machine:
 * <ul>
 * <li>internal:  allow input/output for all ports, this is used by recipe processor.</li>
 * <li>menu:      allow input/output for input ports, but only allow output for output ports,
 *                this is used by menu.</li>
 * <li>external:  only allow input for input ports, output for output port,
 *                this is used by logistics.</li>
 * </ul>
 */
public enum ContainerAccess {
    INTERNAL, MENU, EXTERNAL
}
