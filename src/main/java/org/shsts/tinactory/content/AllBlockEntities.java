package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.machine.Boiler;
import org.shsts.tinactory.content.machine.ElectricChest;
import org.shsts.tinactory.content.machine.ElectricTank;
import org.shsts.tinactory.content.machine.MEDrive;
import org.shsts.tinactory.content.machine.MEStorageInterface;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.material.ComponentBuilder;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.api.logistics.SlotType.FLUID_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_INPUT;
import static org.shsts.tinactory.api.logistics.SlotType.ITEM_OUTPUT;
import static org.shsts.tinactory.content.AllItems.COMPONENTS;
import static org.shsts.tinactory.content.AllMaterials.getMaterial;
import static org.shsts.tinactory.content.machine.MachineSet.baseMachine;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_VERTICAL;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.PROGRESS_ARROW;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final MachineSet ELECTRIC_FURNACE;
    public static final MachineSet BATTERY_BOX;
    public static final MachineSet ELECTRIC_CHEST;
    public static final MachineSet ELECTRIC_TANK;
    public static final MachineSet LOGISTIC_WORKER;
    public static final MachineSet ME_DRIVE;
    public static final MachineSet ME_STORAGE_INTERFACE;
    public static final Map<Voltage, IEntry<MachineBlock>> MULTIBLOCK_INTERFACE;

    public static final IEntry<MachineBlock> NETWORK_CONTROLLER;
    public static final IEntry<PrimitiveBlock> WORKBENCH;
    public static final IEntry<MachineBlock> LOW_PRESSURE_BOILER;
    public static final IEntry<MachineBlock> HIGH_PRESSURE_BOILER;

    public static final Map<String, MachineSet> MACHINE_SETS;
    public static final Set<ProcessingSet> PROCESSING_SETS;

    static {
        PROCESSING_SETS = new HashSet<>();
        MACHINE_SETS = new HashMap<>();

        var set = new SetFactory();

        ELECTRIC_FURNACE = set.machine("electric_furnace")
            .machine(v -> "machine/" + v.id + "/electric_furnace", MachineBlock::factory)
            .menu(AllMenus.ELECTRIC_FURNACE)
            .layoutMachine(StackProcessingContainer::factory)
            .machine(RecipeProcessor::electricFurnace)
            .tintVoltage(2)
            .voltages(Voltage.ULV)
            .transform(simpleLayout(PROGRESS_ARROW))
            .buildObject();

        LOW_PRESSURE_BOILER = boiler("low", 5d);
        HIGH_PRESSURE_BOILER = boiler("high", 17d);

        MULTIBLOCK_INTERFACE = ComponentBuilder
            .simple(ProcessingSet::multiblockInterface)
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

        ME_DRIVE = set.machine("me_drive")
            .machine(v -> "logistics/" + v.id + "/me_drive", MachineBlock::factory)
            .menu(AllMenus.SIMPLE_MACHINE)
            .layoutMachine(MEDrive::factory)
            .voltages(Voltage.HV)
            .layoutSet()
            .port(ITEM_INPUT)
            .slots(0, 0, 3, 3)
            .build()
            .tintVoltage(2)
            .buildObject();

        ME_STORAGE_INTERFACE = set.machine("me_storage_interface")
            .machine(v -> "logistics/" + v.id + "/me_storage_interface", MachineBlock::factory)
            .menu(AllMenus.ME_STORAGE_INTERFACE)
            .voltages(Voltage.HV)
            .machine(MEStorageInterface::factory)
            .tintVoltage(2)
            .buildObject();

        NETWORK_CONTROLLER = set.blockEntity("network/controller",
                MachineBlock.factory(Voltage.PRIMITIVE))
            .menu(AllMenus.NETWORK_CONTROLLER)
            .blockEntity()
            .transform(NetworkController::factory)
            .end()
            .translucent()
            .buildObject();

        WORKBENCH = set.blockEntity("primitive/workbench",
                PrimitiveBlock::new)
            .menu(AllMenus.WORKBENCH)
            .blockEntity()
            .transform(Workbench::factory)
            .end()
            .buildObject();

        // TODO: make it a MachineSet without any layout
        COMPONENTS.put("multiblock_interface", MULTIBLOCK_INTERFACE);
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

    private static IEntry<MachineBlock> boiler(String name, double burnSpeed) {
        var id = "machine/boiler/" + name;
        var layout = AllLayouts.BOILER;
        var water = getMaterial("water");
        return BlockEntityBuilder.builder(id,
                MachineBlock.factory(Voltage.PRIMITIVE))
            .menu(AllMenus.BOILER)
            .blockEntity()
            .transform(Boiler.factory(burnSpeed, water.fluid("liquid"), water.fluid("gas")))
            .transform(StackProcessingContainer.factory(layout))
            .end()
            .transform(baseMachine())
            .buildObject();
    }

    private static <S extends MachineSet.BuilderBase<?, ?,
        S>> Transformer<S> simpleLayout(Texture progressBar) {
        return $ -> $.layoutSet()
            .port(ITEM_INPUT)
            .slot(0, 1 + SLOT_SIZE / 2)
            .port(ITEM_OUTPUT)
            .slot(SLOT_SIZE * 3, 1 + SLOT_SIZE / 2)
            .progressBar(progressBar, 8 + SLOT_SIZE, SLOT_SIZE / 2)
            .build();
    }

    public static MachineSet getMachine(String name) {
        return MACHINE_SETS.get(name);
    }
}
