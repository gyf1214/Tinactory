package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.content.material.MiscMeta;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.HalfBlock;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.content.multiblock.TurbineBlock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiblocks {
    public static final Map<String, MultiblockSet> MULTIBLOCK_SETS;

    public static final Map<String, IEntry<Block>> SOLID_CASINGS;
    public static final Map<String, IEntry<CoilBlock>> COIL_BLOCKS;

    static {
        SOLID_CASINGS = new HashMap<>();
        COIL_BLOCKS = new HashMap<>();
        MULTIBLOCK_SETS = new HashMap<>();

        // misc
        REGISTRATE.block("multiblock/misc/autofarm_base", Block::new)
            .material(Material.HEAVY_METAL, MaterialColor.DIRT)
            .properties(MiscMeta.CASING_PROPERTY)
            .properties($ -> $.sound(SoundType.GRAVEL))
            .register();

        REGISTRATE.block("multiblock/misc/launch_site_base", HalfBlock::new)
            .material(Material.HEAVY_METAL)
            .properties(MiscMeta.CASING_PROPERTY)
            .register();

        REGISTRATE.block("multiblock/misc/turbine_blade", TurbineBlock::new)
            .material(Material.HEAVY_METAL, MaterialColor.COLOR_MAGENTA)
            .properties(MiscMeta.CASING_PROPERTY)
            .translucent()
            .register();
    }

    public static MultiblockSet getMultiblock(String name) {
        return MULTIBLOCK_SETS.get(name);
    }

    public static void init() {}
}
