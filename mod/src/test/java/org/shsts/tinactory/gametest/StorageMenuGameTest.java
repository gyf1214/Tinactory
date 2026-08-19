package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
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
import org.shsts.tinactory.content.gui.ElectricStorageMenu;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricStorage;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.IMenuHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.function.Supplier;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@GameTestHolder(TinactoryKeys.ID)
public final class StorageMenuGameTest {
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

        if (chest.getStorageAmount(waterBucket) != 1 || !menu.getCarried().isEmpty()) {
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

        if (tank.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1000 ||
            !menu.getCarried().is(Items.BUCKET)) {
            helper.fail("Tank left-click did not fall back from item insertion to fluid insertion", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testShiftRightClickUsesNormalRightClick(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var blockEntity = helper.getBlockEntity(pos);
        var chest = getContainer(blockEntity, ElectricChest.ID, ElectricChest.class);
        var diamonds = new ItemStack(Items.DIAMOND, 2);
        var diamondKey = StackHelper.ITEM_ADAPTER.keyOf(diamonds);
        chest.insert(diamonds, false);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = chestMenu(helper, pos, player);

        menu.handleEventPacket(AllMenus.STORAGE_SLOT, new StorageEventPacket(diamondKey, 1, true));

        if (!menu.getCarried().is(Items.DIAMOND) || menu.getCarried().getCount() != 1 ||
            player.getInventory().countItem(Items.DIAMOND) != 0) {
            helper.fail("Shift-right-click used quick move instead of normal right-click", pos);
            return;
        }
        helper.succeed();
    }

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

    private static ElectricStorageMenu chestMenu(GameTestHelper helper, BlockPos pos, Player player) {
        return ElectricStorageMenu.chest(new MenuBase.Properties(MENU_HELPER, AllMenus.ELECTRIC_CHEST.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
    }

    private static ElectricStorageMenu tankMenu(GameTestHelper helper, BlockPos pos, Player player) {
        return ElectricStorageMenu.tank(new MenuBase.Properties(MENU_HELPER, AllMenus.ELECTRIC_TANK.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
    }

    private static Block block(String id) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, id));
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
