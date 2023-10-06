package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.tool.WrenchItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllItems {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<WrenchItem> WRENCH;

    static {
        WRENCH = REGISTRATE.item("tool/wrench", WrenchItem::new)
                .model(ModelGen.basicItem(ctx -> ctx.vendorLoc("gregtech", "items/tools/wrench")))
                .register();
    }

    public static void init() {}
}
