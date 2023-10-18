package org.shsts.tinactory.content;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.logistics.IItemCollection;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllCapabilities {

    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<Capability<IItemCollection>> ITEM_COLLECTION;

    static {
        ITEM_COLLECTION = REGISTRATE.capability(IItemCollection.class, new CapabilityToken<>() {});
    }

    public static void init() {}
}
