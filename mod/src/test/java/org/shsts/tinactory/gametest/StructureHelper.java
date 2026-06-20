package org.shsts.tinactory.gametest;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class StructureHelper {
    private static final Pattern EMPTY_TEMPLATE_PATTERN = Pattern.compile("empty_(\\d+)x(\\d+)x(\\d+)");
    private static final Map<String, StructureTemplate> EMPTY_TEMPLATES = new HashMap<>();
    public static final StructureTemplate EMPTY = createEmpty(5, 5, 5);

    private StructureHelper() {}

    public static StructureTemplate emptyFor(String structureName) {
        var path = new ResourceLocation(structureName).getPath();
        var lastDot = path.lastIndexOf('.');
        var lastSlash = path.lastIndexOf('/');
        var name = path.substring(Math.max(lastDot, lastSlash) + 1);
        var matcher = EMPTY_TEMPLATE_PATTERN.matcher(name);
        if (!matcher.matches()) {
            return EMPTY;
        }
        var x = Integer.parseInt(matcher.group(1));
        var y = Integer.parseInt(matcher.group(2));
        var z = Integer.parseInt(matcher.group(3));
        if (x <= 0 || y <= 0 || z <= 0) {
            throw new IllegalArgumentException("Empty GameTest template dimensions must be positive: " + structureName);
        }
        var key = x + "x" + y + "x" + z;
        return EMPTY_TEMPLATES.computeIfAbsent(key, ignored -> createEmpty(x, y, z));
    }

    private static StructureTemplate createEmpty(int x, int y, int z) {
        var tag = new CompoundTag();

        var size = new ListTag();
        size.add(IntTag.valueOf(x));
        size.add(IntTag.valueOf(y));
        size.add(IntTag.valueOf(z));
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
