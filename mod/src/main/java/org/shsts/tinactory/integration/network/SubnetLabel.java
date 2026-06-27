package org.shsts.tinactory.integration.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import org.shsts.tinactory.api.network.ISubnetLabel;

import java.util.Collection;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SubnetLabel implements ISubnetLabel {
    private static Collection<ISubnetLabel> subnetLabels;

    public static void onBake(Registry<ISubnetLabel> registry) {
        subnetLabels = registry.stream().toList();
    }

    public static Collection<ISubnetLabel> getSubnetLabels() {
        return subnetLabels;
    }
}
