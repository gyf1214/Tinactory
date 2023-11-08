package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record IconSet(String subfolder, @Nullable IconSet parent) {
    private static final ResourceLocation BASE_LOC = ModelGen.gregtech("items/material_sets");

    public static final IconSet DULL = new IconSet();
    public static final IconSet ROUGH = new IconSet("rough");
    public static final IconSet METALLIC = new IconSet("metallic");

    private IconSet() {
        this("dull", null);
    }

    private IconSet(String subfolder) {
        this(subfolder, DULL);
    }

    private Optional<ResourceLocation> getTex(ExistingFileHelper helper, String sub) {
        for (var set = this; set != null; set = set.parent) {
            var loc = ModelGen.extend(BASE_LOC, set.subfolder + "/" + sub);
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
            var base = this.getTex(helper, sub).orElseThrow(() -> new IllegalArgumentException(
                    "No icon %s for icon set %s".formatted(sub, this.subfolder)));
            var overlay = this.getTex(helper, sub + "_overlay");
            var model = ctx.provider.withExistingParent(ctx.id, "item/generated")
                    .texture("layer0", base);
            overlay.ifPresent(resourceLocation -> model.texture("layer1", resourceLocation));
        };
    }
}
