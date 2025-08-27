package org.shsts.tinactory.datagen.content;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.util.LocHelper;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

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

            var items = new JsonArray();
            var aliases = new JsonObject();
            var fluids = new JsonObject();

            for (var sub : mat.itemSubs()) {
                if (mat.isAlias(sub)) {
                    var target = LocHelper.name(mat.tag(sub).location().getPath(), -2);
                    aliases.addProperty(sub, target);
                } else if (!sub.startsWith("tool/")) {
                    items.add(sub);
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

            jo.add("items", items);
            jo.add("fluids", fluids);
            jo.add("aliases", aliases);

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
