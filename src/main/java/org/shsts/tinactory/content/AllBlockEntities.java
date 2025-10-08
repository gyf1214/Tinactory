package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final IEntry<MachineBlock> NETWORK_CONTROLLER;
    public static final IEntry<PrimitiveBlock> WORKBENCH;

    public static final Map<String, MachineSet> MACHINE_SETS;

    static {
        MACHINE_SETS = new HashMap<>();

        NETWORK_CONTROLLER = BlockEntityBuilder.builder("network/controller", MachineBlock::simple)
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

        WORKBENCH = BlockEntityBuilder.builder("primitive/workbench", PrimitiveBlock::new)
            .menu(AllMenus.WORKBENCH)
            .blockEntity()
            .transform(Workbench::factory)
            .end()
            .block()
            .material(Material.WOOD)
            .properties($ -> $.strength(2f).sound(SoundType.WOOD))
            .end()
            .buildObject();
    }

    public static void init() {}

    public static MachineSet getMachine(String name) {
        return MACHINE_SETS.get(name);
    }
}
