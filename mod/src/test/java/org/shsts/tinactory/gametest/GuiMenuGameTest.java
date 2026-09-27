package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.content.gui.BoilerMenu;
import org.shsts.tinactory.content.gui.MachineMenu;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.sync.SlotEventPacket;
import org.shsts.tinactory.integration.gui.InventoryMenu;
import org.shsts.tinactory.integration.gui.LayoutMenu;
import org.shsts.tinactory.integration.gui.ProcessingMenu;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.gui.IMenuHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.function.Supplier;

import static org.shsts.tinactory.AllCapabilities.MACHINE;

@GameTestHolder(TinactoryKeys.ID)
public final class GuiMenuGameTest {
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
                    return true;
                }

                @Override
                public P createPacket() {
                    return factory.get();
                }
            };
        }

        @Override
        public <P extends IPacket> void sendSyncPacket(ServerPlayer player,
            int containerId, int syncSlotId, IPacketType<P> type, P packet) {}

        @Override
        public <P extends IPacket> void sendEventPacket(int containerId, IPacketType<P> type, P packet) {}

        @Override
        public void requireMenuSyncPacket(IPacketType<?> type) {}

        @Override
        public void requireMenuEventPacket(IPacketType<?> type) {}
    };

    @GameTest
    public static void testQuickMoveMovesPlayerItemIntoMenuSlot(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 3));

        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);

        if (!menu.target.getItem(0).is(Items.DIAMOND) || menu.target.getItem(0).getCount() != 3 ||
            !player.getInventory().getItem(0).isEmpty()) {
            helper.fail("Quick-move did not transfer the hotbar stack into the menu slot", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveRetainsMenuRemainderWhenPlayerInventoryIsFull(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        for (var i = 0; i < player.getInventory().getContainerSize(); i++) {
            player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 63));
        menu.target.setItem(0, new ItemStack(Items.EMERALD, 5));

        menu.clicked(36, 0, ClickType.QUICK_MOVE, player);

        if (player.getInventory().getItem(0).getCount() != 64 || !menu.target.getItem(0).is(Items.EMERALD) ||
            menu.target.getItem(0).getCount() != 4) {
            helper.fail("Quick-move did not retain the part that could not fit", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveReturnsMenuItemToPlayerInventory(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.target.setItem(0, new ItemStack(Items.EMERALD, 5));

        menu.clicked(36, 0, ClickType.QUICK_MOVE, player);

        if (!player.getInventory().contains(new ItemStack(Items.EMERALD, 5)) ||
            !menu.target.getItem(0).isEmpty()) {
            helper.fail("Quick-move did not return the menu stack to the player inventory", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveKeepsPlayerItemWhenMenuSlotRejectsIt(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new RejectingInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 2));

        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);

        if (!player.getInventory().getItem(0).is(Items.DIAMOND) ||
            player.getInventory().getItem(0).getCount() != 2 || !menu.target.getItem(0).isEmpty()) {
            helper.fail("Quick-move changed inventory when the menu slot rejected the stack", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveKeepsPlayerItemWhenMenuSlotCannotMergeIt(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 2));
        menu.target.setItem(0, new ItemStack(Items.EMERALD, 1));

        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);

        if (player.getInventory().getItem(0).getCount() != 2 || !menu.target.getItem(0).is(Items.EMERALD)) {
            helper.fail("Quick-move changed stacks when the occupied menu slot could not accept them", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveMergesIntoPartiallyFilledMenuSlot(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 4));
        menu.target.setItem(0, new ItemStack(Items.DIAMOND, 62));

        menu.clicked(0, 0, ClickType.QUICK_MOVE, player);

        if (menu.target.getItem(0).getCount() != 64 || player.getInventory().getItem(0).getCount() != 2) {
            helper.fail("Quick-move did not merge only the available space", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveHonorsMenuSlotPickupPermission(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new UnpickableInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.target.setItem(0, new ItemStack(Items.DIAMOND));

        menu.clicked(36, 0, ClickType.QUICK_MOVE, player);

        if (!menu.target.getItem(0).is(Items.DIAMOND) || !player.getInventory().isEmpty()) {
            helper.fail("Quick-move ignored the menu slot pickup permission", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveMergesMenuStackIntoPlayerInventory(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND, 62));
        menu.target.setItem(0, new ItemStack(Items.DIAMOND, 4));

        menu.clicked(36, 0, ClickType.QUICK_MOVE, player);

        if (player.getInventory().getItem(0).getCount() != 64 ||
            !player.getInventory().contains(new ItemStack(Items.DIAMOND, 2)) || !menu.target.getItem(0).isEmpty()) {
            helper.fail("Quick-move did not merge into the partial player stack", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testQuickMoveLeavesMenuItemWhenPlayerInventoryHasNoSpace(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TestInventoryMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        for (var i = 0; i < player.getInventory().getContainerSize(); i++) {
            player.getInventory().setItem(i, new ItemStack(Items.COBBLESTONE, 64));
        }
        menu.target.setItem(0, new ItemStack(Items.DIAMOND, 2));

        menu.clicked(36, 0, ClickType.QUICK_MOVE, player);

        if (!menu.target.getItem(0).is(Items.DIAMOND) || menu.target.getItem(0).getCount() != 2) {
            helper.fail("Quick-move changed the menu stack without player inventory space", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testLayoutAndProcessingHelpersHandleNonMachineBlockEntity(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var properties = new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos));
        var menu = new TestLayoutMenu(properties);

        if (menu.layout() != Layout.EMPTY || !LayoutMenu.getProcessor(helper.getBlockEntity(pos)).isEmpty() ||
            !ProcessingMenu.getTitle(helper.getBlockEntity(pos)).equals(Component.empty()) ||
            !ProcessingMenu.portLabel(PortType.ITEM, 2).getString().contains("2")) {
            helper.fail("Generic layout or processing menu helpers returned unexpected state", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testMachineProcessingMenuUsesRegisteredMachine(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        var machineMeta = AllBlockEntities.getMachine("ore_analyzer");
        var state = machineMeta.block(Voltage.HV).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST);
        helper.setBlock(pos, state);
        var absolutePos = helper.absolutePos(pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
        var menu = new MachineMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.PROCESSING_MACHINE.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.setCarried(new ItemStack(Items.DIAMOND));
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(100, 0));
        if (!menu.getCarried().is(Items.DIAMOND)) {
            helper.fail("Invalid machine port event changed the carried stack", pos);
            return;
        }
        menu.setCarried(new ItemStack(Items.COBBLESTONE, 2));
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(0, 1));
        if (menu.getCarried().getCount() != 1) {
            helper.fail("Right-click machine input did not insert one item", pos);
            return;
        }
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(0, 0));
        if (!menu.getCarried().isEmpty()) {
            helper.fail("Left-click machine input did not insert the remaining stack", pos);
            return;
        }
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(0, 0));
        if (!menu.getCarried().is(Items.COBBLESTONE) || menu.getCarried().getCount() != 2) {
            helper.fail("Empty-hand machine port click did not extract its stored items", pos);
            return;
        }
        menu.setCarried(new ItemStack(Items.COBBLESTONE, 4));
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(0, 0));
        menu.setCarried(ItemStack.EMPTY);
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(0, 1));
        var itemPort = MACHINE.get(helper.getBlockEntity(pos)).container().orElseThrow()
            .getPort(0, ContainerAccess.MENU).asItem();
        if (!menu.getCarried().is(Items.COBBLESTONE) || menu.getCarried().getCount() != 2 ||
            itemPort.getStorageAmount(new ItemStack(Items.COBBLESTONE)) != 2) {
            helper.fail("Right-click machine port extraction did not take half the available items", pos);
            return;
        }

        if (menu.stillValid(player) || menu.machineBlock().orElse(null) != state.getBlock() ||
            ProcessingMenu.getTitle(helper.getBlockEntity(pos)).equals(Component.empty())) {
            helper.fail("Unowned machine menu access or machine metadata was unexpected", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testBoilerMenuBuildsMachineSlotsAndChecksAccess(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID,
            "machine/boiler/high"));
        var state = block.defaultBlockState().setValue(MachineBlock.IO_FACING, Direction.EAST);
        helper.setBlock(pos, state);
        var absolutePos = helper.absolutePos(pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.getLevel().getBlockState(absolutePos).useItemOn(ItemStack.EMPTY, helper.getLevel(), player,
            InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH,
                absolutePos, true));
        var menu = new BoilerMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.PROCESSING_MACHINE.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.broadcastChanges();

        if (menu.stillValid(player) || menu.slots.size() <= 36 || menu.machineBlock().isEmpty()) {
            helper.fail("Boiler menu did not expose machine slots or enforce ownership", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testMachineMenuFillsFluidPortFromBucket(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        var block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID,
            "machine/boiler/high"));
        var state = block.defaultBlockState().setValue(MachineBlock.IO_FACING, Direction.EAST);
        helper.setBlock(pos, state);
        var absolutePos = helper.absolutePos(pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.getLevel().getBlockState(absolutePos).useItemOn(ItemStack.EMPTY, helper.getLevel(), player,
            InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH,
                absolutePos, true));
        var menu = new BoilerMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.PROCESSING_MACHINE.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        var machine = MACHINE.get(helper.getBlockEntity(pos));
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));

        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(1, 0));

        var waterPort = machine.container().orElseThrow().getPort(1, ContainerAccess.MENU).asFluid();
        if (!menu.getCarried().is(Items.BUCKET) ||
            waterPort.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1_000) {
            helper.fail("Machine fluid click did not transfer bucket water into the boiler", pos);
            return;
        }
        menu.setCarried(new ItemStack(Items.DIAMOND));
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(1, 0));
        if (!menu.getCarried().is(Items.DIAMOND) ||
            waterPort.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 1_000) {
            helper.fail("Unsupported fluid container changed the carried stack or boiler input", pos);
            return;
        }

        var outputPort = machine.container().orElseThrow().getPort(2, ContainerAccess.INTERNAL).asFluid();
        outputPort.insert(new FluidStack(Fluids.WATER, 1_000), false);
        menu.setCarried(new ItemStack(Items.BUCKET));
        menu.handleEventPacket(AllMenus.PORT_CLICK, new SlotEventPacket(2, 0));
        if (!menu.getCarried().is(Items.WATER_BUCKET) ||
            outputPort.getStorageAmount(new FluidStack(Fluids.WATER, 1)) != 0) {
            helper.fail("Machine fluid port did not drain stored water into the empty bucket", pos);
            return;
        }

        var fluidSlot = menu.layout().slots.stream()
            .filter(slot -> slot.type().portType == PortType.FLUID).findFirst().orElse(null);
        if (fluidSlot == null) {
            helper.fail("Boiler layout has no fluid slot for the layout menu click event", pos);
            return;
        }
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        menu.handleEventPacket(AllMenus.FLUID_SLOT_CLICK, new SlotEventPacket(fluidSlot.index(), 0));
        if (!menu.getCarried().is(Items.BUCKET)) {
            helper.fail("Fluid slot click event did not consume the water bucket", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testMachineMenuSimpleConfigUsesRegisteredMachine(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        var state = AllBlockEntities.getMachine("battery_box").block(Voltage.HV).defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.EAST);
        helper.setBlock(pos, state);
        var absolutePos = helper.absolutePos(pos);
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        state.useItemOn(ItemStack.EMPTY, helper.getLevel(), player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
        var menu = MachineMenu.simpleConfig(new MenuBase.Properties(MENU_HELPER, AllMenus.BATTERY_BOX.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.handleEventPacket(AllMenus.SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().get());
        menu.broadcastChanges();

        if (menu.stillValid(player) || menu.layout() == Layout.EMPTY || menu.slots.size() < 36) {
            helper.fail("Simple machine menu did not create a layout or enforce access", pos);
            return;
        }
        helper.succeed();
    }

    private static final class TestInventoryMenu extends InventoryMenu {
        private final SimpleContainer target = new SimpleContainer(1);

        private TestInventoryMenu(Properties properties) {
            super(properties, 0);
            addSlot(new Slot(target, 0, 0, 0));
        }
    }

    private static final class RejectingInventoryMenu extends InventoryMenu {
        private final SimpleContainer target = new SimpleContainer(1);

        private RejectingInventoryMenu(Properties properties) {
            super(properties, 0);
            addSlot(new Slot(target, 0, 0, 0) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }
            });
        }
    }

    private static final class TestLayoutMenu extends LayoutMenu {
        private TestLayoutMenu(Properties properties) {
            super(properties, Layout.EMPTY, 0);
            addLayoutSlots(layout);
            addProgressBar();
            addFluidSlots();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }

    private static final class UnpickableInventoryMenu extends InventoryMenu {
        private final SimpleContainer target = new SimpleContainer(1);

        private UnpickableInventoryMenu(Properties properties) {
            super(properties, 0);
            addSlot(new Slot(target, 0, 0, 0) {
                @Override
                public boolean mayPickup(Player player) {
                    return false;
                }
            });
        }
    }
}
