package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.network.PrimitiveBlock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final IEntry<PrimitiveBlock> WORKBENCH;

    public static final Map<String, MachineSet> MACHINE_SETS;

    static {
        MACHINE_SETS = new HashMap<>();

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
