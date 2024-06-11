package org.shsts.tinactory.datagen.builder;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.model.CableModel;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.Models.VOID_TEX;
import static org.shsts.tinactory.datagen.content.Models.basicItem;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialBuilder<P> extends DataBuilder<Unit, P, MaterialBuilder<P>> {
    private static ResourceLocation toolTex(String sub) {
        return gregtech("items/tools/" + sub);
    }

    private static final Map<String, String> TOOL_HANDLE_TEX = ImmutableMap.<String, String>builder()
            .put("hammer", "handle_hammer")
            .put("mortar", "mortar_base")
            .put("file", "handle_file")
            .put("saw", "handle_saw")
            .put("screwdriver", "handle_screwdriver")
            .put("wire_cutter", "wire_cutter_base")
            .build();

    private final MaterialSet material;
    @Nullable
    private IconSet icon = null;

    public MaterialBuilder(DataGen dataGen, P parent, MaterialSet material) {
        super(dataGen, parent, material.name);
        this.material = material;
    }

    public MaterialBuilder<P> icon(IconSet value) {
        icon = value;
        return this;
    }

    @Override
    protected Unit createObject() {
        return Unit.INSTANCE;
    }

    private <U extends Item> Consumer<RegistryDataContext<Item, U, ItemModelProvider>>
    toolItem(String sub) {
        var category = sub.substring("tool/".length());
        var handle = Optional.ofNullable(TOOL_HANDLE_TEX.get(category))
                .map(MaterialBuilder::toolTex)
                .orElse(VOID_TEX);
        var head = gregtech("items/tools/" + category);
        return basicItem(handle, head);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void register() {
        assert icon != null;
        for (var sub : material.itemSubs()) {
            var prefixTag = AllMaterials.tag(sub);
            var tag = material.tag(sub);
            dataGen.tag(tag, prefixTag);

            if (material.isAlias(sub)) {
                continue;
            }

            if (material.hasTarget(sub)) {
                dataGen.tag(material.target(sub), tag);
                continue;
            }

            var entry = material.entry(sub);
            if (entry instanceof RegistryEntry<?> entry1) {
                var entry2 = (RegistryEntry<? extends Item>) entry1;
                var builder = dataGen.item(entry2).tag(tag);

                if (sub.startsWith("tool/")) {
                    builder = builder.model(toolItem(sub));
                } else if (sub.equals("wire")) {
                    builder = builder.model(CableModel::wireModel);
                } else if (sub.equals("pipe")) {
                    builder = builder.model(CableModel::pipeModel);
                } else if (sub.equals("raw")) {
                    builder = builder.model(basicItem(modLoc("items/material/raw")));
                } else {
                    builder = builder.model(icon.itemModel(sub));
                }
                builder.build();
            } else {
                dataGen.tag(entry, tag);
            }
        }
    }
}
