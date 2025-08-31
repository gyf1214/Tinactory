package org.shsts.tinactory.core.machine;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllBlockEntities.MACHINE_SETS;
import static org.shsts.tinactory.content.AllBlockEntities.PROCESSING_SETS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineMeta extends MetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public MachineMeta() {
        super("Machine");
    }

    private static class UnsupportedTypeException extends RuntimeException {
        public UnsupportedTypeException(String field, String value) {
            super("Unsupported " + field + ": " + value);
        }
    }

    private void parseImage(JsonObject jo, int sh, BiConsumer<Rect, Texture> cons) {
        var x = GsonHelper.getAsInt(jo, "x");
        var y = GsonHelper.getAsInt(jo, "y");
        var w = GsonHelper.getAsInt(jo, "width");
        var h = GsonHelper.getAsInt(jo, "height");
        var texLoc = new ResourceLocation(GsonHelper.getAsString(jo, "texture"));
        var tw = GsonHelper.getAsInt(jo, "textureWidth", w);
        var th = GsonHelper.getAsInt(jo, "textureHeight", sh * h);
        var tex = new Texture(texLoc, tw, th);
        cons.accept(new Rect(x, y, w, h), tex);
    }

    private Map<Voltage, Layout> buildLayouts(JsonObject jo) {
        var builder = Layout.builder();

        var ja1 = GsonHelper.getAsJsonArray(jo, "slots");
        for (var je1 : ja1) {
            var jo1 = GsonHelper.convertToJsonObject(je1, "slots");
            var port = GsonHelper.getAsInt(jo1, "port");
            var type = SlotType.fromName(GsonHelper.getAsString(jo1, "type"));
            var x = GsonHelper.getAsInt(jo1, "x");
            var y = GsonHelper.getAsInt(jo1, "y");
            Collection<Voltage> voltages;
            if (jo1.has("voltages")) {
                voltages = Voltage.parseJson(jo1, "voltages");
            } else {
                voltages = Arrays.asList(Voltage.values());
            }
            builder.slot(port, type, x, y, voltages);
        }

        var ja3 = GsonHelper.getAsJsonArray(jo, "images", new JsonArray());
        for (var je3 : ja3) {
            var jo2 = GsonHelper.convertToJsonObject(je3, "images");
            parseImage(jo2, 1, builder::image);
        }

        if (jo.has("progressBar")) {
            var jo3 = GsonHelper.getAsJsonObject(jo, "progressBar");
            parseImage(jo3, 2, builder::progressBar);
        }

        return builder.buildObject();
    }

    private IRecipeType<ProcessingRecipe.Builder> buildProcessingRecipe(
        String id, boolean displayInput) {
        var builder = displayInput ? REGISTRATE.recipeType(id, DisplayInputRecipe::builder) :
            REGISTRATE.recipeType(id, ProcessingRecipe.Builder::new);

        return builder.recipeClass(ProcessingRecipe.class)
            .serializer(ProcessingRecipe.SERIALIZER)
            .register();
    }

    private IEntry<? extends Block> buildPrimitiveMachine(String id,
        IRecipeType<? extends ProcessingRecipe.Builder> recipeType,
        Layout layout) {
        var machineId = "primitive/" + id;
        return BlockEntityBuilder.builder(machineId, PrimitiveBlock::new)
            .menu(AllMenus.PRIMITIVE_MACHINE)
            .blockEntity()
            .transform(PrimitiveMachine::factory)
            .transform(RecipeProcessor.machine(recipeType))
            .transform(StackProcessingContainer.factory(layout))
            .end()
            .translucent()
            .buildObject();
    }

    private IEntry<? extends Block> buildProcessingMachine(
        String id, Voltage v, IRecipeType<? extends ProcessingRecipe.Builder> recipeType,
        Layout layout, boolean autoRecipe, String menuType) {
        if (v == Voltage.PRIMITIVE) {
            return buildPrimitiveMachine(id, recipeType, layout);
        }

        var machineId = "machine/" + v.id + "/" + id;
        var menu = switch (menuType) {
            case "default" -> AllMenus.PROCESSING_MACHINE;
            case "marker" -> AllMenus.MARKER;
            case "markerWithNormal" -> AllMenus.MARKER_WITH_NORMAL;
            default -> throw new UnsupportedTypeException("menu", menuType);
        };

        return BlockEntityBuilder.builder(machineId, MachineBlock.factory(v))
            .menu(menu)
            .blockEntity()
            .transform(Machine::factory)
            .transform(StackProcessingContainer.factory(layout))
            .transform(autoRecipe ? RecipeProcessor.machine(recipeType) :
                RecipeProcessor.noAutoRecipe(recipeType))
            .end()
            .block()
            .translucent()
            .tint(i -> i == 2 ? v.color : 0xFFFFFFFF)
            .end()
            .buildObject();
    }

    private void doAcceptMeta(String id, JsonObject jo) {
        var recipe = GsonHelper.getAsString(jo, "recipe", "default");
        var recipeType = switch (recipe) {
            case "default" -> buildProcessingRecipe(id, false);
            case "displayInput" -> buildProcessingRecipe(id, true);
            default -> throw new UnsupportedTypeException("recipe", recipe);
        };

        var layouts = buildLayouts(GsonHelper.getAsJsonObject(jo, "layout"));
        var menu = GsonHelper.getAsString(jo, "menu", "default");

        var machineType = GsonHelper.getAsString(jo, "machine", "default");
        var machines = new HashMap<Voltage, IEntry<? extends Block>>();
        for (var v : Voltage.parseJson(jo, "voltages")) {
            var layout = layouts.get(v);
            var block = switch (machineType) {
                case "default" -> buildProcessingMachine(id, v, recipeType, layout, true, menu);
                case "noAutoRecipe" -> buildProcessingMachine(id, v, recipeType, layout, false, menu);
                default -> throw new UnsupportedTypeException("machine", machineType);
            };
            machines.put(v, block);
        }

        var set = new ProcessingSet(recipeType, layouts, machines);
        PROCESSING_SETS.add(set);
        MACHINE_SETS.put(id, set);
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var id = loc.getPath();
        try {
            doAcceptMeta(id, jo);
        } catch (UnsupportedTypeException ex) {
            LOGGER.debug("Skip unsupported type: " + loc, ex);
        }
    }
}
