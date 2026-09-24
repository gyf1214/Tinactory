package org.shsts.tinactory.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.content.gui.MESignalControllerMenu;
import org.shsts.tinactory.content.gui.MEStorageDetectorMenu;
import org.shsts.tinactory.content.gui.StorageMenu;
import org.shsts.tinactory.content.gui.StorageMenus;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricStorage;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.content.logistics.SignalConfig;
import org.shsts.tinactory.content.logistics.StorageDetectorConfig;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.gui.IMenuHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllNetworks.SIGNAL_CONFIG;
import static org.shsts.tinactory.AllNetworks.STORAGE_DETECTOR;
import static org.shsts.tinactory.AllNetworks.STORAGE_FILTERS;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@GameTestHolder(TinactoryKeys.ID)
public final class MEStorageSignalGameTest {
    private static final BlockPos HUB = new BlockPos(8, 2, 8);
    private static final BlockPos INTERFACE = HUB.above();
    private static final BlockPos NORTH_CABLE = HUB.north();
    private static final BlockPos NORTH_ENDPOINT = HUB.north(2);
    private static final BlockPos SOUTH_CABLE = HUB.south();
    private static final BlockPos SOUTH_ENDPOINT = HUB.south(2);
    private static final BlockPos WEST_CABLE = HUB.west();
    private static final BlockPos WEST_ENDPOINT = HUB.west(2);
    private static final BlockPos EAST_CABLE = HUB.east();
    private static final BlockPos EAST_ENDPOINT = HUB.east(2);

    @GameTest(template = "empty_32x8x16", timeoutTicks = 220)
    public static void testMEStorageInterfaceAggregatesTransfersAndRevalidates(GameTestHelper helper) {
        var isolatedPos = new BlockPos(2, 2, 2);
        helper.setBlock(isolatedPos, machineState("logistics/me_storage_interface", Direction.DOWN, Direction.EAST));
        var isolatedSync = storageSync(helper, isolatedPos);
        if (!isolatedSync.entries().isEmpty()) {
            helper.fail("Unconnected ME Storage Interface exposed its own storage", isolatedPos);
            return;
        }

        placeCableTree(helper);
        helper.setBlock(INTERFACE, machineState("logistics/me_storage_interface", Direction.DOWN, Direction.EAST));
        helper.setBlock(WEST_ENDPOINT, machineState("logistics/ulv/electric_chest", Direction.EAST, Direction.NORTH));
        helper.setBlock(EAST_ENDPOINT, machineState("logistics/ulv/electric_tank", Direction.WEST, Direction.NORTH));
        seedChest(helper, WEST_ENDPOINT, new ItemStack(Items.DIAMOND, 4));
        seedTank(helper, EAST_ENDPOINT, new FluidStack(Fluids.WATER, 1000));
        useWithMockPlayer(helper, INTERFACE);

        helper.runAfterDelay(24, () -> {
            var sync = storageSync(helper, INTERFACE);
            var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.DIAMOND));
            var waterKey = StackHelper.FLUID_ADAPTER.keyOf(new FluidStack(Fluids.WATER, 1));
            if (!hasEntry(sync, diamondKey, 4) || !hasEntry(sync, waterKey, 1000)) {
                helper.fail("ME Storage Interface did not aggregate connected item and fluid storage", INTERFACE);
                return;
            }

            var player = helper.makeMockPlayer(GameType.SURVIVAL);
            var itemMenu = storageMenu(helper, INTERFACE, player);
            itemMenu.handleEventPacket(AllMenus.STORAGE_SLOT,
                new StorageEventPacket(diamondKey, 4, 0, false));
            if (chestAmount(helper, WEST_ENDPOINT, Items.DIAMOND) != 0 ||
                itemMenu.getCarried().getCount() != 4) {
                helper.fail("ME Storage Interface item transfer did not extract exactly once", INTERFACE);
                return;
            }

            var fluidMenu = storageMenu(helper, INTERFACE, player);
            fluidMenu.setCarried(new ItemStack(Items.BUCKET));
            fluidMenu.handleEventPacket(AllMenus.STORAGE_SLOT,
                new StorageEventPacket(waterKey, 1000, 0, false));
            if (tankAmount(helper, EAST_ENDPOINT, Fluids.WATER) != 0 ||
                !fluidMenu.getCarried().is(Items.WATER_BUCKET)) {
                helper.fail("ME Storage Interface fluid transfer did not drain exactly once", INTERFACE);
                return;
            }

            seedChest(helper, WEST_ENDPOINT, new ItemStack(Items.EMERALD));
            helper.destroyBlock(HUB);
            helper.runAfterDelay(24, () -> {
                if (!storageSync(helper, INTERFACE).entries().isEmpty()) {
                    helper.fail("ME Storage Interface retained detached storage after network invalidation", INTERFACE);
                    return;
                }
                helper.destroyBlock(INTERFACE);
                restoreCableTree(helper);
                helper.setBlock(INTERFACE,
                    machineState("logistics/me_storage_interface", Direction.DOWN, Direction.EAST));
                useWithMockPlayer(helper, INTERFACE);
                helper.runAfterDelay(24, () -> {
                    var reconnected = storageSync(helper, INTERFACE);
                    var emeraldKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.EMERALD));
                    if (!hasEntry(reconnected, emeraldKey, 1)) {
                        helper.fail("ME Storage Interface did not rebuild its aggregate after reconnection", INTERFACE);
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    @GameTest(template = "empty_32x8x16", timeoutTicks = 220)
    public static void testMEStorageDetectorItemSignals(GameTestHelper helper) {
        placeCableTree(helper);
        helper.setBlock(SOUTH_ENDPOINT,
            machineState("logistics/me_storage_detector", Direction.NORTH, Direction.EAST));
        helper.setBlock(WEST_ENDPOINT,
            machineState("logistics/ulv/electric_chest", Direction.EAST, Direction.NORTH));
        placeRedstoneOutput(helper, SOUTH_ENDPOINT);
        useWithMockPlayer(helper, SOUTH_ENDPOINT);

        helper.runAfterDelay(24, () -> {
            var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.DIAMOND));
            configureDetector(helper, SOUTH_ENDPOINT, new StorageDetectorConfig(diamondKey, 4));
            helper.runAfterDelay(4, () -> {
                if (!assertRedstone(helper, SOUTH_ENDPOINT, 0)) {
                    return;
                }
                insertItem(helper, WEST_ENDPOINT, new ItemStack(Items.DIAMOND, 2));
                helper.runAfterDelay(4, () -> {
                    if (!assertRedstone(helper, SOUTH_ENDPOINT, 8)) {
                        return;
                    }
                    insertItem(helper, WEST_ENDPOINT, new ItemStack(Items.DIAMOND, 2));
                    helper.runAfterDelay(4, () -> {
                        if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                            return;
                        }
                        insertItem(helper, WEST_ENDPOINT, new ItemStack(Items.DIAMOND, 4));
                        helper.runAfterDelay(4, () -> {
                            if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                                return;
                            }
                            configureChestFilters(helper, WEST_ENDPOINT,
                                List.of(FilterEntry.fromItem(new ItemStack(Items.EMERALD))));
                            helper.runAfterDelay(4, () -> {
                                if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                                    return;
                                }
                                configureDetector(helper, SOUTH_ENDPOINT, null);
                                helper.runAfterDelay(4, () -> {
                                    if (!assertRedstone(helper, SOUTH_ENDPOINT, 0)) {
                                        return;
                                    }
                                    configureDetector(helper, SOUTH_ENDPOINT,
                                        new StorageDetectorConfig(diamondKey, 0));
                                    helper.runAfterDelay(4, () -> {
                                        if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                                            return;
                                        }
                                        removeItem(helper, WEST_ENDPOINT, Items.DIAMOND, 8);
                                        helper.runAfterDelay(4, () -> {
                                            if (assertRedstone(helper, SOUTH_ENDPOINT, 0)) {
                                                helper.succeed();
                                            }
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(template = "empty_32x8x16", timeoutTicks = 220)
    public static void testMEStorageDetectorFluidSignals(GameTestHelper helper) {
        placeCableTree(helper);
        helper.setBlock(SOUTH_ENDPOINT,
            machineState("logistics/me_storage_detector", Direction.NORTH, Direction.EAST));
        helper.setBlock(EAST_ENDPOINT,
            machineState("logistics/ulv/electric_tank", Direction.WEST, Direction.NORTH));
        placeRedstoneOutput(helper, SOUTH_ENDPOINT);
        useWithMockPlayer(helper, SOUTH_ENDPOINT);

        helper.runAfterDelay(24, () -> {
            var waterKey = StackHelper.FLUID_ADAPTER.keyOf(new FluidStack(Fluids.WATER, 1));
            configureDetector(helper, SOUTH_ENDPOINT, new StorageDetectorConfig(waterKey, 1000));
            helper.runAfterDelay(4, () -> {
                if (!assertRedstone(helper, SOUTH_ENDPOINT, 0)) {
                    return;
                }
                fillTank(helper, EAST_ENDPOINT, 500);
                helper.runAfterDelay(4, () -> {
                    if (!assertRedstone(helper, SOUTH_ENDPOINT, 8)) {
                        return;
                    }
                    fillTank(helper, EAST_ENDPOINT, 500);
                    helper.runAfterDelay(4, () -> {
                        if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                            return;
                        }
                        fillTank(helper, EAST_ENDPOINT, 1000);
                        helper.runAfterDelay(4, () -> {
                            if (!assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                                return;
                            }
                            configureTankFilters(helper, EAST_ENDPOINT,
                                List.of(FilterEntry.fromFluid(new FluidStack(
                                    Fluids.LAVA, 1))));
                            helper.runAfterDelay(4, () -> {
                                if (assertRedstone(helper, SOUTH_ENDPOINT, 15)) {
                                    helper.succeed();
                                }
                            });
                        });
                    });
                });
            });
        });
    }

    @GameTest(template = "empty_32x8x16", timeoutTicks = 180)
    public static void testMESignalControllerReadsStorageAndClearsUnknownConfig(GameTestHelper helper) {
        placeCableTree(helper);
        helper.setBlock(NORTH_ENDPOINT,
            machineState("logistics/me_signal_controller", Direction.SOUTH, Direction.EAST));
        helper.setBlock(WEST_ENDPOINT,
            machineState("logistics/ulv/electric_chest", Direction.EAST, Direction.NORTH));
        placeRedstoneOutput(helper, NORTH_ENDPOINT);
        useWithMockPlayer(helper, NORTH_ENDPOINT);

        helper.runAfterDelay(24, () -> {
            var storageMachine = MACHINE.get(helper.getBlockEntity(WEST_ENDPOINT));
            var signal = visibleSignal(helper, NORTH_ENDPOINT, storageMachine.uuid(), ElectricStorage.AMOUNT_SIGNAL,
                false);
            if (signal.isEmpty()) {
                helper.fail("ME Signal Controller did not expose the chest amount signal", NORTH_ENDPOINT);
                return;
            }
            configureController(helper, NORTH_ENDPOINT,
                new SignalConfig(storageMachine.uuid(), ElectricStorage.AMOUNT_SIGNAL));
            helper.runAfterDelay(4, () -> {
                if (!assertRedstone(helper, NORTH_ENDPOINT, 0)) {
                    return;
                }
                insertItem(helper, WEST_ENDPOINT, new ItemStack(Items.DIAMOND));
                helper.runAfterDelay(4, () -> {
                    if (!assertRedstone(helper, NORTH_ENDPOINT, 1)) {
                        return;
                    }
                    configureController(helper, NORTH_ENDPOINT,
                        new SignalConfig(UUID.randomUUID(), ElectricStorage.AMOUNT_SIGNAL));
                    helper.runAfterDelay(8, () -> {
                        if (assertRedstone(helper, NORTH_ENDPOINT, 0)) {
                            helper.succeed();
                        }
                    });
                });
            });
        });
    }

    @GameTest(template = "empty_32x8x16", timeoutTicks = 260)
    public static void testMESignalControllerWritesBoilerStopSignalAndRevalidates(GameTestHelper helper) {
        placeCableTree(helper);
        helper.setBlock(NORTH_ENDPOINT,
            machineState("logistics/me_signal_controller", Direction.SOUTH, Direction.EAST));
        helper.setBlock(WEST_ENDPOINT, boilerState(Direction.EAST));
        var inputPos = NORTH_ENDPOINT.east();
        placeRedstoneInput(helper, inputPos);
        useWithMockPlayer(helper, NORTH_ENDPOINT);

        helper.runAfterDelay(24, () -> {
            var boilerMachine = MACHINE.get(helper.getBlockEntity(WEST_ENDPOINT));
            if (visibleSignal(helper, NORTH_ENDPOINT, boilerMachine.uuid(), Machine.STOP_SIGNAL, true).isEmpty()) {
                helper.fail("ME Signal Controller did not expose the boiler stop signal", NORTH_ENDPOINT);
                return;
            }
            configureController(helper, NORTH_ENDPOINT, new SignalConfig(boilerMachine.uuid(), Machine.STOP_SIGNAL));
            helper.runAfterDelay(4, () -> {
                insertBoilerFuel(helper, WEST_ENDPOINT);
                helper.runAfterDelay(16, () -> {
                    if (!boilerStopped(helper, WEST_ENDPOINT)) {
                        var fuel = MENU_ITEM_HANDLER.get(helper.getBlockEntity(WEST_ENDPOINT)).getStackInSlot(0);
                        helper.fail("Redstone input did not stop the boiler through the signal controller: " +
                            "working=" + helper.getBlockState(WEST_ENDPOINT).getValue(MachineBlock.WORKING) +
                            ", fuel=" + fuel, WEST_ENDPOINT);
                        return;
                    }
                    clearBoilerFuel(helper, WEST_ENDPOINT);
                    configureController(helper, NORTH_ENDPOINT,
                        new SignalConfig(UUID.randomUUID(), Machine.STOP_SIGNAL));
                    helper.runAfterDelay(4, () -> {
                        configureController(helper, NORTH_ENDPOINT,
                            new SignalConfig(boilerMachine.uuid(), Machine.STOP_SIGNAL));
                        helper.runAfterDelay(4, () -> {
                            insertBoilerFuel(helper, WEST_ENDPOINT);
                            helper.runAfterDelay(16, () -> {
                                if (!boilerStopped(helper, WEST_ENDPOINT)) {
                                    helper.fail("Controller reconfiguration did not revalidate its write endpoint",
                                        NORTH_ENDPOINT);
                                    return;
                                }
                                helper.setBlock(inputPos, Blocks.AIR);
                                helper.runAfterDelay(16, () -> {
                                    if (boilerStopped(helper, WEST_ENDPOINT)) {
                                        helper.fail("Removing redstone input did not resume the boiler", WEST_ENDPOINT);
                                        return;
                                    }
                                    placeRedstoneInput(helper, inputPos);
                                    helper.destroyBlock(HUB);
                                    helper.runAfterDelay(24, () -> {
                                        helper.destroyBlock(WEST_ENDPOINT);
                                        restoreCableTree(helper);
                                        helper.setBlock(WEST_ENDPOINT, boilerState(Direction.EAST));
                                        var reconnectedBoiler = MACHINE.get(helper.getBlockEntity(WEST_ENDPOINT));
                                        helper.setBlock(NORTH_ENDPOINT, machineState(
                                            "logistics/me_signal_controller", Direction.SOUTH, Direction.EAST));
                                        useWithMockPlayer(helper, NORTH_ENDPOINT);
                                        helper.runAfterDelay(24, () -> {
                                            configureController(helper, NORTH_ENDPOINT,
                                                new SignalConfig(reconnectedBoiler.uuid(), Machine.STOP_SIGNAL));
                                            helper.runAfterDelay(4, () -> {
                                                insertBoilerFuel(helper, WEST_ENDPOINT);
                                                helper.runAfterDelay(16, () -> {
                                                    if (boilerStopped(helper, WEST_ENDPOINT)) {
                                                        helper.succeed();
                                                    } else {
                                                        helper.fail("Reconnected controller did not revalidate its " +
                                                            "write endpoint", NORTH_ENDPOINT);
                                                    }
                                                });
                                            });
                                        });
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private static void placeCableTree(GameTestHelper helper) {
        helper.setBlock(HUB, cableState());
        helper.setBlock(NORTH_CABLE, cableState());
        helper.setBlock(SOUTH_CABLE, cableState());
        helper.setBlock(WEST_CABLE, cableState());
        helper.setBlock(EAST_CABLE, cableState());
    }

    private static void restoreCableTree(GameTestHelper helper) {
        var cable = AllItems.getComponent("cable").get(Voltage.LV).get().asItem();
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var clickedPos = helper.absolutePos(NORTH_CABLE);
        var hit = new BlockHitResult(Vec3.atCenterOf(clickedPos), Direction.SOUTH, clickedPos, false);
        new ItemStack(cable).useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.setBlock(HUB, cableState());
    }

    private static BlockState cableState() {
        var cable = (Block) AllItems.getComponent("cable").get(Voltage.LV).get();
        return cable.defaultBlockState()
            .setValue(CableBlock.NORTH, true)
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.SOUTH, true)
            .setValue(CableBlock.WEST, true)
            .setValue(CableBlock.UP, true)
            .setValue(CableBlock.DOWN, true);
    }

    private static BlockState machineState(String name, Direction ioFacing, Direction facing) {
        var machine = AllBlockEntities.getMachine(name);
        var block = machine == null ? BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(
            TinactoryKeys.ID, name)) : machine.block(Voltage.LV);
        return block.defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing)
            .setValue(MachineBlock.FACING, facing);
    }

    private static BlockState boilerState(Direction ioFacing) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(
            TinactoryKeys.ID, "machine/boiler/high")).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing)
            .setValue(MachineBlock.FACING, Direction.NORTH);
    }

    private static void placeRedstoneOutput(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.east().below(), Blocks.STONE);
        helper.setBlock(pos.east(), Blocks.REDSTONE_WIRE);
        helper.setBlock(pos.east(2), Blocks.REDSTONE_LAMP);
    }

    private static void placeRedstoneInput(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos.east(), Blocks.AIR);
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.setBlock(pos, Blocks.REDSTONE_WIRE.defaultBlockState()
            .setValue(RedStoneWireBlock.POWER, 15)
            .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE));
    }

    private static void useWithMockPlayer(GameTestHelper helper, BlockPos pos) {
        useWithMockPlayer(helper, helper.makeMockPlayer(GameType.SURVIVAL), pos);
    }

    private static void useWithMockPlayer(GameTestHelper helper, Player player, BlockPos pos) {
        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }

    private static StorageMenu storageMenu(GameTestHelper helper, BlockPos pos, Player player) {
        var properties = new MenuBase.Properties(new CapturingMenuHelper(), AllMenus.ME_STORAGE_INTERFACE.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        return StorageMenus.meStorageInterface(properties);
    }

    private static StorageSyncPacket storageSync(GameTestHelper helper, BlockPos pos) {
        var player = makeServerPlayer(helper);
        var capture = new CapturingMenuHelper();
        var properties = new MenuBase.Properties(capture, AllMenus.ME_STORAGE_INTERFACE.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = StorageMenus.meStorageInterface(properties);
        menu.broadcastChanges();
        return capture.packet(StorageSyncPacket.class).orElseThrow();
    }

    private static void configureDetector(GameTestHelper helper, BlockPos pos, StorageDetectorConfig config) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var properties = new MenuBase.Properties(new CapturingMenuHelper(), AllMenus.ME_STORAGE_DETECTOR.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = new MEStorageDetectorMenu(properties);
        var packet = SetMachineConfigPacket.builder();
        if (config == null) {
            packet.reset(STORAGE_DETECTOR.get());
        } else {
            packet.set(STORAGE_DETECTOR, config);
        }
        menu.handleEventPacket(AllMenus.SET_MACHINE_CONFIG, packet.get());
    }

    private static void configureController(GameTestHelper helper, BlockPos pos, SignalConfig config) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var properties = new MenuBase.Properties(new CapturingMenuHelper(), AllMenus.ME_SIGNAL_CONTROLLER.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = new MESignalControllerMenu(properties);
        menu.handleEventPacket(AllMenus.SET_MACHINE_CONFIG,
            SetMachineConfigPacket.builder().set(SIGNAL_CONFIG, config).get());
    }

    private static void configureChestFilters(GameTestHelper helper, BlockPos pos, List<FilterEntry> filters) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var properties = new MenuBase.Properties(new CapturingMenuHelper(), AllMenus.ELECTRIC_CHEST.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = StorageMenus.electricChest(properties);
        menu.handleEventPacket(AllMenus.SET_MACHINE_CONFIG,
            SetMachineConfigPacket.builder().set(STORAGE_FILTERS, filters).get());
    }

    private static void configureTankFilters(GameTestHelper helper, BlockPos pos, List<FilterEntry> filters) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var properties = new MenuBase.Properties(new CapturingMenuHelper(), AllMenus.ELECTRIC_TANK.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = StorageMenus.electricTank(properties);
        menu.handleEventPacket(AllMenus.SET_MACHINE_CONFIG,
            SetMachineConfigPacket.builder().set(STORAGE_FILTERS, filters).get());
    }

    private static Optional<MESignalControllerSyncPacket.SignalInfo> visibleSignal(GameTestHelper helper,
        BlockPos pos, UUID machineId, String key, boolean write) {
        var player = makeServerPlayer(helper);
        var capture = new CapturingMenuHelper();
        var properties = new MenuBase.Properties(capture, AllMenus.ME_SIGNAL_CONTROLLER.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = new MESignalControllerMenu(properties);
        menu.broadcastChanges();
        return capture.packet(MESignalControllerSyncPacket.class)
            .flatMap(packet -> packet.signals().stream()
                .filter(signal -> signal.machineId().equals(machineId) && signal.key().equals(key) &&
                    signal.isWrite() == write)
                .findFirst());
    }

    private static ServerPlayer makeServerPlayer(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var profile = new GameProfile(UUID.randomUUID(), "me-storage-signal-test");
        return new ServerPlayer(server, helper.getLevel(), profile, ClientInformation.createDefault());
    }

    private static void seedChest(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        var provider = helper.getLevel().registryAccess();
        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        chest.deserializeNBT(provider, storageTag(StackHelper.serializeItemStack(provider, stack)));
    }

    private static void seedTank(GameTestHelper helper, BlockPos pos, FluidStack stack) {
        var provider = helper.getLevel().registryAccess();
        var tank = getContainer(helper.getBlockEntity(pos), ElectricTank.ID, ElectricTank.class);
        tank.deserializeNBT(provider, storageTag((CompoundTag) stack.save(provider, new CompoundTag())));
    }

    private static void insertItem(GameTestHelper helper, BlockPos pos, ItemStack stack) {
        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var remaining = handler.insertItem(0, stack, false);
        if (!remaining.isEmpty()) {
            throw new AssertionError("Test item insertion left " + remaining);
        }
    }

    private static void removeItem(GameTestHelper helper, BlockPos pos, Item item, int amount) {
        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var remaining = amount;
        for (var slot = 0; slot < handler.getSlots() && remaining > 0; slot++) {
            if (handler.getStackInSlot(slot).is(item)) {
                remaining -= handler.extractItem(slot, remaining, false).getCount();
            }
        }
        if (remaining != 0) {
            throw new AssertionError("Test item removal left " + remaining + " items");
        }
    }

    private static void fillTank(GameTestHelper helper, BlockPos pos, int amount) {
        var filled = FLUID_HANDLER.get(helper.getBlockEntity(pos)).fill(
            new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
        if (filled != amount) {
            throw new AssertionError("Test tank accepted " + filled + "/" + amount + " water");
        }
    }

    private static long chestAmount(GameTestHelper helper, BlockPos pos, Item item) {
        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var amount = 0L;
        for (var slot = 0; slot < handler.getSlots(); slot++) {
            var stack = handler.getStackInSlot(slot);
            if (stack.is(item)) {
                amount += stack.getCount();
            }
        }
        return amount;
    }

    private static long tankAmount(GameTestHelper helper, BlockPos pos, Fluid fluid) {
        var handler = FLUID_HANDLER.get(helper.getBlockEntity(pos));
        var amount = 0L;
        for (var tank = 0; tank < handler.getTanks(); tank++) {
            var stack = handler.getFluidInTank(tank);
            if (stack.is(fluid)) {
                amount += stack.getAmount();
            }
        }
        return amount;
    }

    private static void insertBoilerFuel(GameTestHelper helper, BlockPos pos) {
        var remaining = MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos))
            .insertItem(0, new ItemStack(Items.COAL_BLOCK), false);
        if (!remaining.isEmpty()) {
            throw new AssertionError("Boiler rejected coal block test fuel");
        }
    }

    private static void clearBoilerFuel(GameTestHelper helper, BlockPos pos) {
        var handler = MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var fuel = handler.getStackInSlot(0);
        if (!fuel.isEmpty()) {
            handler.extractItem(0, fuel.getCount(), false);
        }
    }

    private static boolean boilerStopped(GameTestHelper helper, BlockPos pos) {
        var state = helper.getBlockState(pos);
        return !state.getValue(MachineBlock.WORKING) &&
            MENU_ITEM_HANDLER.get(helper.getBlockEntity(pos)).getStackInSlot(0).getCount() > 0;
    }

    private static boolean assertRedstone(GameTestHelper helper, BlockPos pos, int expected) {
        var wirePos = pos.east();
        var state = helper.getBlockState(wirePos);
        if (state.getBlock() != Blocks.REDSTONE_WIRE ||
            state.getValue(RedStoneWireBlock.POWER) != expected) {
            helper.fail("Expected redstone power " + expected + " at " + wirePos + ", got " + state, pos);
            return false;
        }
        if (helper.getBlockState(pos.east(2)).getValue(BlockStateProperties.LIT) != (expected > 0)) {
            helper.fail("Redstone lamp did not match power " + expected, pos.east(2));
            return false;
        }
        return true;
    }

    private static boolean hasEntry(StorageSyncPacket packet, IStackKey key, long amount) {
        return packet.entries().stream().anyMatch(entry -> entry.key().equals(key) && entry.amount() == amount);
    }

    private static CompoundTag storageTag(CompoundTag... entries) {
        var tag = new CompoundTag();
        tag.putInt("version", 2);
        var list = new ListTag();
        for (var entry : entries) {
            list.add(entry);
        }
        tag.put("entries", list);
        return tag;
    }

    private static final class CapturingMenuHelper implements IMenuHelper {
        private final List<IPacket> packets = new ArrayList<>();

        @Override
        public <P extends IPacket> ISyncSlotScheduler<P> simpleScheduler(IPacketType<P> type,
            Supplier<P> factory) {
            return new ISyncSlotScheduler<>() {
                @Override
                public IPacketType<P> packetType() {
                    return type;
                }

                @Override
                public boolean shouldSend() {
                    return true;
                }

                @Override
                public P createPacket() {
                    return factory.get();
                }
            };
        }

        @Override
        public <P extends IPacket> void sendSyncPacket(ServerPlayer player, int containerId, int syncSlotId,
            IPacketType<P> type, P packet) {
            packets.add(packet);
        }

        @Override
        public <P extends IPacket> void sendEventPacket(int containerId, IPacketType<P> type, P packet) {}

        @Override
        public void requireMenuSyncPacket(IPacketType<?> type) {}

        @Override
        public void requireMenuEventPacket(IPacketType<?> type) {}

        private <P extends IPacket> Optional<P> packet(Class<P> type) {
            return packets.stream().filter(type::isInstance).map(type::cast).reduce((first, second) -> second);
        }
    }
}
