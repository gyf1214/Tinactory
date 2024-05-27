package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.ComponentSet;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllRecipes.TOOL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final RegistryEntry<CableBlock> ULV_CABLE;
    public static final Map<Voltage, ComponentSet> COMPONENT_SETS;

    static {
        ULV_CABLE = REGISTRATE.block("network/cable/ulv",
                        properties -> new CableBlock(properties, CableBlock.WIRE_RADIUS, Voltage.ULV, 2.0))
                .transform(ModelGen.cable(true))
                .tint(IRON.color, IRON.color)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();

        COMPONENT_SETS = ComponentSet.builder()
                .components(Voltage.LV)
                .material(WROUGHT_IRON)
                .cable(TIN, 1.0)
                .build()
                .components(Voltage.MV)
                .material(ALUMINIUM)
                .cable(COPPER, 1.0)
                .build()
                .buildObject();
    }

    public static void init() {}

    public static void initRecipes() {
        ulvRecipes();
        COMPONENT_SETS.values().forEach(ComponentSet::addRecipes);
    }

    private static void ulvRecipes() {
        TOOL.recipe(ULV_CABLE)
                .result(() -> ULV_CABLE.get().asItem(), 1)
                .pattern("#")
                .define('#', IRON.tag("plate"))
                .toolTag(AllTags.TOOL_WIRE_CUTTER)
                .build();
    }
}
