package org.shsts.tinactory.gametest;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.TechMenu;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinycorelib.api.gui.IMenuHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.UUID;
import java.util.function.Supplier;

@GameTestHolder(TinactoryKeys.ID)
public final class TechMenuGameTest {
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
    public static void testRenameEventUpdatesAndClearsResultName(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new TechMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.TECH_MENU.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.setRenameActive(true);
        player.getInventory().setItem(0, new ItemStack(Items.DIAMOND));
        menu.clicked(2, 0, ClickType.PICKUP, player);
        menu.clicked(0, 0, ClickType.PICKUP, player);
        menu.handleEventPacket(AllMenus.RENAME, new RenameEventPacket("Forged"));

        if (!menu.getSlot(1).getItem().getHoverName().getString().equals("Forged")) {
            helper.fail("Rename event did not update the result slot", pos);
            return;
        }
        menu.handleEventPacket(AllMenus.RENAME, new RenameEventPacket(" "));
        if (!menu.getSlot(1).getItem().getHoverName().getString()
            .equals(new ItemStack(Items.DIAMOND).getHoverName().getString())) {
            helper.fail("Blank rename did not clear the custom name", pos);
            return;
        }
        menu.clicked(1, 0, ClickType.PICKUP, player);
        if (!menu.getCarried().is(Items.DIAMOND) || !menu.getSlot(0).getItem().isEmpty()) {
            helper.fail("Picking up the rename result did not consume the rename input", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testClosingRenameMenuReturnsUnusedInput(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = new ServerPlayer(helper.getLevel().getServer(), helper.getLevel(),
            new GameProfile(UUID.randomUUID(), "tech-menu-test"), ClientInformation.createDefault());
        player.setGameMode(GameType.SURVIVAL);
        player.connection = new ServerGamePacketListenerImpl(helper.getLevel().getServer(),
            new Connection(PacketFlow.SERVERBOUND), player,
            CommonListenerCookie.createInitial(player.getGameProfile(), false));
        var menu = new TechMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.TECH_MENU.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        menu.setRenameActive(true);
        player.getInventory().setItem(0, new ItemStack(Items.EMERALD, 2));
        menu.clicked(2, 0, ClickType.PICKUP, player);
        menu.clicked(0, 0, ClickType.PICKUP, player);

        menu.removed(player);

        if (!player.getInventory().contains(new ItemStack(Items.EMERALD, 2)) || menu.getSlot(0).hasItem()) {
            helper.fail("Closing the rename menu did not return its unused item", pos);
            return;
        }
        helper.succeed();
    }
}
