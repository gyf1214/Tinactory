package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import org.shsts.tinactory.content.worldgen.PlayerStartFeature;
import org.shsts.tinactory.content.worldgen.ore.OreVeinPiece;
import org.shsts.tinactory.content.worldgen.ore.OreVeinStructure;
import org.shsts.tinactory.core.worldgen.ore.EllipsoidShape;
import org.shsts.tinactory.core.worldgen.ore.MultiscaleStructurePlacement;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import static org.shsts.tinactory.AllRegistries.FEATURES;
import static org.shsts.tinactory.AllRegistries.ORE_SHAPES;
import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllWorldGens {
    public static final ResourceKey<Biome> VOID_WITH_START_BIOME;
    public static final IEntry<PlayerStartFeature> PLAYER_START_FEATURE;
    public static final IEntry<EllipsoidShape> ELLIPSOID_SHAPE;
    public static final IEntry<StructureType<OreVeinStructure>> ORE_VEIN_STRUCTURE_TYPE;
    public static final IEntry<StructurePieceType> ORE_VEIN_PIECE_TYPE;
    public static final IEntry<StructurePlacementType<MultiscaleStructurePlacement>> MULTISCALE_PLACEMENT_TYPE;
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_GROWER;

    private static final IEntryHandler<StructureType<?>> STRUCTURE_TYPES =
        REGISTRATE.getHandler(Registries.STRUCTURE_TYPE, BuiltInRegistries.STRUCTURE_TYPE);
    private static final IEntryHandler<StructurePieceType> STRUCTURE_PIECES =
        REGISTRATE.getHandler(Registries.STRUCTURE_PIECE, BuiltInRegistries.STRUCTURE_PIECE);
    private static final IEntryHandler<StructurePlacementType<?>> STRUCTURE_PLACEMENTS =
        REGISTRATE.getHandler(Registries.STRUCTURE_PLACEMENT, BuiltInRegistries.STRUCTURE_PLACEMENT);

    static {
        VOID_WITH_START_BIOME = ResourceKey.create(Registries.BIOME, modLoc("void_with_start"));
        PLAYER_START_FEATURE = REGISTRATE.registryEntry(FEATURES, "player_start", PlayerStartFeature::new);
        ELLIPSOID_SHAPE = REGISTRATE.registryEntry(ORE_SHAPES.getHandler(), "ellipsoid", EllipsoidShape::new);
        ORE_VEIN_STRUCTURE_TYPE = REGISTRATE.registryEntry(
            STRUCTURE_TYPES, "ore_vein", () -> () -> OreVeinStructure.CODEC);
        ORE_VEIN_PIECE_TYPE = REGISTRATE.registryEntry(
            STRUCTURE_PIECES, "ore_vein", () -> OreVeinPiece::new);
        MULTISCALE_PLACEMENT_TYPE = REGISTRATE.registryEntry(
            STRUCTURE_PLACEMENTS, "multiscale", () -> () -> MultiscaleStructurePlacement.CODEC);
        RUBBER_TREE_GROWER = ResourceKey.create(Registries.CONFIGURED_FEATURE, modLoc("rubber_tree"));
    }

    public static void init() {}
}
