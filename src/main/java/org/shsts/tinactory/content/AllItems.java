package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<ToolItem> TEST_WRENCH;

    public static final RegistryEntry<ToolItem> STONE_HAMMER;
    public static final RegistryEntry<ToolItem> FLINT_MORTAR;

    static {
        TEST_WRENCH = toolItem("wrench", "test", 0xFFFFFF, 500);
        STONE_HAMMER = toolItem("hammer", "stone", materialColor(Material.STONE), 500);
        FLINT_MORTAR = toolItem("mortar", "flint", 0x2E2D2D, 500);
    }

    private static int materialColor(Material material) {
        return material.getColor().calculateRGBColor(MaterialColor.Brightness.NORMAL);
    }

    private static ResourceLocation toolTex(String id) {
        return ModelGen.gregtech("items/tools/" + id);
    }

    private static RegistryEntry<ToolItem> toolItem(String category, String material, int tint, int maxDurability) {
        var handle = switch (category) {
            case "saw" -> toolTex("handle_saw");
            case "hammer" -> toolTex("handle_hammer");
            case "mortar" -> toolTex("mortar_base");
            default -> ModelGen.VOID_TEX;
        };
        var head = ModelGen.gregtech("items/tools/" + category);
        var id = "tool/" + category + "/" + material;

        return REGISTRATE.item(id, properties -> new ToolItem(properties, 1, maxDurability))
                .model(ModelGen.basicItem(handle, head))
                .tag(AllTags.TOOL, AllTags.extend(AllTags.TOOL, category))
                .tint(0xFFFFFF, tint)
                .register();
    }

    public static void init() {}
}
