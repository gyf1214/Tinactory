package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS;
import static org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MiscMeta extends MetaConsumer {
    public static final Transformer<BlockBehaviour.Properties> CASING_PROPERTY = $ -> $
        .strength(3f, 8f)
        .sound(SoundType.METAL)
        .requiresCorrectToolForDrops()
        .isValidSpawn(AllItems::never);
    private static final JsonObject EMPTY = new JsonObject();

    public MiscMeta() {
        super("Misc");
    }

    private IEntry<Block> block(String id, JsonObject jo) {
        var materialColor = MaterialColor.byId(GsonHelper.getAsInt(jo, "materialColor"));
        return REGISTRATE.block(id, Block::new)
            .material(Material.HEAVY_METAL, materialColor)
            .properties(CASING_PROPERTY)
            .register();
    }

    private void solid(String name, String id, JsonObject jo) {
        SOLID_CASINGS.put(name, block(id, jo));
    }

    private void coil(String name, String id, JsonObject jo) {
        var temperature = GsonHelper.getAsInt(jo, "temperature");
        // TODO: use string instead of integer
        var materialColor = MaterialColor.byId(GsonHelper.getAsInt(jo, "materialColor"));
        var block = REGISTRATE.block(id, CoilBlock.factory(temperature))
            .material(Material.HEAVY_METAL, materialColor)
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.put(name, block);
    }

    private void buildItem(String type, String name, String id, JsonObject jo) {
        switch (type) {
            case "block" -> block(id, jo);
            case "solid" -> solid(name, id, jo);
            case "coil" -> coil(name, id, jo);
            default -> throw new UnsupportedTypeException("type", type);
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var type = GsonHelper.getAsString(jo, "type");
        if (!jo.has("items")) {
            var id = loc.getPath();
            var name = LocHelper.name(id, -1);
            buildItem(type, name, id, jo);
        } else {
            var prefix = loc.getPath();
            var je = jo.get("items");
            if (je.isJsonArray()) {
                for (var entry : je.getAsJsonArray()) {
                    var name = GsonHelper.convertToString(entry, "items");
                    buildItem(type, name, prefix + "/" + name, EMPTY);
                }
            } else if (je.isJsonObject()) {
                for (var entry : je.getAsJsonObject().entrySet()) {
                    var name = entry.getKey();
                    var jo1 = GsonHelper.convertToJsonObject(entry.getValue(), "items");
                    buildItem(type, name, prefix + "/" + name, jo1);
                }
            } else {
                throw new JsonSyntaxException("Missing items, except JsonArray or JsonObject");
            }
        }
    }
}
