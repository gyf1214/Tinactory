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
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinycorelib.api.meta.MetaLoadingException;

import static org.shsts.tinactory.content.AllRegistries.FLUIDS;
import static org.shsts.tinactory.content.AllRegistries.ITEMS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialMeta extends MetaConsumer {
    public MaterialMeta() {
        super("Material");
    }

    private static int getColor(String hex) {
        if (!hex.startsWith("0x")) {
            throw new MetaLoadingException("Bad color code " + hex);
        }
        return Integer.parseUnsignedInt(hex.substring(2).toLowerCase(), 16);
    }

    private static int getColor(JsonObject jo, String member) {
        return getColor(GsonHelper.getAsString(jo, member));
    }

    private void buildItems(MaterialSet.Builder<?> builder, JsonObject jo, int burnTime) {
        var items = GsonHelper.getAsJsonArray(jo, "items");
        for (var item : items) {
            var sub = GsonHelper.convertToString(item, "items");
            // TODO: more flexible
            if (sub.equals("primary") && burnTime > 0) {
                builder.dummy(sub, properties -> new Item(properties) {
                    @Override
                    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                        return burnTime;
                    }
                });
            } else {
                builder.dummies(sub);
            }
        }

        var existings = GsonHelper.getAsJsonObject(jo, "existings");
        for (var entry : existings.entrySet()) {
            var loc = new ResourceLocation(GsonHelper.convertToString(entry.getValue(), "existings"));
            var item = ITEMS.getEntry(loc).get();
            builder.existing(entry.getKey(), item);
        }
    }

    private void buildFluid(MaterialSet.Builder<?> builder, String sub, JsonObject jo) {
        var baseAmount = GsonHelper.getAsInt(jo, "baseAmount");
        if (jo.has("existing")) {
            var loc = new ResourceLocation(GsonHelper.getAsString(jo, "existing"));
            var fluid = FLUIDS.getEntry(loc).get();
            builder.existing(sub, fluid, baseAmount);
        } else {
            var tex = new ResourceLocation(GsonHelper.getAsString(jo, "texture"));
            var texColor = jo.has("textureColor") ? getColor(jo, "textureColor") : builder.getColor();
            var displayColor = jo.has("displayColor") ? getColor(jo, "displayColor") : builder.getColor();
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
        var ja2 = GsonHelper.getAsJsonArray(jo, "usables");

        for (var entry : ja1) {
            var category = GsonHelper.convertToString(entry, "items");
            toolBuilder.toolItem(category);
        }

        for (var entry : ja2) {
            var category = GsonHelper.convertToString(entry, "usables");
            toolBuilder.usableItem(category);
        }
        toolBuilder.build();
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var color = getColor(jo, "color");
        var builder = AllMaterials.set(loc.getPath()).color(color);
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
