package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.WorkbenchTransferResult;
import org.shsts.tinactory.content.gui.sync.WorkbenchTransferEventPacket;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.gui.IMenuHelper;
import org.shsts.tinycorelib.api.gui.ISyncSlotScheduler;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.network.IPacket;
import org.shsts.tinycorelib.api.network.IPacketType;

import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.CORE;

@GameTestHolder(TinactoryKeys.ID)
public final class WorkbenchTransferGameTest {
    private static final ResourceLocation STICK_RECIPE = ResourceLocation.fromNamespaceAndPath("tinactory",
        "tool_crafting/minecraft/stick");

    @GameTest
    public static void testMissingTransferDoesNotMutateSlots(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new WorkbenchMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
        var recipe = recipe(menu);
        menu.toolSlots().getFirst().set(new ItemStack(BuiltInRegistries.ITEM.get(
            ResourceLocation.fromNamespaceAndPath("tinactory", "tool/saw/test"))));

        var result = menu.planTransfer(recipe, false);

        if (result.code() != WorkbenchTransferResult.Code.MISSING_INPUT ||
            !result.missingIndexes().contains(1)) {
            helper.fail("Missing transfer did not report the first material slot");
            return;
        }
        menu.transfer(recipe, false);
        if (menu.getSlot(0).hasItem()) {
            helper.fail("Missing transfer mutated the player inventory");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTransferEventUsesAuthoritativeRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new WorkbenchMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));

        menu.handleEventPacket(AllMenus.WORKBENCH_TRANSFER, new WorkbenchTransferEventPacket(STICK_RECIPE, false));

        if (menu.getSlot(0).hasItem()) {
            helper.fail("Rejected event mutated the player inventory");
            return;
        }
        helper.succeed();
    }

    private static ToolRecipe recipe(WorkbenchMenu menu) {
        return CORE.recipeManager(menu.world()).byLoc(AllRecipes.TOOL_CRAFTING, STICK_RECIPE)
            .orElseThrow().get();
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
