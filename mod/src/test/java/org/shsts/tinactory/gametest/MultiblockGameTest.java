package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllCapabilities;
import org.shsts.tinactory.AllMultiblocks;
import org.shsts.tinactory.AllRegistries;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.content.multiblock.DigitalInterface;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.content.multiblock.Lithography;
import org.shsts.tinactory.content.multiblock.NuclearReactor;
import org.shsts.tinactory.content.multiblock.PowerSubstation;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.multiblock.BlockIngredient;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinactory.integration.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.integration.multiblock.WorldMultiblockCheckCtx;
import org.shsts.tinactory.integration.multiblock.WorldMultiblockManagers;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinactory.integration.network.PrimitiveBlock;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.shsts.tinactory.AllNetworks.BATTERY_DISCHARGE;

@GameTestHolder(TinactoryKeys.ID)
public final class MultiblockGameTest {
    private static final Direction FACING = Direction.SOUTH;
    private static final Direction WIDTH_DIRECTION = Direction.EAST;
    private static final BlockPos FORGE_HAMMER_CONTROLLER = new BlockPos(2, 1, 2);
    private static final BlockPos FORGE_HAMMER_INTERFACE = new BlockPos(1, 1, 0);
    private static final String[] STRUCTURE_SMOKE_A = {
        "forge_hammer", "ore_processing_unit", "autofarm", "bacteria_vat", "pyrolyse_oven", "lithography_machine",
        "extrusion_press", "precision_cutting_machine", "oil_cracking_unit", "multi_smelter", "large_turbine",
        "large_chemical_reactor", "large_boiler", "distillation_tower", "blast_furnace", "fusion_reactor"
    };
    private static final String[] STRUCTURE_SMOKE_B = {
        "prospecting_station", "phase_exchange_chamber", "electrochemical_processor", "batching_vessel",
        "assembly_line",
        "vacuum_freezer", "sifter", "rocket_launch_site", "metal_former", "autoclave", "power_substation",
        "implosion_compressor", "nuclear_reactor", "cleanroom"
    };

    private MultiblockGameTest() {}

    @GameTest(template = "empty_5x5x5", timeoutTicks = 80)
    public static void testRegisteredMultiblockFormsAndBindsItsInterface(GameTestHelper helper) {
        var placed = placeStructure(helper, "forge_hammer", FORGE_HAMMER_CONTROLLER,
            FORGE_HAMMER_INTERFACE);
        helper.runAfterDelay(20, () -> {
            var multiblock = Multiblock.get(helper.getBlockEntity(placed.controller()));
            var inter = (MultiblockInterface) AllCapabilities.MACHINE.get(helper.getBlockEntity(placed.interfacePos()));
            if (multiblock.getInterface().orElse(null) != inter) {
                fail(helper, placed.id(), "controller did not register its interface", placed.controller());
            }
            if (inter.getMultiblock().orElse(null) != multiblock) {
                fail(helper, placed.id(), "interface did not bind its controller", placed.interfacePos());
            }
            if (multiblock.container().isEmpty() || multiblock.processor().isEmpty()) {
                fail(helper, placed.id(), "registered controller did not expose its runtime capabilities",
                    placed.controller());
            }
            if (inter.container().isEmpty() || inter.processor().isEmpty() || !inter.isContainerReady()) {
                fail(helper, placed.id(), "registered interface did not expose its container and processor",
                    placed.interfacePos());
            }
            if (!helper.getBlockState(placed.interfacePos()).getValue(MultiblockInterfaceBlock.JOINED)) {
                fail(helper, placed.id(), "interface did not enter the joined state", placed.interfacePos());
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_64x16x64", timeoutTicks = 400)
    public static void testRegisteredStructureSmokeBatchA(GameTestHelper helper) {
        testRegisteredStructureSmokeBatch(helper, STRUCTURE_SMOKE_A);
    }

    @GameTest(template = "empty_64x16x64", timeoutTicks = 400)
    public static void testRegisteredStructureSmokeBatchB(GameTestHelper helper) {
        testRegisteredStructureSmokeBatch(helper, STRUCTURE_SMOKE_B);
    }

    @GameTest(template = "empty_5x5x5", timeoutTicks = 120)
    public static void testInvalidStructureRetriesAfterRepair(GameTestHelper helper) {
        var placed = placeStructure(helper, "forge_hammer", FORGE_HAMMER_CONTROLLER, FORGE_HAMMER_INTERFACE);
        var broken = new BlockPos(1, 1, 1);
        helper.runAfterDelay(20, () -> {
            var multiblock = Multiblock.get(helper.getBlockEntity(placed.controller()));
            if (multiblock.getInterface().isEmpty()) {
                fail(helper, placed.id(), "valid structure did not register before invalidation", placed.controller());
                return;
            }
            helper.setBlock(broken, Blocks.AIR.defaultBlockState());
            helper.runAfterDelay(20, () -> {
                var inter = (MultiblockInterface) AllCapabilities.MACHINE.get(
                    helper.getBlockEntity(placed.interfacePos()));
                if (multiblock.getInterface().isPresent() || inter.getMultiblock().isPresent() ||
                    helper.getBlockState(placed.interfacePos()).getValue(MultiblockInterfaceBlock.JOINED)) {
                    fail(helper, placed.id(), "invalidated structure remained joined", broken);
                    return;
                }
                var display = placed.display();
                var restored = structurePos(display, placed.controller(), 0, 0, 1);
                helper.setBlock(restored, display.getIngredient(0, 0, 1)
                    .orElseThrow().display(helper.getLevel().registryAccess()));
                helper.runAfterDelay(20, () -> {
                    if (multiblock.getInterface().orElse(null) != inter ||
                        inter.getMultiblock().orElse(null) != multiblock ||
                        !helper.getBlockState(placed.interfacePos()).getValue(MultiblockInterfaceBlock.JOINED)) {
                        fail(helper, placed.id(), "repaired structure did not re-register", placed.controller());
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = "empty_5x5x5", timeoutTicks = 80)
    public static void testStructureWithoutInterfaceDoesNotRegister(GameTestHelper helper) {
        placeDisplayedStructure(helper, "forge_hammer", FORGE_HAMMER_CONTROLLER, FACING);
        helper.runAfterDelay(20, () -> {
            if (Multiblock.get(helper.getBlockEntity(FORGE_HAMMER_CONTROLLER)).getInterface().isPresent()) {
                helper.fail("structure without interface registered", FORGE_HAMMER_CONTROLLER);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x5", timeoutTicks = 80)
    public static void testWrongControllerFacingDoesNotRegister(GameTestHelper helper) {
        placeDisplayedStructure(helper, "forge_hammer", FORGE_HAMMER_CONTROLLER, Direction.EAST);
        placeInterface(helper, FORGE_HAMMER_INTERFACE);
        helper.runAfterDelay(20, () -> {
            if (Multiblock.get(helper.getBlockEntity(FORGE_HAMMER_CONTROLLER)).getInterface().isPresent()) {
                helper.fail("wrong-facing structure registered", FORGE_HAMMER_CONTROLLER);
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x5", timeoutTicks = 120)
    public static void testMultiblockUpdateAndWorldContextContracts(GameTestHelper helper) {
        var placed = placeStructure(helper, "forge_hammer", FORGE_HAMMER_CONTROLLER, FORGE_HAMMER_INTERFACE);
        helper.runAfterDelay(20, () -> {
            var controllerEntity = helper.getBlockEntity(placed.controller());
            var interfaceEntity = helper.getBlockEntity(placed.interfacePos());
            var multiblock = Multiblock.get(controllerEntity);
            var inter = (MultiblockInterface) AllCapabilities.MACHINE.get(interfaceEntity);
            var context = new WorldMultiblockCheckCtx(helper.getLevel(), helper.absolutePos(placed.controller()));
            if (context.getBlock(helper.absolutePos(placed.controller())).isEmpty() ||
                context.getMachine(helper.absolutePos(placed.interfacePos())).isEmpty() ||
                context.getFacing().orElse(null) != FACING ||
                context.getBlock(new BlockPos(1000000, 0, 1000000)).isPresent() ||
                context.getMachine(new BlockPos(1000000, 0, 1000000)).isPresent()) {
                helper.fail("world-backed multiblock context contract failed", placed.controller());
                return;
            }
            if (WorldMultiblockManagers.get(helper.getLevel()) != WorldMultiblockManagers.get(helper.getLevel()) ||
                WorldMultiblockManagers.tryGet(helper.getLevel()).isEmpty()) {
                helper.fail("world multiblock manager was not reused", placed.controller());
                return;
            }
            if (multiblock.serializeOnUpdate(helper.getLevel().registryAccess()).isEmpty() ||
                inter.serializeOnUpdate(helper.getLevel().registryAccess()).isEmpty() ||
                multiblock.getLayout().isEmpty() || multiblock.electric().isEmpty() ||
                inter.electric().isEmpty() || !inter.isMultiblock() || inter.isDigital() ||
                inter.parallel() < 1 || inter.getAppearanceBlock().isEmpty()) {
                helper.fail("multiblock runtime contract was not exposed", placed.controller());
                return;
            }
            if (Multiblock.tryGet(controllerEntity).orElse(null) != multiblock ||
                multiblock.menu(inter) == null) {
                helper.fail("bound multiblock interface contract was not exposed", placed.interfacePos());
                return;
            }
            inter.title();
            inter.icon();
            inter.canPlayerInteract(helper.makeMockPlayer(GameType.SURVIVAL));
            multiblock.setWorkBlock(helper.getLevel(), helper.getBlockState(placed.controller()));
            inter.setConfig(SetMachineConfigPacket.builder().set(BATTERY_DISCHARGE, true).get(), false);
            inter.setMultiblock(multiblock);
            var update = multiblock.serializeOnUpdate(helper.getLevel().registryAccess());
            var interfaceUpdate = inter.serializeOnUpdate(helper.getLevel().registryAccess());
            multiblock.deserializeOnUpdate(helper.getLevel().registryAccess(), update);
            inter.deserializeOnUpdate(helper.getLevel().registryAccess(), interfaceUpdate);
            var displayIngredient = BlockIngredient.withDisplay(BlockIngredient.of(Blocks.STONE),
                BlockIngredient.of(Blocks.DIRT), state -> state);
            if (!displayIngredient.test(Blocks.STONE.defaultBlockState()) ||
                displayIngredient.expand(helper.getLevel().registryAccess()).isEmpty() ||
                !displayIngredient.display(helper.getLevel().registryAccess()).is(Blocks.DIRT)) {
                helper.fail("block ingredient display contract failed", placed.controller());
                return;
            }
            var interfaceBlock = (MultiblockInterfaceBlock) helper.getBlockState(placed.interfacePos()).getBlock();
            var joinedTint = MultiblockInterfaceBlock.tint(Voltage.IV,
                helper.getBlockState(placed.interfacePos()), 3);
            if (interfaceBlock.getRenderShape(helper.getBlockState(placed.interfacePos())) == null ||
                joinedTint == 0xFFFFFFFF) {
                helper.fail("joined interface block contract failed", placed.interfacePos());
                return;
            }
            interfaceBlock.getStateForPlacement(new BlockPlaceContext(helper.getLevel(),
                helper.makeMockPlayer(GameType.SURVIVAL), InteractionHand.MAIN_HAND, new ItemStack(interfaceBlock),
                new BlockHitResult(Vec3.atCenterOf(helper.absolutePos(placed.interfacePos())), Direction.NORTH,
                    helper.absolutePos(placed.interfacePos()), false)));
            var collectionIngredient = BlockIngredient.of(List.of(BlockIngredient.blockValue(() -> Blocks.STONE)));
            if (!collectionIngredient.test(Blocks.STONE.defaultBlockState())) {
                helper.fail("collection block ingredient did not match", placed.controller());
                return;
            }
            multiblock.onInvalidateStructure();
            if (inter.getMultiblock().isPresent() ||
                helper.getBlockState(placed.interfacePos()).getValue(MultiblockInterfaceBlock.JOINED)) {
                helper.fail("multiblock invalidation did not reset interface", placed.interfacePos());
                return;
            }
            if (inter.title().getString().isEmpty() || inter.icon().isEmpty()) {
                helper.fail("unbound interface presentation contract failed", placed.interfacePos());
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_32x16x32", timeoutTicks = 160)
    public static void testConcreteMultiblockStateAndSerialization(GameTestHelper helper) {
        var coilPlaced = placeStructureAtBase(helper, "multi_smelter", new BlockPos(1, 1, 1));
        var towerPlaced = placeStructureAtBase(helper, "distillation_tower", new BlockPos(10, 1, 1));
        var lithographyPlaced = placeStructureAtBase(helper, "lithography_machine", new BlockPos(20, 1, 1));
        helper.runAfterDelay(30, () -> {
            var coil = (CoilMultiblock) Multiblock.get(helper.getBlockEntity(coilPlaced.controller()));
            var tower = (DistillationTower) Multiblock.get(helper.getBlockEntity(towerPlaced.controller()));
            var lithography = (Lithography) Multiblock.get(helper.getBlockEntity(lithographyPlaced.controller()));
            var provider = helper.getLevel().registryAccess();
            if (coil.getTemperature().isEmpty() ||
                CoilMultiblock.getTemperature(AllCapabilities.MACHINE.get(
                    helper.getBlockEntity(coilPlaced.interfacePos()))).isEmpty()) {
                helper.fail("coil multiblock did not select a registered coil", coilPlaced.controller());
                return;
            }
            var coilUpdate = coil.serializeOnUpdate(provider);
            coil.deserializeOnUpdate(provider, coilUpdate);
            if (coil.getTemperature().isEmpty()) {
                helper.fail("coil selection did not survive update serialization", coilPlaced.controller());
                return;
            }
            if (tower.getSlots() < 1 || tower.getLayout().isEmpty()) {
                helper.fail("distillation tower did not select its height layout", towerPlaced.controller());
                return;
            }
            var towerUpdate = tower.serializeOnUpdate(provider);
            tower.deserializeOnUpdate(provider, towerUpdate);
            if (tower.getSlots() < 1 || tower.getLayout().isEmpty()) {
                helper.fail("distillation layout did not survive update serialization", towerPlaced.controller());
                return;
            }
            if (lithography.getLens().isEmpty() || lithography.getCleannessFactor() <= 0d) {
                helper.fail("lithography multiblock did not select its lens", lithographyPlaced.controller());
                return;
            }
            var lithographyUpdate = lithography.serializeOnUpdate(provider);
            lithography.deserializeOnUpdate(provider, lithographyUpdate);
            if (lithography.getLens().isEmpty()) {
                helper.fail("lithography lens did not survive update serialization", lithographyPlaced.controller());
                return;
            }
            helper.succeed();
        });
    }

    @GameTest(template = "empty_32x16x32", timeoutTicks = 160)
    public static void testNuclearReactorStateAndPersistence(GameTestHelper helper) {
        var placed = placeStructureAtBase(helper, "nuclear_reactor", new BlockPos(1, 1, 1));
        helper.runAfterDelay(30, () -> {
            var reactor = (NuclearReactor) Multiblock.get(helper.getBlockEntity(placed.controller()));
            var provider = helper.getLevel().registryAccess();
            if (reactor.rows() != 2 || reactor.columns() != 2 || reactor.reactorItems().getSlots() < 4 ||
                reactor.minHeat() <= 0d || reactor.maxHeat() <= reactor.minHeat()) {
                helper.fail("nuclear reactor did not select its minimum layout", placed.controller());
                return;
            }
            var rod = AllRegistries.ITEMS
                .getEntry(ResourceLocation.parse("tinactory:component/uranium_fuel_rod")).get();
            var remainder = reactor.reactorItems().insertItem(0, new ItemStack(rod), false);
            if (!remainder.isEmpty()) {
                helper.fail("nuclear reactor rejected its registered fuel rod", placed.controller());
                return;
            }
            reactor.onPreWork();
            reactor.onWorkTick(1d);
            reactor.getInfo(0, 0);
            reactor.getInfo(1, 0);
            reactor.getInfo(2, 0);
            reactor.getAllInfo();
            if (reactor.progressTicks() < 0 || reactor.workSpeed() != -1d) {
                helper.fail("nuclear reactor did not expose boiler runtime state", placed.controller());
                return;
            }
            var state = reactor.serializeNBT(provider);
            reactor.deserializeNBT(provider, state);
            var update = reactor.serializeOnUpdate(provider);
            reactor.deserializeOnUpdate(provider, update);
            if (reactor.rows() != 2 || reactor.columns() != 2 || reactor.heat() < 0d) {
                helper.fail("nuclear reactor state did not survive persistence", placed.controller());
                return;
            }
            reactor.onInvalidateStructure();
            helper.succeed();
        });
    }

    @GameTest(template = "empty_32x16x32", timeoutTicks = 160)
    public static void testCleanroomRuntimeStateAndPersistence(GameTestHelper helper) {
        var placed = placeStructureAtBase(helper, "cleanroom", new BlockPos(1, 1, 1));
        helper.runAfterDelay(30, () -> {
            var cleanroom = (Cleanroom) Multiblock.get(helper.getBlockEntity(placed.controller()));
            var provider = helper.getLevel().registryAccess();
            if (cleanroom.getVoltage() <= 0 || cleanroom.getPowerCons() <= 0d ||
                cleanroom.getPowerGen() != 0d || cleanroom.getMachineType().name().equals("NONE")) {
                helper.fail("cleanroom did not expose its powered runtime state", placed.controller());
                return;
            }
            cleanroom.onWorkTick(1d);
            if (cleanroom.getProgress() <= 0d || !cleanroom.isWorking(1d) ||
                cleanroom.isWorking(0d) || Cleanroom.getCleanness(helper.getLevel(),
                    helper.absolutePos(placed.controller()).below()) <= 0d) {
                helper.fail("cleanroom did not increase cleanness", placed.controller());
                return;
            }
            var state = new CompoundTag();
            state.putDouble("cleanness", 0.5d);
            cleanroom.deserializeNBT(provider, state);
            if (cleanroom.getProgress() != 0.5d || cleanroom.serializeNBT(provider).getDouble("cleanness") != 0.5d) {
                helper.fail("cleanroom cleanness did not persist", placed.controller());
                return;
            }
            if (Cleanroom.getCleanness(helper.getLevel(), helper.absolutePos(placed.controller()).below()) != 0.5d) {
                helper.fail("cleanroom manager did not expose persisted cleanness", placed.controller());
                return;
            }
            cleanroom.onInvalidateStructure();
            helper.succeed();
        });
    }

    @GameTest(template = "empty_32x16x32", timeoutTicks = 160)
    public static void testPowerSubstationRuntimeStateAndPersistence(GameTestHelper helper) {
        var placed = placeStructureAtBase(helper, "power_substation", new BlockPos(1, 1, 1));
        helper.runAfterDelay(30, () -> {
            var substation = (PowerSubstation) Multiblock.get(helper.getBlockEntity(placed.controller()));
            var inter = (MultiblockInterface) AllCapabilities.MACHINE.get(helper.getBlockEntity(placed.interfacePos()));
            var provider = helper.getLevel().registryAccess();
            if (substation.powerCapacity() <= 0 || substation.getVoltage() <= 0 ||
                substation.getPowerCons() <= 0d) {
                helper.fail("power substation did not calculate its capacity", placed.controller());
                return;
            }
            substation.onWorkTick(1d);
            var state = new CompoundTag();
            state.putLong("power", substation.powerCapacity() / 2);
            substation.deserializeNBT(provider, state);
            if (substation.powerLevel() <= 0 || substation.getPowerGen() <= 0d) {
                helper.fail("power substation did not restore stored power", placed.controller());
                return;
            }
            inter.config().apply(SetMachineConfigPacket.builder().set(BATTERY_DISCHARGE, true).get());
            if (substation.getPowerCons() != 0d || substation.getMachineType().name().equals("BUFFER")) {
                helper.fail("power substation discharge mode was not applied", placed.controller());
                return;
            }
            if (substation.serializeNBT(provider).getLong("power") <= 0) {
                helper.fail("power substation did not serialize stored power", placed.controller());
                return;
            }
            substation.onInvalidateStructure();
            helper.succeed();
        });
    }

    @GameTest(template = "empty_5x5x5", timeoutTicks = 80)
    public static void testDigitalInterfaceLayoutAndPersistence(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.getMachine("multiblock/digital_interface").block(Voltage.EV)
            .defaultBlockState().setValue(MachineBlock.IO_FACING, Direction.NORTH));
        var digital = (DigitalInterface) AllCapabilities.MACHINE.get(helper.getBlockEntity(pos));
        var layout = Layout.builder()
            .slot(0, SlotType.ITEM_INPUT, 0, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .slot(1, SlotType.ITEM_OUTPUT, 18, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .slot(2, SlotType.FLUID_INPUT, 36, 0, Voltage.between(Voltage.EV, Voltage.EV))
            .buildLayout(Voltage.EV);
        digital.setLayout(layout);
        var input = digital.getPort(0, ContainerAccess.EXTERNAL).asItem();
        var output = digital.getPort(1, ContainerAccess.INTERNAL).asItem();
        var inserted = input.insert(new ItemStack(Items.COBBLESTONE, 16), false);
        if (!inserted.isEmpty() || digital.bytesCapacity() <= 0 || digital.bytesUsed() <= 0 ||
            digital.portSize() != 3 || !digital.hasPort(0) || digital.hasPort(-1) ||
            digital.hasPort(3) || digital.getLayout() != layout || digital.maxParallel() < 1 ||
            digital.portDirection(0) == digital.portDirection(1)) {
            helper.fail("digital interface did not configure its layout", pos);
            return;
        }
        output.insert(new ItemStack(Items.IRON_INGOT, 8), false);
        var state = digital.serializeNBT(helper.getLevel().registryAccess());
        digital.resetLayout();
        digital.deserializeNBT(helper.getLevel().registryAccess(), state);
        if (digital.bytesUsed() <= 0) {
            helper.fail("digital interface storage did not survive persistence", pos);
            return;
        }
        helper.succeed();
    }

    private static void testRegisteredStructureSmokeBatch(GameTestHelper helper, String[] ids) {
        var placed = new ArrayList<PlacedMultiblock>();
        for (var i = 0; i < ids.length; i++) {
            var display = Objects.requireNonNull(AllMultiblocks.MULTIBLOCK_SETS.get(ids[i]), ids[i]).display();
            var base = new BlockPos((i % 4) * 16 + 1, 1, (i / 4) * 16 + 1);
            var controller = base.offset(display.controllerPosition().getX(), display.controllerPosition().getY(),
                display.controllerPosition().getZ());
            placed.add(placeStructure(helper, ids[i], controller));
        }
        helper.runAfterDelay(40, () -> {
            for (var structure : placed) {
                var multiblock = Multiblock.get(helper.getBlockEntity(structure.controller()));
                var inter = (MultiblockInterface) AllCapabilities.MACHINE.get(
                    helper.getBlockEntity(structure.interfacePos()));
                if (multiblock.getInterface().orElse(null) != inter ||
                    inter.getMultiblock().orElse(null) != multiblock ||
                    !helper.getBlockState(structure.interfacePos()).getValue(MultiblockInterfaceBlock.JOINED)) {
                    fail(helper, structure.id(), "registered structure did not bind its interface",
                        structure.controller());
                    return;
                }
            }
            helper.succeed();
        });
    }

    private static PlacedMultiblock placeStructure(GameTestHelper helper, String id, BlockPos controller,
        BlockPos interfacePos) {
        placeDisplayedStructure(helper, id, controller, FACING);
        placeInterface(helper, interfacePos);
        return new PlacedMultiblock(id, controller, interfacePos,
            Objects.requireNonNull(AllMultiblocks.MULTIBLOCK_SETS.get(id), id).display());
    }

    private static PlacedMultiblock placeStructureAtBase(GameTestHelper helper, String id, BlockPos base) {
        var display = Objects.requireNonNull(AllMultiblocks.MULTIBLOCK_SETS.get(id), id).display();
        var controller = base.offset(display.controllerPosition().getX(), display.controllerPosition().getY(),
            display.controllerPosition().getZ());
        return placeStructure(helper, id, controller);
    }

    private static PlacedMultiblock placeStructure(GameTestHelper helper, String id, BlockPos controller) {
        var display = placeDisplayedStructure(helper, id, controller, FACING);
        var interfacePos = findInterface(helper, controller, display);
        if (interfacePos == null) {
            helper.fail("no compatible interface position found for " + id, controller);
        }
        return new PlacedMultiblock(id, controller, interfacePos, display);
    }

    private static IMultiblockDisplay placeDisplayedStructure(GameTestHelper helper, String id, BlockPos controller,
        Direction controllerFacing) {
        var set = Objects.requireNonNull(AllMultiblocks.MULTIBLOCK_SETS.get(id), id);
        var display = set.display();
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var pos = structurePos(display, controller, x, y, z);
                    display.getIngredient(x, y, z)
                        .ifPresent(ingredient -> helper.setBlock(pos,
                            ingredient.display(helper.getLevel().registryAccess())));
                }
            }
        }

        var controllerState = set.controller().get().defaultBlockState();
        if (controllerState.hasProperty(PrimitiveBlock.FACING)) {
            controllerState = controllerState.setValue(PrimitiveBlock.FACING, controllerFacing);
        }
        helper.setBlock(controller, controllerState);
        return display;
    }

    private static void placeInterface(GameTestHelper helper, BlockPos interfacePos) {
        var interfaceState = AllBlockEntities.getMachine("multiblock/interface").block(Voltage.IV).defaultBlockState();
        if (interfaceState.hasProperty(MachineBlock.IO_FACING)) {
            interfaceState = interfaceState.setValue(MachineBlock.IO_FACING, FACING.getOpposite());
        }
        helper.setBlock(interfacePos, interfaceState);
    }

    private static BlockPos findInterface(GameTestHelper helper, BlockPos controller, IMultiblockDisplay display) {
        var multiblock = Multiblock.get(helper.getBlockEntity(controller));
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    if (display.controllerPosition().equals(new BlockPos(x, y, z))) {
                        continue;
                    }
                    var pos = structurePos(display, controller, x, y, z);
                    if (display.getIngredient(x, y, z).isEmpty()) {
                        continue;
                    }
                    var original = helper.getBlockState(pos);
                    placeInterface(helper, pos);
                    if (multiblock.checkStructure().isPresent()) {
                        return pos;
                    }
                    helper.setBlock(pos, original);
                }
            }
        }
        return null;
    }

    private static BlockPos structurePos(IMultiblockDisplay display, BlockPos controller, int x, int y, int z) {
        var controllerPos = display.controllerPosition();
        return controller.relative(WIDTH_DIRECTION, x - controllerPos.getX())
            .relative(FACING, z - controllerPos.getZ())
            .offset(0, y - controllerPos.getY(), 0);
    }

    private static void fail(GameTestHelper helper, String id, String message, BlockPos pos) {
        helper.fail(message + " for " + id + " at " + pos, pos);
    }

    private record PlacedMultiblock(String id, BlockPos controller, BlockPos interfacePos,
        IMultiblockDisplay display) {}
}
