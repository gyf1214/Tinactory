package org.shsts.tinactory.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.StorageMenu;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.integration.logistics.StackHelper;
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

import static org.shsts.tinactory.api.logistics.IPort.empty;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@GameTestHolder(TinactoryKeys.ID)
public final class StorageMenuGameTest {
    @GameTest
    public static void testAggregateStorageSyncHandlesZeroStackLimit(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var diamond = new ItemStack(Items.DIAMOND);
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.randomUUID(), "storage-menu-test"), ClientInformation.createDefault());
        player.setGameMode(GameType.SURVIVAL);
        var menuHelper = new CapturingMenuHelper();
        var menu = new StorageMenu(new MenuBase.Properties(menuHelper, AllMenus.ELECTRIC_CHEST.get(), 0,
            player.getInventory(), blockEntity), new FixedItemPort(diamond), 0, empty(), 0);

        menu.broadcastChanges();

        var packet = menuHelper.packet(StorageSyncPacket.class);
        if (packet.isEmpty() || packet.get().entries().size() != 1 ||
            !packet.get().entries().getFirst().key().equals(StackHelper.ITEM_ADAPTER.keyOf(diamond)) ||
            packet.get().entries().getFirst().amount() != 1) {
            helper.fail("Zero-limit storage sync did not preserve the aggregate item entry", pos);
            return;
        }
        helper.succeed();
    }

    private static final class FixedItemPort implements IPort<ItemStack> {
        private final ItemStack stack;

        private FixedItemPort(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return stack;
        }

        @Override
        public ItemStack extract(ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(int limit, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public long getStorageAmount(ItemStack stack) {
            return this.stack.getCount();
        }

        @Override
        public List<ItemStack> getAllStorages() {
            return List.of(stack);
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, id));
    }

    private static final class CapturingMenuHelper implements IMenuHelper {
        private final List<IPacket> packets = new ArrayList<>();

        @Override
        public <P extends IPacket> ISyncSlotScheduler<P> simpleScheduler(IPacketType<P> type,
            Supplier<P> factory) {
            return new ISyncSlotScheduler<>() {
                private boolean shouldSend = true;

                @Override
                public IPacketType<P> packetType() {
                    return type;
                }

                @Override
                public boolean shouldSend() {
                    return shouldSend;
                }

                @Override
                public P createPacket() {
                    shouldSend = false;
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

        public <P extends IPacket> Optional<P> packet(Class<P> type) {
            return packets.stream().filter(type::isInstance).map(type::cast).findFirst();
        }
    }

    /*
    @GameTest
    public static void testLockedBlankClickInsertsReservedItem(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        MACHINE.get(blockEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, false).get());
        var diamond = new ItemStack(Items.DIAMOND);
        var key = StackHelper.ITEM_ADAPTER.keyOf(diamond);
        chest.setFilter(key);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(diamond.copy());

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (chest.getStorageAmount(diamond) != 1 || !menu.getCarried().isEmpty()) {
            helper.fail("Locked blank click did not insert an already reserved item", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testLeftClickDifferentEmptyFilterUsesNormalInsert(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var emeraldKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.EMERALD));
        chest.setFilter(emeraldKey);
        var diamond = new ItemStack(Items.DIAMOND);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(diamond.copy());

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(emeraldKey, 0, false));

        if (chest.getStorageAmount(diamond) != 1 || !chest.filters().contains(emeraldKey) ||
            !menu.getCarried().isEmpty()) {
            helper.fail("Left-click replaced an empty filter instead of using normal insertion", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testRightClickInvalidReplacementPreservesFilter(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var emeraldKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.EMERALD));
        chest.setFilter(emeraldKey);
        var diamond = new ItemStack(Items.DIAMOND);
        chest.insert(diamond.copy(), false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(diamond.copy());

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(emeraldKey, 1, false));

        if (!chest.filters().contains(emeraldKey) || chest.getStorageAmount(diamond) != 1 ||
            menu.getCarried().getCount() != 1) {
            helper.fail("Invalid right-click replacement cleared or mutated storage", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testRightClickReplacesEmptyFilter(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var emeraldKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.EMERALD));
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.DIAMOND));
        chest.setFilter(emeraldKey);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.DIAMOND));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(emeraldKey, 1, false));

        if (chest.filters().contains(emeraldKey) || !chest.filters().contains(diamondKey) ||
            menu.getCarried().getCount() != 1) {
            helper.fail("Right-click did not replace the empty filter atomically", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testChestFilterUsesFluidContainersItemKey(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        MACHINE.get(blockEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, false).get());
        var waterBucket = new ItemStack(Items.WATER_BUCKET);
        var itemKey = StackHelper.ITEM_ADAPTER.keyOf(waterBucket);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(waterBucket);

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (!chest.filters().contains(itemKey) || menu.getCarried().isEmpty()) {
            helper.fail("Electric Chest did not reserve a fluid container by item key", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTankFilterUsesFluidContainersContainedFluidKey(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var blockEntity = helper.getBlockEntity(pos);
        var tank = getContainer(blockEntity, ElectricTank.ID, ElectricTank.class);
        MACHINE.get(blockEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, false).get());
        var water = new FluidStack(Fluids.WATER, 1);
        var fluidKey = StackHelper.FLUID_ADAPTER.keyOf(water);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = tankMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (!tank.filters().contains(fluidKey) || menu.getCarried().isEmpty()) {
            helper.fail("Electric Tank did not reserve a fluid container by contained-fluid key", pos);
            return;
        }
        helper.succeed();
    }

    */

    @GameTest
    public static void testChestLeftClickFluidContainerPrefersItemInsert(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var waterBucket = new ItemStack(Items.WATER_BUCKET);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(waterBucket.copy());

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (chest.port().getStorageAmount(waterBucket) != 1 || !menu.getCarried().isEmpty()) {
            helper.fail("Chest left-click did not prefer item insertion for a fluid container", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTankLeftClickFluidContainerFallsBackToFluid(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var blockEntity = helper.getBlockEntity(pos);
        var tank = getContainer(blockEntity, ElectricTank.ID, ElectricTank.class);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = tankMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (tank.port().getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1000 ||
            !menu.getCarried().is(Items.BUCKET)) {
            helper.fail("Tank left-click did not fall back from item insertion to fluid insertion", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testShiftRightClickQuickMoves(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var diamonds = new ItemStack(Items.DIAMOND, 2);
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(diamonds);
        chest.port().insert(diamonds, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(diamondKey, 1, true));

        if (!menu.getCarried().isEmpty() || chest.port().getStorageAmount(diamonds) != 0 ||
            player.getInventory().countItem(Items.DIAMOND) != 2) {
            helper.fail("Shift-right-click did not quick move the storage entry", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testShiftLeftClickWithFluidContainerDoesNotQuickMove(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var diamond = new ItemStack(Items.DIAMOND);
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(diamond);
        chest.port().insert(diamond, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(diamondKey, 0, true));

        if (chest.port().getStorageAmount(diamond) != 1 ||
            chest.port().getStorageAmount(new ItemStack(Items.WATER_BUCKET)) != 1 ||
            !menu.getCarried().isEmpty() || player.getInventory().countItem(Items.DIAMOND) != 0) {
            helper.fail("Shift-left-click with a fluid container quick moved instead of inserting the item", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testLeftClickFluidContainerPrefersFluidTransfer(GameTestHelper helper) {
        var chestPos = new BlockPos(1, 1, 1);
        var tankPos = new BlockPos(2, 1, 1);
        helper.setBlock(chestPos, block("logistics/ulv/electric_chest"));
        helper.setBlock(tankPos, block("logistics/ulv/electric_tank"));
        var chest = getContainer(helper.getBlockEntity(chestPos), ElectricChest.ID, ElectricChest.class);
        var tank = getContainer(helper.getBlockEntity(tankPos), ElectricTank.ID, ElectricTank.class);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = dualStorageMenu(helper, chestPos, player, chest, tank);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(0));

        if (chest.port().getStorageAmount(new ItemStack(Items.WATER_BUCKET)) != 0 ||
            tank.port().getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1000 ||
            !menu.getCarried().is(Items.BUCKET)) {
            helper.fail("Left-click did not prefer fluid transfer for a fluid container", chestPos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testLeftClickEmptyFluidContainerDrainsFluidEntry(GameTestHelper helper) {
        var chestPos = new BlockPos(1, 1, 1);
        var tankPos = new BlockPos(2, 1, 1);
        helper.setBlock(chestPos, block("logistics/ulv/electric_chest"));
        helper.setBlock(tankPos, block("logistics/ulv/electric_tank"));
        var chest = getContainer(helper.getBlockEntity(chestPos), ElectricChest.ID, ElectricChest.class);
        var tank = getContainer(helper.getBlockEntity(tankPos), ElectricTank.ID, ElectricTank.class);
        var water = new FluidStack(Fluids.WATER, 1000);
        var waterKey = StackHelper.FLUID_ADAPTER.keyOf(water);
        tank.port().insert(water, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = dualStorageMenu(helper, chestPos, player, chest, tank);
        menu.setCarried(new ItemStack(Items.BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 0, false));

        if (chest.port().getStorageAmount(new ItemStack(Items.BUCKET)) != 0 ||
            tank.port().getStorageAmount(water) != 0 ||
            !menu.getCarried().is(Items.WATER_BUCKET)) {
            helper.fail("Left-click with an empty fluid container did not drain the clicked fluid entry", chestPos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testItemExtractionUsesClickedVirtualSlotAmount(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/mv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var diamond = new ItemStack(Items.DIAMOND);
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(diamond);
        chest.port().insert(new ItemStack(Items.DIAMOND, 64), false);
        chest.port().insert(new ItemStack(Items.DIAMOND, 64), false);
        chest.port().insert(new ItemStack(Items.DIAMOND, 4), false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(diamondKey, 4, 0, false));

        if (chest.port().getStorageAmount(diamond) != 128 || menu.getCarried().getCount() != 4) {
            helper.fail("Item extraction did not use the clicked virtual slot amount", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testRightClickItemExtractionHalvesClickedVirtualSlotAmount(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/mv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var diamond = new ItemStack(Items.DIAMOND);
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(diamond);
        chest.port().insert(new ItemStack(Items.DIAMOND, 64), false);
        chest.port().insert(new ItemStack(Items.DIAMOND, 64), false);
        chest.port().insert(new ItemStack(Items.DIAMOND, 4), false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(diamondKey, 4, 1, false));

        if (chest.port().getStorageAmount(diamond) != 130 || menu.getCarried().getCount() != 2) {
            helper.fail("Right-click item extraction did not halve the clicked virtual slot amount", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testFluidDrainUsesClickedVirtualSlotAmount(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var tank = getContainer(helper.getBlockEntity(pos), ElectricTank.ID, ElectricTank.class);
        var water = new FluidStack(Fluids.WATER, 20000);
        var waterKey = StackHelper.FLUID_ADAPTER.keyOf(water);
        tank.port().insert(water, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = tankMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.BUCKET, 16));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 4000, 0, false));

        if (tank.port().getStorageAmount(water) != 16000 || !menu.getCarried().is(Items.BUCKET) ||
            menu.getCarried().getCount() != 12 || player.getInventory().countItem(Items.WATER_BUCKET) != 4) {
            helper.fail("Fluid drain did not use the clicked virtual slot amount", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testShiftClickOnTankDoesNotTransferFluid(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var tank = getContainer(helper.getBlockEntity(pos), ElectricTank.ID, ElectricTank.class);
        var water = new FluidStack(Fluids.WATER, 1);
        var waterKey = StackHelper.FLUID_ADAPTER.keyOf(water);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = tankMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 0, true));

        if (tank.port().getStorageAmount(water) != 0 || !menu.getCarried().is(Items.WATER_BUCKET)) {
            helper.fail("Shift-click with a fluid container transferred fluid", pos);
            return;
        }
        helper.succeed();
    }

    /*

    @GameTest
    public static void testTankEmptyFilterNoFluidKeyOnlyRightClickClears(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var blockEntity = helper.getBlockEntity(pos);
        var tank = getContainer(blockEntity, ElectricTank.ID, ElectricTank.class);
        var water = new FluidStack(Fluids.WATER, 1);
        var waterKey = StackHelper.FLUID_ADAPTER.keyOf(water);
        tank.setFilter(waterKey);
        MACHINE.get(blockEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, false).get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = tankMenu(helper, pos, player);
        menu.setCarried(new ItemStack(Items.BUCKET));

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 0, false));

        if (!tank.filters().contains(waterKey)) {
            helper.fail("Tank left-click with an empty container cleared the filter", pos);
            return;
        }
        menu.setCarried(new ItemStack(Items.DIAMOND));
        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 0, false));

        if (!tank.filters().contains(waterKey)) {
            helper.fail("Tank left-click with a non-container item cleared the filter", pos);
            return;
        }
        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(waterKey, 1, false));

        if (tank.filters().contains(waterKey)) {
            helper.fail("Tank right-click with a non-container item did not clear the filter", pos);
            return;
        }
        helper.succeed();
    }

    */

    private static StorageMenu chestMenu(GameTestHelper helper, BlockPos pos, Player player) {
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var properties = new MenuBase.Properties(MENU_HELPER, AllMenus.ELECTRIC_CHEST.get(), 0,
            player.getInventory(), blockEntity);
        return new StorageMenu(properties, chest.port(), chest.stackLimit(), empty(), 0);
    }

    private static StorageMenu tankMenu(GameTestHelper helper, BlockPos pos, Player player) {
        var blockEntity = helper.getBlockEntity(pos);
        var tank = getContainer(blockEntity, ElectricTank.ID, ElectricTank.class);
        var properties = new MenuBase.Properties(MENU_HELPER, AllMenus.ELECTRIC_TANK.get(), 0,
            player.getInventory(), blockEntity);
        return new StorageMenu(properties, empty(), 0, tank.port(), tank.stackLimit());
    }

    private static StorageMenu dualStorageMenu(GameTestHelper helper, BlockPos pos, Player player,
        ElectricChest chest, ElectricTank tank) {
        var properties = new MenuBase.Properties(MENU_HELPER, AllMenus.ELECTRIC_CHEST.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        return new StorageMenu(properties, chest.port(), chest.stackLimit(), tank.port(), tank.stackLimit());
    }

    private static final IMenuHelper MENU_HELPER = new IMenuHelper() {
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
                    return false;
                }

                @Override
                public P createPacket() {
                    return factory.get();
                }
            };
        }

        @Override
        public <P extends IPacket> void sendSyncPacket(ServerPlayer player, int containerId, int syncSlotId,
            IPacketType<P> type, P packet) {}

        @Override
        public <P extends IPacket> void sendEventPacket(int containerId, IPacketType<P> type, P packet) {}

        @Override
        public void requireMenuSyncPacket(IPacketType<?> type) {}

        @Override
        public void requireMenuEventPacket(IPacketType<?> type) {}
    };

}
