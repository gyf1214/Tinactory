package org.shsts.tinactory.gametest;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StructureHelper {
    public static final StructureTemplate EMPTY = createEmpty();

    private static StructureTemplate createEmpty() {
        var tag = new CompoundTag();

        var size = new ListTag();
        size.add(IntTag.valueOf(1));
        size.add(IntTag.valueOf(1));
        size.add(IntTag.valueOf(1));
        tag.put("size", size);

        var palette = new ListTag();
        var airBlock = new CompoundTag();
        airBlock.putString("Name", "minecraft:air");
        palette.add(airBlock);
        tag.put("palette", palette);

        tag.put("blocks", new ListTag());
        tag.put("entities", new ListTag());

        var ret = new StructureTemplate();
        ret.load(tag);
        return ret;
    }
}
