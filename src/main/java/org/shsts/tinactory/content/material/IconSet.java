package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record IconSet(String subfolder, @Nullable IconSet parent) {
    private static final ResourceLocation ITEM_LOC = ModelGen.gregtech("items/material_sets");
    private static final ResourceLocation BLOCK_LOC = ModelGen.gregtech("blocks/material_sets");

    public static final IconSet DULL = new IconSet();
    public static final IconSet ROUGH = new IconSet("rough");
    public static final IconSet METALLIC = new IconSet("metallic");
    public static final IconSet SHINY = new IconSet("shiny");
    public static final IconSet RUBY = new IconSet("ruby", SHINY);

    private IconSet() {
        this("dull", null);
    }

    private IconSet(String subfolder) {
        this(subfolder, DULL);
    }

    private Optional<ResourceLocation> getTex(ResourceLocation baseLoc, ExistingFileHelper helper, String sub) {
        for (var set = this; set != null; set = set.parent) {
            var loc = ModelGen.extend(baseLoc, set.subfolder + "/" + sub);
            if (helper.exists(loc, ModelGen.TEXTURE_TYPE)) {
                return Optional.of(loc);
            }
        }
        return Optional.empty();
    }

    public <U extends Item, P extends ItemModelProvider>
    Consumer<RegistryDataContext<Item, U, P>> itemModel(String sub) {
        return ctx -> {
            var helper = ctx.provider.existingFileHelper;
            var base = getTex(ITEM_LOC, helper, sub).orElseThrow(() -> new IllegalArgumentException(
                    "No icon %s for icon set %s".formatted(sub, subfolder)));
            var overlay = getTex(ITEM_LOC, helper, sub + "_overlay");
            var model = ctx.provider.withExistingParent(ctx.id, "item/generated")
                    .texture("layer0", base);
            overlay.ifPresent(resourceLocation -> model.texture("layer1", resourceLocation));
        };
    }

    public <T extends ModelBuilder<T>> T blockOverlay(ModelProvider<T> prov, String id, String sub) {
        var tex = getTex(BLOCK_LOC, prov.existingFileHelper, sub).orElseThrow(() ->
                new IllegalArgumentException("No block overlay %s for icon set %s"
                        .formatted(sub, subfolder)));
        return prov.withExistingParent(id + "_overlay", ModelGen.modLoc("block/cube_tint"))
                .texture("all", tex);
    }
}
