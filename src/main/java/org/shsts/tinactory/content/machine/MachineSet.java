package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet {
    public final Set<Voltage> voltages;
    protected final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, ? extends IEntry<? extends Block>> machines;

    public MachineSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
        Map<Voltage, ? extends IEntry<? extends Block>> machines) {
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.voltages = new HashSet<>(voltages);
    }

    public MachineSet(Map<Voltage, Layout> layoutSet,
        Map<Voltage, ? extends IEntry<? extends Block>> machines) {
        this(machines.keySet(), layoutSet, machines);
    }

    public boolean hasVoltage(Voltage voltage) {
        return machines.containsKey(voltage);
    }

    public IEntry<? extends Block> entry(Voltage voltage) {
        return machines.get(voltage);
    }

    public Collection<? extends IEntry<? extends Block>> entries() {
        return machines.values();
    }

    public Block block(Voltage voltage) {
        return machines.get(voltage).get();
    }

    public Layout layout(Voltage voltage) {
        return layoutSet.get(voltage);
    }

    public static <U extends SmartEntityBlock, P> BlockEntityBuilder<U, P> baseMachine(
        BlockEntityBuilder<U, P> builder) {
        return builder.blockEntity()
            .transform(Machine::factory)
            .end()
            .block()
            .material(Material.HEAVY_METAL)
            .properties(MACHINE_PROPERTY)
            .translucent()
            .end();
    }
}
