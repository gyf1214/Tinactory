package org.shsts.tinactory.integration.network;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import org.shsts.tinactory.api.network.ISubnetLabel;

import java.util.Collection;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class SubnetLabel extends ForgeRegistryEntry<ISubnetLabel> implements ISubnetLabel {
    private static Collection<ISubnetLabel> subnetLabels;

    public static void onBake(IForgeRegistry<ISubnetLabel> registry) {
        subnetLabels = registry.getValues();
    }

    public static Collection<ISubnetLabel> getSubnetLabels() {
        return subnetLabels;
    }
}
