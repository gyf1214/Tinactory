package org.shsts.tinactory.datagen.extra;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.shsts.tinactory.content.machine.UnsupportedTypeException;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.slf4j.Logger;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.AllRegistries.BLOCKS;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.datagen.content.AllData.initDelayed;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LitematicaMeta extends MetaConsumer {
    private static final Logger LOGGER = LogUtils.getLogger();

    public LitematicaMeta() {
        super("Litematica");
    }

    private static class Executor implements Runnable {
        private final ResourceLocation loc;
        private final JsonObject jo;
        private final Set<Character> interfaceCh = new HashSet<>();

        private Executor(ResourceLocation loc, JsonObject jo) {
            this.loc = loc;
            this.jo = jo;
        }

        private BlockState parseDefine(String str) {
            var block = BLOCKS.getEntry(new ResourceLocation(str));
            return block.get().defaultBlockState();
        }

        private static final Map<ResourceLocation, ResourceLocation> TAG_MAP = Map.of(
            new ResourceLocation("forge", "glass"), mcLoc("glass"),
            modLoc("multiblock/coil"), modLoc("multiblock/coil/cupronickel"),
            modLoc("multiblock/power"), modLoc("multiblock/misc/power_block/hv"),
            modLoc("multiblock/glass_casing"), modLoc("multiblock/misc/hardened_glass"),
            modLoc("multiblock/lithography_lens"), modLoc("multiblock/misc/lithography_lens/good"));

        private BlockState parseDefine(char ch, JsonElement je) {
            if (je.isJsonPrimitive()) {
                var blockStr = GsonHelper.convertToString(je, "defines");
                if (blockStr.equals("air")) {
                    return Blocks.AIR.defaultBlockState();
                }
                return parseDefine(blockStr);
            }

            var jo = GsonHelper.convertToJsonObject(je, "defines");
            var type = GsonHelper.getAsString(jo, "type");
            switch (type) {
                case "block_or_interface" -> {
                    interfaceCh.add(ch);
                    return parseDefine(GsonHelper.getAsString(jo, "block"));
                }
                case "tag", "tag_with_same_block", "tag_or_block" -> {
                    var tag = new ResourceLocation(GsonHelper.getAsString(jo, "tag"));
                    assert TAG_MAP.containsKey(tag) : tag;
                    return BLOCKS.getEntry(TAG_MAP.get(tag)).get().defaultBlockState();
                }
            }
            throw new UnsupportedTypeException("defines", type);
        }

        private <T extends Comparable<T>> String getValueName(BlockState blockState, Property<T> prop) {
            return prop.getName(blockState.getValue(prop));
        }

        private CompoundTag serializeBlockState(BlockState blockState) {
            var ret = new CompoundTag();
            var loc = blockState.getBlock().getRegistryName();
            assert loc != null;
            ret.putString("Name", loc.toString());
            if (blockState.getProperties().isEmpty()) {
                return ret;
            }
            var props = new CompoundTag();
            for (var prop : blockState.getProperties()) {
                props.putString(prop.getName(), getValueName(blockState, prop));
            }
            ret.put("Properties", props);
            return ret;
        }

        private LongArrayTag packBits(List<Integer> raw, int size) {
            var bits = 0;
            size -= 1;
            while (size > 0) {
                bits++;
                size /= 2;
            }
            bits = Math.max(2, bits);

            var ret = new ArrayList<Long>();
            var cur = 0L;
            var curBits = 0;
            for (var x : raw) {
                assert x >= 0;
                var y = (long) x;
                if (curBits + bits >= 64) {
                    var cb = 64 - curBits;
                    assert cb > 0;
                    ret.add(cur | ((y & ((1L << cb) - 1L)) << curBits));
                    cur = (x >> cb);
                    curBits = bits - cb;
                } else {
                    cur |= (y << curBits);
                    curBits += bits;
                }
            }
            if (curBits > 0) {
                ret.add(cur);
            }
            return new LongArrayTag(ret);
        }

        private void putXYZ(CompoundTag tag, String name, int x, int y, int z) {
            var xyz = new CompoundTag();
            xyz.putInt("x", x);
            xyz.putInt("y", y);
            xyz.putInt("z", z);
            tag.put(name, xyz);
        }

        private CompoundTag getMeta(String id, int w, int d, int h, List<Integer> blockArr) {
            var ret = new CompoundTag();
            var time = System.currentTimeMillis();
            ret.putString("Name", id);
            ret.putString("Description", "");
            ret.putString("Author", "Tinactory");
            ret.putLong("TimeCreated", time);
            ret.putLong("TimeModified", time);
            ret.putInt("TotalVolume", w * d * h);
            ret.putInt("TotalBlocks", (int) blockArr.stream().filter($ -> $ != 0).count());
            ret.putInt("RegionCount", 1);
            putXYZ(ret, "EnclosingSize", w, h, d);
            return ret;
        }

        private CompoundTag getRegion(int w, int d, int h, List<Integer> blockArr, ListTag palette) {
            var ret = new CompoundTag();
            putXYZ(ret, "Position", 0, 0, 0);
            putXYZ(ret, "Size", w, h, -d);
            ret.put("BlockStates", packBits(blockArr, palette.size()));
            ret.put("BlockStatePalette", palette);
            ret.put("TileEntities", new ListTag());
            ret.put("Entities", new ListTag());
            ret.put("PendingBlockTicks", new ListTag());
            ret.put("PendingFluidTicks", new ListTag());
            return ret;
        }

        private CompoundTag getTag() {
            var layers = GsonHelper.getAsJsonArray(jo, "layers");
            var defines = GsonHelper.getAsJsonObject(jo, "defines");

            var palette = new ListTag();
            var defineToPalette = new HashMap<Character, Integer>();
            var stateToPalette = new HashMap<BlockState, Integer>();

            // air = 0
            var air = Blocks.AIR.defaultBlockState();
            palette.add(serializeBlockState(air));
            stateToPalette.put(air, 0);
            defineToPalette.put(' ', 0);

            // main block = 1
            var mainBlock = BLOCKS.getEntry(LocHelper.prepend(loc, "multiblock")).get().defaultBlockState();
            var mainBlock1 = mainBlock.hasProperty(PrimitiveBlock.FACING) ?
                mainBlock.setValue(PrimitiveBlock.FACING, Direction.SOUTH) : mainBlock;
            palette.add(serializeBlockState(mainBlock1));
            stateToPalette.put(mainBlock1, 1);
            defineToPalette.put('$', 1);

            // interface = 2
            var interfaceBlock = BLOCKS.getEntry(modLoc("multiblock/lv/interface")).get().defaultBlockState();
            palette.add(serializeBlockState(interfaceBlock));
            stateToPalette.put(interfaceBlock, 2);

            interfaceCh.clear();
            var hasInterface = false;
            for (var entry : defines.entrySet()) {
                var ch = entry.getKey().charAt(0);
                var blockState = parseDefine(ch, entry.getValue());
                if (blockState.isAir()) {
                    defineToPalette.put(ch, 0);
                } else if (stateToPalette.containsKey(blockState)) {
                    defineToPalette.put(ch, stateToPalette.get(blockState));
                } else {
                    var k = palette.size();
                    palette.add(serializeBlockState(blockState));
                    stateToPalette.put(blockState, k);
                    defineToPalette.put(ch, k);
                }
            }

            assert !interfaceCh.isEmpty();

            var blocksArr = new ArrayList<Integer>();
            int w = -1;
            int d = -1;
            int h = 0;
            for (var je : layers) {
                int layerHeights;
                JsonArray ja;
                if (je.isJsonArray()) {
                    layerHeights = 1;
                    ja = GsonHelper.convertToJsonArray(je, "layers");
                } else {
                    var jo1 = GsonHelper.convertToJsonObject(je, "layers");
                    var minHeight = Integer.parseInt(GsonHelper.getAsString(jo1, "height").split("-")[0]);
                    layerHeights = Math.max(1, minHeight);
                    ja = GsonHelper.getAsJsonArray(jo1, "rows");
                }

                if (d == -1) {
                    d = ja.size();
                }
                assert d == ja.size();

                for (var y = 0; y < layerHeights; y++) {
                    for (var je1 : ja) {
                        var s = GsonHelper.convertToString(je1, "rows");
                        if (w == -1) {
                            w = s.length();
                        }
                        assert w == s.length();

                        for (var x = 0; x < w; x++) {
                            var ch = s.charAt(x);
                            if (!hasInterface && interfaceCh.contains(ch)) {
                                hasInterface = true;
                                blocksArr.add(2);
                                continue;
                            }
                            assert defineToPalette.containsKey(ch) : ch;
                            blocksArr.add(defineToPalette.get(ch));
                        }
                    }
                }

                h += layerHeights;
            }

            assert hasInterface;
            assert d > 0 && h > 0;

            var id = loc.getPath();

            var ret = new CompoundTag();
            ret.putInt("MinecraftDataVersion", 2975);
            ret.putInt("Version", 6);
            ret.putInt("SubVersion", 1);
            var meta = getMeta(id, w, d, h, blocksArr);
            ret.put("Metadata", meta);
            var region = getRegion(w, d, h, blocksArr, palette);
            var regions = new CompoundTag();
            regions.put(id, region);
            ret.put("Regions", regions);

            return ret;
        }

        private void unsafeRun() throws Exception {
            var type = GsonHelper.getAsString(jo, "machine", "default");
            if (type.equals("cleanroom")) {
                // TODO
                LOGGER.warn("skip cleanroom {}", loc);
                return;
            }
            var tag = getTag();

            var dir = Path.of("schematics");
            if (!Files.isDirectory(dir)) {
                Files.createDirectories(dir);
            }

            try (var fos = new FileOutputStream("schematics/" + loc.getPath() + ".litematic")) {
                NbtIo.writeCompressed(tag, fos);
            }
        }

        @Override
        public void run() {
            try {
                unsafeRun();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        initDelayed(new Executor(loc, jo));
    }
}
