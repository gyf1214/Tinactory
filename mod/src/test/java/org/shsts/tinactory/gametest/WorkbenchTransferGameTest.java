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
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.AllRecipes;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.gui.WorkbenchTransferResult;
import org.shsts.tinactory.content.gui.sync.WorkbenchTransferEventPacket;
import org.shsts.tinactory.content.machine.Workbench;
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
    private static final ResourceLocation VANILLA_STICK_RECIPE = ResourceLocation.withDefaultNamespace("stick");
    private static final ResourceLocation OAK_PLANKS_RECIPE = ResourceLocation.withDefaultNamespace("oak_planks");

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
    public static void testMissingTransferReportsToolsAndMaterials(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = new WorkbenchMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));

        var result = menu.planTransfer(recipe(menu), false);

        if (result.code() != WorkbenchTransferResult.Code.MISSING_INPUT ||
            !result.missingIndexes().contains(1) || !result.missingIndexes().contains(10)) {
            helper.fail("Missing transfer did not report both material and tool slots");
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

    @GameTest
    public static void testTransferEventFallsBackToVanillaCrafting(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.playerSlots().getFirst().set(new ItemStack(Items.OAK_LOG));

        menu.handleEventPacket(AllMenus.WORKBENCH_TRANSFER,
            new WorkbenchTransferEventPacket(OAK_PLANKS_RECIPE, false));

        if (!menu.materialSlots().get(4).getItem().is(Items.OAK_LOG)) {
            helper.fail("Vanilla recipe event did not use the authoritative crafting recipe manager");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testCraftingShapelessRecipeConsumesOneIngredient(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.materialSlots().getFirst().set(new ItemStack(Items.OAK_LOG, 2));
        var workbench = Workbench.get(helper.getBlockEntity(pos));
        var result = workbench.getResult();

        if (!result.is(Items.OAK_PLANKS) || result.getCount() != 2) {
            helper.fail("Workbench did not find the oak planks shapeless recipe");
            return;
        }
        workbench.onTake(player, result.copy());
        if (menu.materialSlots().getFirst().getItem().getCount() != 1) {
            helper.fail("Workbench consumed more than one shapeless ingredient");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testCraftsToolRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        var saw = testSaw();
        menu.toolSlots().getFirst().set(saw);
        menu.materialSlots().get(4).set(new ItemStack(Items.OAK_PLANKS));
        menu.materialSlots().get(7).set(new ItemStack(Items.OAK_PLANKS));
        var workbench = Workbench.get(helper.getBlockEntity(pos));
        var result = workbench.getResult();

        if (!result.is(Items.STICK) || result.getCount() != 4) {
            helper.fail("Workbench did not find the stick tool recipe");
            return;
        }
        workbench.onTake(player, result.copy());
        if (menu.materialSlots().get(4).hasItem() || menu.materialSlots().get(7).hasItem()) {
            helper.fail("Workbench did not consume tool recipe materials");
            return;
        }
        if (saw.getDamageValue() != 1) {
            helper.fail("Workbench did not damage the required tool");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testCraftsShapedRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.materialSlots().get(4).set(new ItemStack(Items.OAK_PLANKS));
        menu.materialSlots().get(7).set(new ItemStack(Items.OAK_PLANKS));
        var workbench = Workbench.get(helper.getBlockEntity(pos));
        var result = workbench.getResult();

        if (!result.is(Items.STICK) || result.getCount() != 2) {
            helper.fail("Workbench did not find the vanilla shaped stick recipe");
            return;
        }
        workbench.onTake(player, result.copy());
        if (menu.materialSlots().get(4).hasItem() || menu.materialSlots().get(7).hasItem()) {
            helper.fail("Workbench did not consume shaped recipe materials");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testCraftsShapelessRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.materialSlots().get(8).set(new ItemStack(Items.OAK_LOG));
        var workbench = Workbench.get(helper.getBlockEntity(pos));
        var result = workbench.getResult();

        if (!result.is(Items.OAK_PLANKS) || result.getCount() != 2) {
            helper.fail("Workbench did not find the vanilla shapeless oak planks recipe");
            return;
        }
        workbench.onTake(player, result.copy());
        if (menu.materialSlots().get(8).hasItem()) {
            helper.fail("Workbench did not consume the shapeless recipe ingredient");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTransfersVanillaShapedRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.playerSlots().getFirst().set(new ItemStack(Items.OAK_PLANKS, 2));

        var result = menu.planTransfer(craftingRecipe(menu, VANILLA_STICK_RECIPE), false);

        if (result.code() != WorkbenchTransferResult.Code.SUCCESS) {
            helper.fail("Vanilla shaped transfer did not plan successfully");
            return;
        }
        menu.transfer(craftingRecipe(menu, VANILLA_STICK_RECIPE), false);
        if (!menu.materialSlots().getFirst().getItem().is(Items.OAK_PLANKS) ||
            !menu.materialSlots().get(3).getItem().is(Items.OAK_PLANKS)) {
            helper.fail("Vanilla shaped transfer did not use the JEI grid positions");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTransferUsesGridMaterialsAndStowsRemainder(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.materialSlots().getFirst().set(new ItemStack(Items.OAK_PLANKS, 3));

        menu.transfer(craftingRecipe(menu, VANILLA_STICK_RECIPE), false);

        if (!menu.materialSlots().getFirst().getItem().is(Items.OAK_PLANKS) ||
            !menu.materialSlots().get(3).getItem().is(Items.OAK_PLANKS)) {
            helper.fail("Transfer did not use materials already in the crafting grid");
            return;
        }
        var remainder = menu.playerSlots().getFirst().getItem();
        if (!remainder.is(Items.OAK_PLANKS) || remainder.getCount() != 1) {
            helper.fail("Transfer did not stow only the unused grid materials");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testTransfersVanillaShapelessRecipe(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);
        menu.playerSlots().getFirst().set(new ItemStack(Items.OAK_LOG));

        menu.transfer(craftingRecipe(menu, OAK_PLANKS_RECIPE), false);

        if (!menu.materialSlots().get(4).getItem().is(Items.OAK_LOG)) {
            helper.fail("Vanilla shapeless transfer did not use JEI's centered position");
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testVanillaTransferReportsMissingInputWithoutMutation(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, AllBlockEntities.WORKBENCH.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var menu = menu(helper, pos, player);

        var result = menu.planTransfer(craftingRecipe(menu, VANILLA_STICK_RECIPE), false);

        if (result.code() != WorkbenchTransferResult.Code.MISSING_INPUT ||
            !result.missingIndexes().contains(1) || !result.missingIndexes().contains(4)) {
            helper.fail("Vanilla transfer did not report missing shaped inputs");
            return;
        }
        menu.transfer(craftingRecipe(menu, VANILLA_STICK_RECIPE), false);
        if (menu.materialSlots().stream().anyMatch(slot -> slot.hasItem())) {
            helper.fail("Failed vanilla transfer mutated the material grid");
            return;
        }
        helper.succeed();
    }

    private static ToolRecipe recipe(WorkbenchMenu menu) {
        return CORE.recipeManager(menu.world()).byLoc(AllRecipes.TOOL_CRAFTING, STICK_RECIPE)
            .orElseThrow().get();
    }

    private static CraftingRecipe craftingRecipe(WorkbenchMenu menu, ResourceLocation id) {
        RecipeHolder<?> holder = menu.world().getRecipeManager().byKey(id).orElseThrow();
        if (holder.value() instanceof CraftingRecipe recipe) {
            return recipe;
        }
        throw new IllegalArgumentException("Not a crafting recipe: " + id);
    }

    private static WorkbenchMenu menu(GameTestHelper helper, BlockPos pos, Player player) {
        return new WorkbenchMenu(new MenuBase.Properties(MENU_HELPER, AllMenus.WORKBENCH.get(), 0,
            player.getInventory(), helper.getBlockEntity(pos)));
    }

    private static ItemStack testSaw() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tinactory",
            "tool/saw/test")));
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
