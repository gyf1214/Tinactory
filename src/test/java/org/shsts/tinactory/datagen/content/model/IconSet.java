package org.shsts.tinactory.datagen.content.model;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext;

import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.extend;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.Models.TEXTURE_TYPE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record IconSet(String subfolder, @Nullable IconSet parent) {
    private static final ResourceLocation ITEM_LOC = gregtech("items/material_sets");
    private static final ResourceLocation BLOCK_LOC = gregtech("blocks/material_sets");
    private static final ResourceLocation MAGNETIC_LOC = gregtech("items/material_sets/magnetic/magnetic_overlay");

    public static final IconSet DULL = new IconSet();
    public static final IconSet ROUGH = new IconSet("rough");
    public static final IconSet METALLIC = new IconSet("metallic");
    public static final IconSet SHINY = new IconSet("shiny");
    public static final IconSet FINE = new IconSet("fine");
    public static final IconSet RUBY = new IconSet("ruby", SHINY);
    public static final IconSet GEM_VERTICAL = new IconSet("gem_vertical", SHINY);
    public static final IconSet GEM_HORIZONTAL = new IconSet("gem_horizontal", SHINY);
    public static final IconSet LIGNITE = new IconSet("lignite", ROUGH);
    public static final IconSet QUARTZ = new IconSet("quartz", ROUGH);
    public static final IconSet CERTUS = new IconSet("certus", QUARTZ);
    public static final IconSet LAPIS = new IconSet("lapis", QUARTZ);
    public static final IconSet BRIGHT = new IconSet("bright", SHINY);

    private IconSet() {
        this("dull", null);
    }

    private IconSet(String subfolder) {
        this(subfolder, DULL);
    }

    private Optional<ResourceLocation> getTex(ResourceLocation baseLoc,
        ExistingFileHelper helper, String sub) {
        if (sub.equals("magnetic_overlay")) {
            return Optional.of(MAGNETIC_LOC);
        }
        for (var set = this; set != null; set = set.parent) {
            var loc = extend(baseLoc, set.subfolder + "/" + sub);
            if (helper.exists(loc, TEXTURE_TYPE)) {
                return Optional.of(loc);
            }
        }
        return Optional.empty();
    }

    public <U extends Item, P extends ItemModelProvider> void itemModel(
        IEntryDataContext<Item, U, P> ctx, String sub) {
        this.<U, P>itemModel(sub).accept(ctx);
    }

    public <U extends Item, P extends ItemModelProvider> Consumer<IEntryDataContext<Item,
        U, P>> itemModel(String sub) {
        return ctx -> {
            var helper = ctx.provider().existingFileHelper;

            var sub1 = sub.equals("sheet") ? "plate" : sub;
            var baseSub = sub1.equals("magnetic") ? "stick" : sub1;
            var base = getTex(ITEM_LOC, helper, baseSub).orElseThrow(() -> new IllegalArgumentException(
                "No icon %s for icon set %s, item=%s".formatted(baseSub, subfolder, ctx.id())));
            var overlay = getTex(ITEM_LOC, helper, sub1 + "_overlay");

            var model = ctx.provider().withExistingParent(ctx.id(), "item/generated")
                .texture("layer0", base);
            overlay.ifPresent(loc -> model.texture("layer1", loc));
        };
    }

    public <T extends ModelBuilder<T>> T blockOverlay(ModelProvider<T> prov,
        String id, String sub) {
        var tex = getTex(BLOCK_LOC, prov.existingFileHelper, sub).orElseThrow(() ->
            new IllegalArgumentException("No block overlay %s for icon set %s"
                .formatted(sub, subfolder)));
        return prov.withExistingParent(id + "_overlay", modLoc("block/cube_tint"))
            .texture("all", tex);
    }
}
