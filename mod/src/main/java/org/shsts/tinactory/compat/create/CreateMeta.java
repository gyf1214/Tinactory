package org.shsts.tinactory.compat.create;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.content.machine.MachineMeta;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.material.ComponentMeta;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.integration.builder.BlockEntityBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateMeta extends MetaConsumer {
    public CreateMeta() {
        super("Create");
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var id = loc.getPath();
        var type = GsonHelper.getAsString(jo, "type");
        var machines = new HashMap<Voltage, IEntry<? extends Block>>();
        for (var entry : ComponentMeta.parseVoltageConfig(jo, "voltages")) {
            var voltage = entry.voltage();
            var config = entry.jo();
            var stress = GsonHelper.getAsFloat(config, "stress");
            var rpm = GsonHelper.getAsInt(config, "rpm");
            var power = GsonHelper.getAsDouble(config, "power");
            var machine = switch (type) {
                case "electric_motor" -> buildMotor(id, voltage, stress, rpm, power);
                case "kinetic_generator" -> buildGenerator(id, voltage, stress, rpm, power);
                default -> throw new JsonSyntaxException("Unknown Create machine type: " + type);
            };
            machines.put(voltage, machine);
        }

        var set = new MachineSet(Layout.EMPTY_SET, machines);
        var setId = "create/" + id;
        AllBlockEntities.MACHINE_SETS.put(setId, set);
    }

    private static IEntry<? extends Block> buildMotor(String id, Voltage voltage,
        float stress, int rpm, double power) {
        var machineId = "create/" + voltage.id + "/" + id;
        return BlockEntityBuilder.builder(machineId, CreateMachineBlock.factory(voltage))
            .blockEntity()
            .transform(CreateElectricMotorBlockEntity.factory(stress, rpm, power))
            .end()
            .block()
            .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .properties(MachineMeta.MACHINE_PROPERTY)
            .end()
            .buildObject();
    }

    private static IEntry<? extends Block> buildGenerator(String id, Voltage voltage,
        float stress, int rpm, double power) {
        var machineId = "create/" + voltage.id + "/" + id;
        return BlockEntityBuilder.builder(machineId, CreateMachineBlock.factory(voltage))
            .blockEntity()
            .transform(CreateKineticGeneratorBlockEntity.factory(stress, rpm, power))
            .end()
            .block()
            .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .properties(MachineMeta.MACHINE_PROPERTY)
            .end()
            .buildObject();
    }
}
