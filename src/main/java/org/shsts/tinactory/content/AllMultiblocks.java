package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.shsts.tinactory.content.material.MiscMeta;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.HalfBlock;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.content.multiblock.TurbineBlock;
import org.shsts.tinactory.content.network.FixedBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllRegistries.BLOCKS;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_CONNECTOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_WALL;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiblocks {
    public static final Map<String, MultiblockSet> MULTIBLOCK_SETS;
    public static final IEntry<FixedBlock> CLEANROOM;

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
            .register();

        var plascrete = BLOCKS.getEntry("multiblock/misc/plascrete");
        var cleanroomCeiling = BLOCKS.getEntry("multiblock/misc/filter_casing");
        CLEANROOM = BlockEntityBuilder.builder("multiblock/cleanroom", FixedBlock::new)
            .blockEntity()
            .child(Multiblock.builder(Cleanroom::new))
            .appearanceBlock(plascrete)
            .spec(Cleanroom::spec)
            .baseBlock(plascrete)
            .ceilingBlock(cleanroomCeiling)
            .wallTag(CLEANROOM_WALL)
            .connectorTag(CLEANROOM_CONNECTOR)
            .doorTag(CLEANROOM_DOOR)
            .maxSize(7)
            .maxConnector(1)
            .maxDoor(2)
            .build()
            .build()
            .end()
            .block()
            .material(Material.HEAVY_METAL)
            .properties(MACHINE_PROPERTY)
            .translucent()
            .end()
            .buildObject();
    }

    public static MultiblockSet getMultiblock(String name) {
        return MULTIBLOCK_SETS.get(name);
    }

    public static void init() {}
}
