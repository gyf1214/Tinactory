package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllItems {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<ToolItem> WRENCH;

    static {
        WRENCH = REGISTRATE.item("tool/wrench", properties -> new ToolItem(properties, 1, 200))
                .model(ModelGen.basicItem(ctx -> ctx.vendorLoc("gregtech", "items/tools/wrench")))
                .tint()
                .tag(AllTags.TOOL, AllTags.TOOL_WRENCH)
                .register();
    }

    public static void init() {}
}
