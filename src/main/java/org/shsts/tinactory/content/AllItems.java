package org.shsts.tinactory.content;

import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllItems {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<ToolItem> WRENCH;
    public static final RegistryEntry<ToolItem> SAW;

    static {
        WRENCH = REGISTRATE.item("tool/wrench", properties -> new ToolItem(properties, 1, 200))
                .model(ModelGen.basicItem(ModelGen.vendorLoc("gregtech", "items/tools/wrench")))
                .tag(AllTags.TOOL, AllTags.TOOL_WRENCH)
                .register();
        SAW = REGISTRATE.item("tool/saw", properties -> new ToolItem(properties, 1, 500))
                .model(ModelGen.basicItem(ModelGen.vendorLoc("gregtech", "items/tools/saw")))
                .tag(AllTags.TOOL, AllTags.TOOL_SAW)
                .register();
    }

    public static void init() {}
}
