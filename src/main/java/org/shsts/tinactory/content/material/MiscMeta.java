package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.content.AllItems;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.logistics.MEDrive;
import org.shsts.tinactory.content.logistics.MEStorageCell;
import org.shsts.tinactory.content.logistics.MEStorageCellSet;
import org.shsts.tinactory.content.logistics.MEStorageInterface;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.MachineMeta;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.SignalController;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.LensBlock;
import org.shsts.tinactory.content.multiblock.PowerBlock;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllItems.STORAGE_CELLS;
import static org.shsts.tinactory.content.AllMaterials.getMaterial;
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

    public MiscMeta() {
        super("Misc");
    }

    private static MaterialColor parseMaterialColor(JsonObject jo, String field) {
        // TODO: use string instead of integer
        return MaterialColor.byId(GsonHelper.getAsInt(jo, field));
    }

    private IEntry<Block> casing(String id, JsonObject jo) {
        var materialColor = parseMaterialColor(jo, "materialColor");
        return REGISTRATE.block(id, Block::new)
            .material(Material.HEAVY_METAL, materialColor)
            .properties(CASING_PROPERTY)
            .register();
    }

    private void solid(String name, String id, JsonObject jo) {
        SOLID_CASINGS.put(name, casing(id, jo));
    }

    private void coil(String name, String id, JsonObject jo) {
        var temperature = GsonHelper.getAsInt(jo, "temperature");
        var materialColor = parseMaterialColor(jo, "materialColor");
        var block = REGISTRATE.block(id, CoilBlock.factory(temperature))
            .material(Material.HEAVY_METAL, materialColor)
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.put(name, block);
    }

    private static <U extends Block, P> IBlockBuilder<U, P> glass(IBlockBuilder<U, P> builder) {
        return builder.material(Material.BARRIER)
            .properties(MiscMeta.CASING_PROPERTY)
            .properties($ -> $.isViewBlocking(AllItems::never)
                .noOcclusion()
                .sound(SoundType.GLASS))
            .translucent();
    }

    private void glass(String id) {
        REGISTRATE.block(id, GlassBlock::new)
            .transform(MiscMeta::glass)
            .register();
    }

    private void lens(String id, JsonObject jo) {
        var ja = GsonHelper.getAsJsonArray(jo, "materials");
        var lens = new ArrayList<Supplier<? extends Item>>();
        for (var je : ja) {
            var mat = GsonHelper.convertToString(je, "materials");
            var entry = getMaterial(mat).entry("lens");
            lens.add(entry);
        }

        REGISTRATE.block(id, LensBlock.factory(lens))
            .transform(MiscMeta::glass)
            .register();
    }

    private void power(String name, String id, JsonObject jo) {
        var voltage = Voltage.fromName(GsonHelper.getAsString(jo, "voltage", name));
        var capacity = GsonHelper.getAsLong(jo, "capacity");
        var materialColor = parseMaterialColor(jo, "materialColor");
        REGISTRATE.block(id, PowerBlock.factory(voltage, capacity))
            .material(Material.HEAVY_METAL, materialColor)
            .properties(CASING_PROPERTY)
            .register();
    }

    private void item(String id, JsonObject jo) {
        var builder = REGISTRATE.item(id);
        if (jo.has("tint")) {
            builder.tint(MaterialMeta.getColor(jo, "tint"));
        }
        builder.register();
    }

    private void meStorageInterface(String id, JsonObject jo) {
        BlockEntityBuilder.builder(id, MachineBlock::simple)
            .transform(MachineSet::baseMachine)
            .menu(AllMenus.ME_STORAGE_INTERFACE)
            .blockEntity()
            .transform(MEStorageInterface.factory(GsonHelper.getAsDouble(jo, "power")))
            .end()
            .build();
    }

    private void meDrive(String id, JsonObject jo) {
        var jo1 = GsonHelper.getAsJsonObject(jo, "layout");
        var layout = MachineMeta.parseLayout(jo1).buildLayout();
        BlockEntityBuilder.builder(id, MachineBlock::simple)
            .transform(MachineSet::baseMachine)
            .menu(AllMenus.ME_DRIVE)
            .blockEntity()
            .transform(MEDrive.factory(layout, GsonHelper.getAsDouble(jo, "power")))
            .end()
            .build();
    }

    private void meStorageCell(String name, String id, JsonObject jo) {
        var parent = LocHelper.name(id, -2);
        var prefix = id.substring(0, id.length() - parent.length() - name.length() - 1);
        var componentPrefix = GsonHelper.getAsString(jo, "componentPrefix");
        var bytes = GsonHelper.getAsInt(jo, "bytes");

        var component = REGISTRATE.item(componentPrefix + "/" + name).register();
        var item = REGISTRATE.item(prefix + "item_" + parent + "/" + name,
            MEStorageCell.itemCell(bytes)).register();
        var fluid = REGISTRATE.item(prefix + "fluid_" + parent + "/" + name,
            MEStorageCell.fluidCell(bytes)).register();

        STORAGE_CELLS.add(new MEStorageCellSet(component, item, fluid));
    }

    private void boiler(String id, JsonObject jo) {
        var mat = getMaterial(GsonHelper.getAsString(jo, "material", "water"));
        var jo1 = GsonHelper.getAsJsonObject(jo, "layout");
        var layout = MachineMeta.parseLayout(jo1).buildLayout();
        var speed = GsonHelper.getAsDouble(jo, "burnSpeed");
        BlockEntityBuilder.builder(id, MachineBlock::simple)
            .transform(MachineSet::baseMachine)
            .menu(AllMenus.BOILER)
            .blockEntity()
            .transform(Boiler.factory(speed, mat.fluid("liquid"), mat.fluid("gas")))
            .transform(StackProcessingContainer.factory(layout))
            .end()
            .build();
    }

    private void meSignalController(String id, JsonObject jo) {
        BlockEntityBuilder.builder(id, MachineBlock::signal)
            .transform(MachineSet::baseMachine)
            .menu(AllMenus.SIGNAL_CONTROLLER)
            .blockEntity()
            .transform(SignalController.factory(GsonHelper.getAsDouble(jo, "power")))
            .end()
            .build();
    }

    private void buildItem(String type, String name, String id, JsonObject jo) {
        switch (type) {
            case "casing" -> casing(id, jo);
            case "solid" -> solid(name, id, jo);
            case "coil" -> coil(name, id, jo);
            case "glass" -> glass(id);
            case "lens" -> lens(id, jo);
            case "power" -> power(name, id, jo);
            case "item" -> item(id, jo);
            case "me_storage_interface" -> meStorageInterface(id, jo);
            case "me_drive" -> meDrive(id, jo);
            case "me_storage_cell" -> meStorageCell(name, id, jo);
            case "me_signal_controller" -> meSignalController(id, jo);
            case "boiler" -> boiler(id, jo);
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
                    buildItem(type, name, prefix + "/" + name, jo);
                }
            } else if (je.isJsonObject()) {
                for (var entry : je.getAsJsonObject().entrySet()) {
                    // add base settings
                    var jo2 = new JsonObject();
                    for (var entry1 : jo.entrySet()) {
                        if (!entry1.getKey().equals("type") && !entry1.getKey().equals("items")) {
                            jo2.add(entry1.getKey(), entry1.getValue().deepCopy());
                        }
                    }

                    // add specific setting
                    var name = entry.getKey();
                    var jo1 = GsonHelper.convertToJsonObject(entry.getValue(), "items");
                    for (var entry1 : jo1.entrySet()) {
                        jo2.add(entry1.getKey(), entry1.getValue().deepCopy());
                    }

                    buildItem(type, name, prefix + "/" + name, jo2);
                }
            } else {
                throw new JsonSyntaxException("Missing items, except JsonArray or JsonObject");
            }
        }
    }
}
