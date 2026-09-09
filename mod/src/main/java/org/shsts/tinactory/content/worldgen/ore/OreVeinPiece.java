package org.shsts.tinactory.content.worldgen.ore;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import org.shsts.tinactory.AllRegistries;
import org.shsts.tinactory.AllWorldGens;
import org.shsts.tinactory.core.worldgen.ore.IOreShape;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class OreVeinPiece extends StructurePiece {
    private static final String PAYLOAD_KEY = "ore_vein";
    private static Codec<OreVeinInstance> instanceCodec;
    private final OreVeinInstance instance;

    public OreVeinPiece(OreVeinInstance instance) {
        super(AllWorldGens.ORE_VEIN_PIECE_TYPE.get(), 0, OreVeinUtil.bounds(instance));
        this.instance = instance;
    }

    public OreVeinPiece(CompoundTag tag) {
        super(AllWorldGens.ORE_VEIN_PIECE_TYPE.get(), tag);
        this.instance = decodeInstance(tag.getCompound(PAYLOAD_KEY));
        this.boundingBox = OreVeinUtil.bounds(instance);
    }

    public OreVeinInstance instance() {
        return instance;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.put(PAYLOAD_KEY, instanceCodec().encodeStart(NbtOps.INSTANCE, instance).getOrThrow());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
        RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos pos) {
        if (!boundingBox.intersects(box)) {
            return;
        }
        var minX = Math.max(boundingBox.minX(), box.minX());
        var minY = Math.max(boundingBox.minY(), box.minY());
        var minZ = Math.max(boundingBox.minZ(), box.minZ());
        var maxX = Math.min(boundingBox.maxX(), box.maxX());
        var maxY = Math.min(boundingBox.maxY(), box.maxY());
        var maxZ = Math.min(boundingBox.maxZ(), box.maxZ());
        for (var x = minX; x <= maxX; x++) {
            for (var y = minY; y <= maxY; y++) {
                for (var z = minZ; z <= maxZ; z++) {
                    var position = new BlockPos(x, y, z);
                    if (level.getBlockState(position).is(instance.hostBlock())) {
                        OreVeinUtil.oreAt(instance, position)
                            .ifPresent(ore -> level.setBlock(position, ore.defaultBlockState(), 2));
                    }
                }
            }
        }
    }

    private static Codec<OreVeinInstance> instanceCodec() {
        if (instanceCodec == null) {
            Codec<IOreShape<?, ?>> shapeCodec = AllRegistries.ORE_SHAPES.get().byNameCodec();
            instanceCodec = OreVeinInstance.codec(
                BuiltInRegistries.BLOCK.byNameCodec(), OreVeinUtil.instanceCodec(shapeCodec)).codec();
        }
        return instanceCodec;
    }

    private static OreVeinInstance decodeInstance(CompoundTag tag) {
        return instanceCodec().parse(NbtOps.INSTANCE, tag)
            .getOrThrow(error -> new IllegalArgumentException("Invalid ore vein payload: " + error));
    }
}
