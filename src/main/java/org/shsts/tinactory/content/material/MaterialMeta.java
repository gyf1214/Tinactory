package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.TierSortingRegistry;
import org.shsts.tinactory.AllMaterials;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.material.MaterialSet;
import org.shsts.tinactory.core.material.OreVariant;
import org.shsts.tinycorelib.api.meta.MetaLoadingException;

import static org.shsts.tinactory.AllRegistries.FLUIDS;
import static org.shsts.tinactory.AllRegistries.ITEMS;
import static org.shsts.tinactory.AllRegistries.SOUND_EVENTS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialMeta extends MetaConsumer {
    public MaterialMeta() {
        super("Material");
    }

    private static int parseColor(String hex) {
        if (!hex.startsWith("0x")) {
            throw new MetaLoadingException("Bad color code " + hex);
        }
        return Integer.parseUnsignedInt(hex.substring(2).toLowerCase(), 16);
    }

    public static int parseColor(JsonObject jo, String member) {
        return parseColor(GsonHelper.getAsString(jo, member));
    }

    private void buildItems(MaterialSet.Builder<?> builder, JsonObject jo, int burnTime) {
        var items = GsonHelper.getAsJsonArray(jo, "items");
        for (var item : items) {
            var sub = GsonHelper.convertToString(item, "items");
            // TODO: more flexible
            if (sub.equals("gem") && burnTime > 0) {
                builder.item(sub, properties -> new Item(properties) {
                    @Override
                    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                        return burnTime;
                    }
                });
            } else {
                builder.item(sub);
            }
        }

        var existings = GsonHelper.getAsJsonObject(jo, "existings");
        for (var entry : existings.entrySet()) {
            var loc = new ResourceLocation(GsonHelper.convertToString(entry.getValue(), "existings"));
            var item = ITEMS.getEntry(loc);
            builder.existing(entry.getKey(), item);
        }
    }

    private void buildFluid(MaterialSet.Builder<?> builder, String sub, JsonObject jo) {
        var baseAmount = GsonHelper.getAsInt(jo, "baseAmount");
        if (jo.has("existing")) {
            var loc = new ResourceLocation(GsonHelper.getAsString(jo, "existing"));
            var fluid = FLUIDS.getEntry(loc);
            builder.existing(sub, fluid, baseAmount);
        } else {
            var tex = new ResourceLocation(GsonHelper.getAsString(jo, "texture"));
            var texColor = jo.has("textureColor") ? parseColor(jo, "textureColor") : builder.getColor();
            var displayColor = jo.has("displayColor") ? parseColor(jo, "displayColor") : builder.getColor();
            builder.fluid(sub, tex, texColor, displayColor, baseAmount);
        }
    }

    private void buildFluids(MaterialSet.Builder<?> builder, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "fluids");
        for (var entry : jo1.entrySet()) {
            var jo2 = GsonHelper.convertToJsonObject(entry.getValue(), "fluids");
            buildFluid(builder, entry.getKey(), jo2);
        }
    }

    private void buildAliases(MaterialSet.Builder<?> builder, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "aliases");
        for (var entry : jo1.entrySet()) {
            var target = GsonHelper.convertToString(entry.getValue(), "aliases");
            builder.alias(entry.getKey(), target);
        }
    }

    private void buildTools(MaterialSet.Builder<?> builder, JsonObject jo) {
        var durability = GsonHelper.getAsInt(jo, "durability");
        Tier tier;
        if (jo.has("tier")) {
            var loc = new ResourceLocation(GsonHelper.getAsString(jo, "tier"));
            tier = TierSortingRegistry.byName(loc);
        } else {
            tier = null;
        }

        var toolBuilder = builder.tool(durability, tier);
        var ja1 = GsonHelper.getAsJsonArray(jo, "items");
        var jo1 = GsonHelper.getAsJsonObject(jo, "usables");

        for (var entry : ja1) {
            var category = GsonHelper.convertToString(entry, "items");
            toolBuilder.item(category);
        }

        for (var entry : jo1.entrySet()) {
            var category = entry.getKey();
            var jo2 = GsonHelper.convertToJsonObject(entry.getValue(), "usables");

            if (jo2.has("sound")) {
                var sound = SOUND_EVENTS.getEntry(new ResourceLocation(GsonHelper.getAsString(jo2, "sound")));
                toolBuilder.usable(category, sound);
            } else {
                toolBuilder.usable(category);
            }
        }
        toolBuilder.build();
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        if (jo.has("alias")) {
            var sub2 = GsonHelper.getAsString(jo, "alias");
            AllMaterials.alias(loc.getPath(), sub2);
            return;
        }

        var color = parseColor(jo, "color");
        var builder = AllMaterials.newMaterial(loc.getPath()).color(color);
        var burnTime = jo.has("burnTime") ? GsonHelper.getAsInt(jo, "burnTime") : -1;

        buildItems(builder, jo, burnTime);
        buildFluids(builder, jo);
        if (jo.has("ore")) {
            var variant = OreVariant.fromName(GsonHelper.getAsString(jo, "ore"));
            builder.oreOnly(variant);
        }
        buildAliases(builder, jo);
        if (jo.has("tools")) {
            buildTools(builder, GsonHelper.getAsJsonObject(jo, "tools"));
        }
        builder.build();
    }
}
