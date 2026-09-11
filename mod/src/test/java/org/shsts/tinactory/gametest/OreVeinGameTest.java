package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllWorldGens;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.worldgen.ore.OreVeinPiece;
import org.shsts.tinactory.content.worldgen.ore.OreVeinStructure;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.OreEntry;
import org.shsts.tinactory.core.worldgen.ore.OreShapeDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreShapeInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinDefinition;
import org.shsts.tinactory.core.worldgen.ore.OreVeinInstance;
import org.shsts.tinactory.core.worldgen.ore.OreVeinUtil;

import java.util.List;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@GameTestHolder(TinactoryKeys.ID)
public final class OreVeinGameTest {
    @GameTest
    public static void testOreVeinTypesAreRegistered(GameTestHelper helper) {
        var structureType = BuiltInRegistries.STRUCTURE_TYPE.get(modLoc("ore_vein"));
        var pieceType = BuiltInRegistries.STRUCTURE_PIECE.get(modLoc("ore_vein"));
        if (!(structureType instanceof StructureType<?> type) || type.codec() == null) {
            helper.fail("Ore vein structure type is not registered");
            return;
        }
        if (pieceType instanceof StructurePieceType.ContextlessType) {
            helper.fail("Ore vein piece type is registered without serialization context");
            return;
        }
        if (AllWorldGens.ORE_VEIN_STRUCTURE_TYPE.get() != type || AllWorldGens.ORE_VEIN_PIECE_TYPE.get() != pieceType) {
            helper.fail("Ore vein registry entries do not point at the registered values");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testOreVeinStructureCodecRoundTripsDefinitions(GameTestHelper helper) {
        var definition = new OreVeinDefinition(
            modLoc("test/ore"), 1, -32, 48,
            new OreShapeDefinition<>(AllWorldGens.ELLIPSOID_SHAPE.get(),
                new EllipsoidShape.Definition(100d, 200d, 0.6d, 1d, 3d)),
            0.75d, Blocks.STONE, List.of(new OreEntry(Blocks.IRON_ORE, 1)));
        var structure = new OreVeinStructure(
            new Structure.StructureSettings(HolderSet.direct(helper.getLevel().registryAccess()
                .registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS))),
            List.of(definition));
        var codec = AllWorldGens.ORE_VEIN_STRUCTURE_TYPE.get().codec();
        var encoded = CodecHelper.encodeTag(helper.getLevel().registryAccess(), codec.codec(), structure);
        var decoded = CodecHelper.parseTag(helper.getLevel().registryAccess(), codec.codec(), encoded);
        if (!structure.definitions().equals(decoded.definitions())) {
            helper.fail("Ore vein structure definitions did not round-trip");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testOreVeinPieceRoundTripsTheCompleteInstance(GameTestHelper helper) {
        var instance = sampledInstance();
        var piece = new OreVeinPiece(instance);
        var context = StructurePieceSerializationContext.fromLevel(helper.getLevel());
        var tag = piece.createTag(context);
        if (!"tinactory:ore_vein".equals(tag.getString("id")) || !tag.contains("ore_vein")) {
            helper.fail("Ore vein piece did not write its registered payload");
            return;
        }
        var loaded = loadPiece(context, tag);
        if (!instance.equals(loaded.instance())) {
            helper.fail("Ore vein piece did not preserve its complete instance");
            return;
        }
        if (!piece.getBoundingBox().equals(loaded.getBoundingBox())) {
            helper.fail("Reloaded ore vein piece changed its bounding box");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testOreVeinPieceRejectsAnUnsupportedAlgorithmVersion(GameTestHelper helper) {
        var tag = new OreVeinPiece(sampledInstance()).createTag(
            StructurePieceSerializationContext.fromLevel(helper.getLevel()));
        var payload = tag.getCompound("ore_vein");
        payload.putInt("algorithm_version", OreVeinUtil.ALGORITHM_VERSION + 1);
        try {
            loadPiece(StructurePieceSerializationContext.fromLevel(helper.getLevel()), tag);
            helper.fail("Ore vein piece accepted an unsupported algorithm version");
        } catch (IllegalArgumentException expected) {
            helper.succeed();
        }
    }

    @GameTest(template = "empty_8x5x8")
    public static void testOreVeinPiecePlacesOnlyWritableHostBlocks(GameTestHelper helper) {
        var center = helper.absolutePos(new BlockPos(3, 2, 3));
        var instance = placementInstance(center);
        var piece = new OreVeinPiece(instance);
        var bounds = piece.getBoundingBox();
        var left = findFilledPosition(instance, bounds, true);
        var right = findFilledPosition(instance, bounds, false);
        var nonHost = center;
        helper.getLevel().setBlock(left, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(right, Blocks.STONE.defaultBlockState(), 3);
        helper.getLevel().setBlock(nonHost, Blocks.DEEPSLATE.defaultBlockState(), 3);

        var leftBox = new BoundingBox(
            bounds.minX(), bounds.minY(), bounds.minZ(), center.getX(), bounds.maxY(), bounds.maxZ());
        var rightBox = new BoundingBox(center.getX() + 1, bounds.minY(), bounds.minZ(),
            bounds.maxX(), bounds.maxY(), bounds.maxZ());
        postProcess(helper, piece, leftBox, center);
        if (!Blocks.IRON_ORE.equals(helper.getLevel().getBlockState(left).getBlock())) {
            helper.fail("Ore vein did not place a filled host position in the writable box");
            return;
        }
        if (!Blocks.STONE.equals(helper.getLevel().getBlockState(right).getBlock())) {
            helper.fail("Ore vein placed a block outside the writable box");
            return;
        }
        if (!Blocks.DEEPSLATE.equals(helper.getLevel().getBlockState(nonHost).getBlock())) {
            helper.fail("Ore vein replaced a non-host block");
            return;
        }

        postProcess(helper, piece, rightBox, center);
        if (!Blocks.IRON_ORE.equals(helper.getLevel().getBlockState(right).getBlock())) {
            helper.fail("Ore vein did not place the remaining writable portion");
            return;
        }
        helper.succeed();
    }

    private static OreVeinPiece loadPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        return (OreVeinPiece) AllWorldGens.ORE_VEIN_PIECE_TYPE.get().load(context, tag);
    }

    private static OreVeinInstance sampledInstance() {
        var definition = new OreVeinDefinition(
            modLoc("test/ore"), 1, -32, 48,
            new OreShapeDefinition<>(AllWorldGens.ELLIPSOID_SHAPE.get(),
                new EllipsoidShape.Definition(100d, 200d, 0.6d, 1d, 3d)),
            0.75d, Blocks.STONE, List.of(new OreEntry(Blocks.IRON_ORE, 1), new OreEntry(Blocks.GOLD_ORE, 2)));
        return OreVeinUtil.sample(definition, 12345L, new BlockPos(10, 20, 30));
    }

    private static OreVeinInstance placementInstance(BlockPos center) {
        return new OreVeinInstance(
            OreVeinUtil.ALGORITHM_VERSION, modLoc("test/placement"), 67890L, center,
            new OreShapeInstance<>(AllWorldGens.ELLIPSOID_SHAPE.get(), new EllipsoidShape.Instance(3, 1, 3, 0)),
            1d, Blocks.STONE, List.of(new OreEntry(Blocks.IRON_ORE, 1)));
    }

    private static BlockPos findFilledPosition(OreVeinInstance instance, BoundingBox bounds, boolean left) {
        var centerX = instance.center().getX();
        for (var x = bounds.minX(); x <= bounds.maxX(); x++) {
            if ((left && x >= centerX) || (!left && x <= centerX)) {
                continue;
            }
            for (var y = bounds.minY(); y <= bounds.maxY(); y++) {
                for (var z = bounds.minZ(); z <= bounds.maxZ(); z++) {
                    var position = new BlockPos(x, y, z);
                    if (OreVeinUtil.oreAt(instance, position).isPresent()) {
                        return position;
                    }
                }
            }
        }
        throw new IllegalStateException("Placement fixture has no filled position on the requested side");
    }

    private static void postProcess(GameTestHelper helper, OreVeinPiece piece, BoundingBox box, BlockPos center) {
        piece.postProcess(helper.getLevel(), helper.getLevel().structureManager(),
            helper.getLevel().getChunkSource().getGenerator(), RandomSource.create(), box,
            new ChunkPos(center), center);
    }
}
