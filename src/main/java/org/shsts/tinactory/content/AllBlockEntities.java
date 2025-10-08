package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.core.multiblock.client.MultiblockInterfaceRenderer;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final MachineSet BATTERY_BOX;
    public static final MachineSet ELECTRIC_CHEST;
    public static final MachineSet ELECTRIC_TANK;
    public static final MachineSet LOGISTIC_WORKER;
    public static final Map<Voltage, IEntry<MachineBlock>> MULTIBLOCK_INTERFACE;

    public static final IEntry<MachineBlock> NETWORK_CONTROLLER;
    public static final IEntry<PrimitiveBlock> WORKBENCH;

    public static final Map<String, MachineSet> MACHINE_SETS;

    static {
        MACHINE_SETS = new HashMap<>();

        var set = new SetFactory();

        MULTIBLOCK_INTERFACE = ComponentBuilder
            .simple(v -> BlockEntityBuilder.builder("multiblock/" + v.id + "/interface",
                    MachineBlock.multiblockInterface(v))
                .menu(AllMenus.PROCESSING_MACHINE)
                .blockEntity()
                .transform(MultiblockInterface::factory)
                .transform(FlexibleStackContainer::factory)
                .renderer(() -> () -> MultiblockInterfaceRenderer::new)
                .end()
                .block()
                .material(Material.HEAVY_METAL)
                .properties(MACHINE_PROPERTY)
                .tint(() -> () -> (state, $2, $3, i) ->
                    MultiblockInterfaceBlock.tint(v, state, i))
                .translucent()
                .end()
                .buildObject())
            .voltages(Voltage.ULV, Voltage.LuV)
            .buildObject();

        BATTERY_BOX = set.machine("battery_box")
            .machine(v -> "machine/" + v.id + "/battery_box", MachineBlock::sided)
            .menu(AllMenus.SIMPLE_MACHINE)
            .layoutMachine(BatteryBox::factory)
            .voltages(Voltage.LV, Voltage.HV)
            .layoutSet()
            .port(ITEM_INPUT)
            .transform($ -> {
                for (var i = 0; i < 4; i++) {
                    for (var j = 0; j < 4; j++) {
                        $.slot(j * SLOT_SIZE, i * SLOT_SIZE, Voltage.fromRank(1 + Math.max(i, j)));
                    }
                }
                return $;
            }).build()
            .tintVoltage(0)
            .buildObject();

        ELECTRIC_CHEST = set.machine("electric_chest")
            .machine(v -> "machine/" + v.id + "/electric_chest", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_CHEST)
            .layoutMachine(ElectricChest::factory)
            .voltages(Voltage.ULV, Voltage.HV)
            .layoutSet()
            .port(SlotType.NONE)
            .transform($ -> {
                for (var i = 0; i < 2; i++) {
                    for (var j = 0; j < 8; j++) {
                        var voltage = Voltage.fromValue(8 * (j + 1) * (j + 1));
                        $.slot(j * (SLOT_SIZE + 2), 1 + i * 2 * (SLOT_SIZE + MARGIN_VERTICAL), voltage);
                    }
                }
                return $;
            }).build()
            .tintVoltage(2)
            .buildObject();

        ELECTRIC_TANK = set.machine("electric_tank")
            .machine(v -> "machine/" + v.id + "/electric_tank", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_TANK)
            .layoutMachine(ElectricTank::factory)
            .voltages(Voltage.ULV, Voltage.HV)
            .layoutSet()
            .port(FLUID_INPUT)
            .transform($ -> {
                for (var i = 0; i < 8; i++) {
                    var voltage = Voltage.fromValue(8 * (i + 1) * (i + 1));
                    $.slot(i * (SLOT_SIZE + 2), 1, voltage);
                }
                return $;
            }).build()
            .tintVoltage(2)
            .buildObject();

        LOGISTIC_WORKER = set.machine("logistic_worker")
            .machine(v -> "logistics/" + v.id + "/logistic_worker", MachineBlock::factory)
            .menu(AllMenus.LOGISTIC_WORKER)
            .machine(LogisticWorker::factory)
            .voltages(Voltage.ULV)
            .tintVoltage(2)
            .buildObject();

        NETWORK_CONTROLLER = set.blockEntity("network/controller", MachineBlock::simple)
            .menu(AllMenus.NETWORK_CONTROLLER)
            .blockEntity()
            .transform(NetworkController::factory)
            .end()
            .block()
            .material(Material.HEAVY_METAL)
            .properties(MACHINE_PROPERTY)
            .translucent()
            .end()
            .buildObject();

        WORKBENCH = set.blockEntity("primitive/workbench",
                PrimitiveBlock::new)
            .menu(AllMenus.WORKBENCH)
            .blockEntity()
            .transform(Workbench::factory)
            .end()
            .block()
            .material(Material.WOOD)
            .properties($ -> $.strength(2f).sound(SoundType.WOOD))
            .end()
            .buildObject();

        MACHINE_SETS.put("multiblock_interface", new MachineSet(Layout.EMPTY_SET, MULTIBLOCK_INTERFACE));
    }

    public static void init() {}

    private static class SetFactory {
        public <U extends SmartEntityBlock> BlockEntityBuilder<U, SetFactory> blockEntity(
            String id, SmartEntityBlock.Factory<U> factory) {
            return BlockEntityBuilder.builder(this, id, factory);
        }

        public MachineSet.Builder<SetFactory> machine(String id) {
            return MachineSet.builder(this)
                .onCreateObject($ -> MACHINE_SETS.put(id, $));
        }
    }

    public static MachineSet getMachine(String name) {
        return MACHINE_SETS.get(name);
    }
}
