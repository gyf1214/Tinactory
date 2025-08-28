package org.shsts.tinactory.datagen.content;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.TierSortingRegistry;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.tool.ToolItem;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MaterialExporter {
    public static void init() {
        exportMaterial();
    }

    private static String formatColor(int color) {
        return "0x%08X".formatted(color);
    }

    private static void exportMaterial() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (var mat : AllMaterials.SET.values()) {
            var jo = new JsonObject();

            jo.addProperty("color", formatColor(mat.color));

            var items = new ArrayList<String>();
            var tools = new ArrayList<String>();
            var usables = new ArrayList<String>();
            var aliases = new JsonObject();
            var fluids = new JsonObject();
            var existings = new JsonObject();
            var toolDurability = 0;
            Tier toolTier = null;

            for (var sub : mat.itemSubs()) {
                var entry = mat.entry(sub);
                if (mat.isAlias(sub)) {
                    var target = LocHelper.name(mat.tag(sub).location().getPath(), -2);
                    aliases.addProperty(sub, target);
                } else if (sub.startsWith("tool/")) {
                    var item = mat.item(sub);
                    if (item instanceof UsableToolItem usableItem) {
                        toolDurability = usableItem.durability();
                        toolTier = usableItem.tier();
                        usables.add(sub.substring(5));
                    } else {
                        toolDurability = ((ToolItem) item).durability();
                        tools.add(sub.substring(5));
                    }
                } else if (entry instanceof IEntry<? extends Item>) {
                    items.add(sub);
                } else {
                    var loc = mat.item(sub).getRegistryName();
                    assert loc != null;
                    existings.addProperty(sub, loc.toString());
                }
            }

            for (var sub : mat.fluidSubs()) {
                if (sub.equals("fluid")) {
                    if (mat.fluid(sub).get() != Fluids.WATER) {
                        var target = LocHelper.name(mat.fluidLoc(sub).getPath(), -2);
                        aliases.addProperty(sub, target);
                    } else {
                        aliases.addProperty(sub, "liquid");
                    }
                } else {
                    var jo1 = new JsonObject();
                    jo1.addProperty("baseAmount", mat.fluidAmount(sub, 1f));

                    var fluid = mat.fluid(sub).get();
                    if (fluid instanceof SimpleFluid simpleFluid) {
                        var attributes = fluid.getAttributes();
                        jo1.addProperty("texture", attributes.getStillTexture().toString());
                        if (attributes.getColor() != mat.color) {
                            jo1.addProperty("textureColor", formatColor(attributes.getColor()));
                        }
                        if (simpleFluid.displayColor != mat.color) {
                            jo1.addProperty("displayColor", formatColor(simpleFluid.displayColor));
                        }
                    } else {
                        var loc = mat.fluidLoc(sub);
                        jo1.addProperty("existing", loc.toString());
                    }
                    fluids.add(sub, jo1);
                }
            }

            if (mat.hasBlock("ore")) {
                jo.addProperty("ore", mat.oreVariant().getName());
            }

            items.sort(String::compareTo);
            var ja1 = new JsonArray();
            items.forEach(ja1::add);

            if (!tools.isEmpty() || !usables.isEmpty()) {
                var jo1 = new JsonObject();

                if (toolTier != null) {
                    var tierName = TierSortingRegistry.getName(toolTier);
                    assert tierName != null;
                    jo1.addProperty("tier", tierName.toString());
                }
                jo1.addProperty("durability", toolDurability);

                tools.sort(String::compareTo);
                usables.sort(String::compareTo);
                var ja2 = new JsonArray();
                var ja3 = new JsonArray();
                tools.forEach(ja2::add);
                usables.forEach(ja3::add);
                jo1.add("items", ja2);
                jo1.add("usables", ja3);

                jo.add("tools", jo1);
            }

            jo.add("items", ja1);
            jo.add("fluids", fluids);
            jo.add("aliases", aliases);
            jo.add("existings", existings);

            var path = Paths.get("materials", mat.name + ".json");
            try (var is = Files.newOutputStream(path);
                var writer = new OutputStreamWriter(is)) {
                gson.toJson(jo, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
