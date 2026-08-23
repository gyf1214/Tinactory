package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.items.IItemHandler;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricStorage;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.StackHelper;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER_ITEM;
import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL_ITEM;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@GameTestHolder(TinactoryKeys.ID)
public final class PersistenceGameTest {
    @GameTest
    public static void testStorageCellCapacities(GameTestHelper helper) {
        var capacities = new long[] {4L << 20, 16L << 20, 64L << 20, 256L << 20};
        var patternLimits = new long[] {16L, 64L, 256L, 1024L};
        for (var index = 0; index < capacities.length; index++) {
            var tier = "tier_" + (index + 1);
            var itemCell = new ItemStack(item("logistics/item_storage_cell/" + tier));
            var fluidCell = new ItemStack(item("logistics/fluid_storage_cell/" + tier));
            var patternCell = new ItemStack(item("logistics/pattern_cell/" + tier));
            require(helper, BYTES_PROVIDER_ITEM.tryGet(itemCell).orElseThrow().bytesCapacity() == capacities[index] &&
                BYTES_PROVIDER_ITEM.tryGet(fluidCell).orElseThrow().bytesCapacity() == capacities[index] &&
                PATTERN_CELL_ITEM.tryGet(patternCell).orElseThrow().bytesCapacity() == capacities[index] &&
                capacities[index] / TinactoryConfig.CONFIG.bytesPerPattern.get() == patternLimits[index],
                "Storage tier " + tier + " did not expose its expected capacity or pattern limit", BlockPos.ZERO);
        }
        helper.succeed();
    }

    @GameTest
    public static void testStorageCellsMigrateLegacyIds(GameTestHelper helper) {
        var provider = helper.getLevel().registryAccess();
        var families = new String[] {"component/storage_component", "logistics/item_storage_cell",
            "logistics/fluid_storage_cell", "logistics/pattern_cell"};
        var legacyNames = new String[] {"1m", "4m", "16m", "64m"};
        for (var family : families) {
            for (var index = 0; index < legacyNames.length; index++) {
                var currentId = ResourceLocation.fromNamespaceAndPath("tinactory", family + "/tier_" + (index + 1));
                var legacyId = ResourceLocation.fromNamespaceAndPath("tinactory", family + "/" + legacyNames[index]);
                var current = BuiltInRegistries.ITEM.get(currentId);
                var stack = new ItemStack(current);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("migration-" + family + '-' + index));
                var serialized = (CompoundTag) stack.save(provider);
                serialized.putString("id", legacyId.toString());
                var restored = ItemStack.parseOptional(provider, serialized);
                require(helper, restored.is(current) && restored.has(DataComponents.CUSTOM_NAME),
                    "Legacy " + legacyId + " did not resolve to " + currentId + " with components", BlockPos.ZERO);
                require(helper, BuiltInRegistries.ITEM.get(legacyId) == current &&
                    BuiltInRegistries.ITEM.getKey(current).equals(currentId),
                    "Legacy " + legacyId + " was registered instead of aliased to " + currentId, BlockPos.ZERO);
            }
        }
        helper.succeed();
    }

    @GameTest
    public static void testIdleBoilerHasNoProcessingInfo(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("machine/boiler/low"));
        var boiler = (IBoiler) PROCESSOR.get(helper.getBlockEntity(pos));
        if (!boiler.getAllInfo().isEmpty()) {
            helper.fail("Idle boiler reported processing info", pos);
            return;
        }
        helper.succeed();
    }

    @GameTest
    public static void testElectricStorageIsUnlockedByDefault(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        require(helper, chest.isUnlocked(), "Electric Chest was not unlocked by default", pos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestMapStorage(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var destinationPos = new BlockPos(3, 1, 1);
        var block = block("logistics/ulv/electric_chest");
        helper.setBlock(sourcePos, block);
        var sourceEntity = helper.getBlockEntity(sourcePos);
        var chest = getContainer(sourceEntity, ElectricChest.ID, ElectricChest.class);
        var provider = helper.getLevel().registryAccess();
        chest.deserializeNBT(provider, storageTag(provider,
            entryTag(provider, new ItemStack(Items.DIAMOND), 65, false),
            entryTag(provider, new ItemStack(Items.EMERALD), 0, true)));
        MACHINE.get(sourceEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, false).get());

        var handler = ITEM_HANDLER.get(sourceEntity);
        require(helper, handler.getSlots() == 54, "ULV Electric Chest did not expose 54 virtual slots", sourcePos);
        require(helper, chest.amountSignal() == 1,
            "Electric Chest amount signal did not count unfiltered stored content", sourcePos);
        require(helper, handler.getStackInSlot(0).is(Items.DIAMOND) &&
            handler.getStackInSlot(0).getCount() == 64,
            "Electric Chest did not split its first deterministic virtual stack", sourcePos);
        require(helper, handler.getStackInSlot(1).is(Items.DIAMOND) &&
            handler.getStackInSlot(1).getCount() == 1,
            "Electric Chest did not split its second deterministic virtual stack", sourcePos);
        var filterSlot = findItemFilter(handler, new ItemStack(Items.EMERALD));
        require(helper, filterSlot >= 0 && !handler.isItemValid(filterSlot, new ItemStack(Items.GOLD_INGOT)),
            "Electric Chest filter did not reserve an exact-key virtual slot", sourcePos);
        require(helper, handler.insertItem(3, new ItemStack(Items.GOLD_INGOT), false).getCount() == 1,
            "Locked Electric Chest accepted a new unfiltered type", sourcePos);
        require(helper, handler.insertItem(1, new ItemStack(Items.DIAMOND), false).isEmpty(),
            "Locked Electric Chest rejected an existing type", sourcePos);

        MACHINE.get(sourceEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, true).set(ElectricStorage.VOID_KEY, true).get());
        var oversized = StackHelper.copyWithCount(new ItemStack(Items.GOLD_INGOT), 65);
        require(helper, handler.insertItem(3, oversized, false).isEmpty() &&
            handler.getStackInSlot(3).getCount() == 64,
            "Electric Chest void mode did not accept eligible overflow", sourcePos);

        var persisted = chest.serializeNBT(provider);
        require(helper, persisted.getInt("version") == 1 && persisted.getList("entries", 10).size() == 3,
            "Electric Chest did not write versioned map entries", sourcePos);

        helper.setBlock(destinationPos, block);
        var restoredEntity = helper.getBlockEntity(destinationPos);
        getContainer(restoredEntity, ElectricChest.ID, ElectricChest.class).deserializeNBT(provider, persisted);
        var restored = ITEM_HANDLER.get(restoredEntity);
        require(helper, restored.getStackInSlot(0).is(Items.DIAMOND) &&
            findItemFilter(restored, new ItemStack(Items.EMERALD)) >= 0,
            "Electric Chest map-format round trip changed content or filters", destinationPos);
        helper.destroyBlock(destinationPos);
        helper.assertItemEntityPresent(Items.DIAMOND, destinationPos, 2);
        helper.succeed();
    }

    @GameTest
    public static void testElectricTankMapStorage(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var destinationPos = new BlockPos(3, 1, 1);
        var block = block("logistics/ulv/electric_tank");
        helper.setBlock(sourcePos, block);
        var sourceEntity = helper.getBlockEntity(sourcePos);
        var tank = getContainer(sourceEntity, ElectricTank.ID, ElectricTank.class);
        var provider = helper.getLevel().registryAccess();
        tank.deserializeNBT(provider, storageTag(provider,
            entryTag(provider, new FluidStack(Fluids.LAVA, 1), 16001, false),
            entryTag(provider, new FluidStack(Fluids.WATER, 1), 0, true)));

        var handler = FLUID_HANDLER.get(sourceEntity);
        require(helper, handler.getTanks() == 54, "ULV Electric Tank did not expose 54 virtual tanks", sourcePos);
        require(helper, tank.amountSignal() == 1,
            "Electric Tank amount signal did not count unfiltered stored content", sourcePos);
        require(helper, handler.getFluidInTank(0).is(Fluids.LAVA) &&
            handler.getFluidInTank(0).getAmount() == 16000,
            "Electric Tank did not split its first deterministic virtual tank", sourcePos);
        require(helper, handler.getFluidInTank(1).is(Fluids.LAVA) &&
            handler.getFluidInTank(1).getAmount() == 1,
            "Electric Tank did not split its second deterministic virtual tank", sourcePos);
        var filterTank = findFluidFilter(handler, new FluidStack(Fluids.WATER, 1));
        require(helper, filterTank >= 0 &&
            !handler.isFluidValid(filterTank, new FluidStack(Fluids.LAVA, 1)),
            "Electric Tank filter did not reserve an exact-key virtual tank", sourcePos);
        require(helper, handler.fill(new FluidStack(Fluids.WATER, 1000),
            IFluidHandler.FluidAction.EXECUTE) == 1000,
            "Locked Electric Tank rejected a filtered type", sourcePos);

        MACHINE.get(sourceEntity).setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.UNLOCK_KEY, true).set(ElectricStorage.VOID_KEY, true).get());
        require(helper, handler.fill(new FluidStack(Fluids.WATER, 1_000_000),
            IFluidHandler.FluidAction.EXECUTE) == 1_000_000,
            "Electric Tank void mode did not accept eligible overflow", sourcePos);

        var persisted = tank.serializeNBT(provider);
        require(helper, persisted.getInt("version") == 1 && persisted.getList("entries", 10).size() == 2,
            "Electric Tank did not write versioned map entries", sourcePos);

        helper.setBlock(destinationPos, block);
        var restoredEntity = helper.getBlockEntity(destinationPos);
        getContainer(restoredEntity, ElectricTank.ID, ElectricTank.class).deserializeNBT(provider, persisted);
        var restored = FLUID_HANDLER.get(restoredEntity);
        restored.drain(new FluidStack(Fluids.WATER, Integer.MAX_VALUE), IFluidHandler.FluidAction.EXECUTE);
        require(helper, restored.getFluidInTank(0).is(Fluids.LAVA) &&
            findFluidFilter(restored, new FluidStack(Fluids.WATER, 1)) >= 0,
            "Electric Tank map-format round trip changed content or filters", destinationPos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestMigratesLegacyItemsAndFilters(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var provider = helper.getLevel().registryAccess();
        var legacy = new CompoundTag();
        var itemHandler = new CompoundTag();
        var items = new ListTag();
        var diamond = StackHelper.serializeItemStack(provider, new ItemStack(Items.DIAMOND, 32));
        diamond.putInt("Slot", 0);
        items.add(diamond);
        itemHandler.putInt("Size", 8);
        itemHandler.put("Items", items);
        legacy.put("items", itemHandler);

        var filters = new ListTag();
        addLegacyItemFilter(provider, filters, new ItemStack(Items.DIAMOND), 0);
        var slot = 1;
        for (var item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR || item == Items.DIAMOND) {
                continue;
            }
            addLegacyItemFilter(provider, filters, new ItemStack(item), slot++);
            if (slot == 55) {
                break;
            }
        }
        legacy.put("filters", filters);

        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        chest.deserializeNBT(provider, legacy);
        var migrated = chest.serializeNBT(provider);
        require(helper, migrated.getList("entries", 10).size() == 54,
            "Electric Chest did not discard only the overflowing legacy filter", pos);
        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var diamondSlot = findItemContent(handler, new ItemStack(Items.DIAMOND));
        require(helper, diamondSlot >= 0 && handler.getStackInSlot(diamondSlot).getCount() == 32,
            "Electric Chest did not migrate legacy item content", pos);
        handler.extractItem(diamondSlot, 32, false);
        require(helper, findItemFilter(handler, new ItemStack(Items.DIAMOND)) >= 0,
            "Electric Chest did not merge its legacy item and filter reservation", pos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricTankMigratesLegacyTanksAndFilters(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_tank"));
        var provider = helper.getLevel().registryAccess();
        var legacy = new CompoundTag();
        var tankHandler = new CompoundTag();
        var tanks = new ListTag();
        var lava = (CompoundTag) new FluidStack(Fluids.LAVA, 4000).save(provider, new CompoundTag());
        lava.putInt("Tank", 0);
        tanks.add(lava);
        tankHandler.putInt("Size", 2);
        tankHandler.put("Tanks", tanks);
        legacy.put("tanks", tankHandler);
        var filters = new ListTag();
        var water = (CompoundTag) new FluidStack(Fluids.WATER, 1).save(provider, new CompoundTag());
        water.putInt("Slot", 1);
        filters.add(water);
        legacy.put("filters", filters);

        var tank = getContainer(helper.getBlockEntity(pos), ElectricTank.ID, ElectricTank.class);
        tank.deserializeNBT(provider, legacy);
        var handler = FLUID_HANDLER.get(helper.getBlockEntity(pos));
        require(helper, handler.getFluidInTank(0).is(Fluids.LAVA) &&
            handler.getFluidInTank(0).getAmount() == 4000,
            "Electric Tank did not migrate legacy fluid content", pos);
        require(helper, findFluidFilter(handler, new FluidStack(Fluids.WATER, 1)) >= 0,
            "Electric Tank did not migrate its legacy filter reservation", pos);
        helper.succeed();
    }

    private static CompoundTag storageTag(HolderLookup.Provider provider, CompoundTag... entries) {
        var tag = new CompoundTag();
        tag.putInt("version", 1);
        var list = new ListTag();
        for (var entry : entries) {
            list.add(entry);
        }
        tag.put("entries", list);
        return tag;
    }

    private static CompoundTag entryTag(HolderLookup.Provider provider, ItemStack stack, long amount,
        boolean isFilter) {
        return entryTag(provider, StackHelper.ITEM_ADAPTER.keyOf(stack), amount, isFilter);
    }

    private static CompoundTag entryTag(HolderLookup.Provider provider, FluidStack stack, long amount,
        boolean isFilter) {
        return entryTag(provider, StackHelper.FLUID_ADAPTER.keyOf(stack), amount, isFilter);
    }

    private static CompoundTag entryTag(HolderLookup.Provider provider, IStackKey key, long amount,
        boolean isFilter) {
        var tag = new CompoundTag();
        tag.put("key", CodecHelper.encodeTag(provider, StackHelper.KEY_CODEC, key));
        tag.putLong("amount", amount);
        tag.putBoolean("isFilter", isFilter);
        return tag;
    }

    private static void addLegacyItemFilter(HolderLookup.Provider provider, ListTag filters,
        ItemStack stack, int slot) {
        var tag = (CompoundTag) stack.save(provider, new CompoundTag());
        tag.putInt("Slot", slot);
        filters.add(tag);
    }

    private static int findItemFilter(IItemHandler handler, ItemStack expected) {
        for (var slot = 0; slot < handler.getSlots(); slot++) {
            if (handler.getStackInSlot(slot).isEmpty() && handler.isItemValid(slot, expected)) {
                return slot;
            }
        }
        return -1;
    }

    private static int findItemContent(IItemHandler handler, ItemStack expected) {
        for (var slot = 0; slot < handler.getSlots(); slot++) {
            if (ItemStack.isSameItemSameComponents(handler.getStackInSlot(slot), expected)) {
                return slot;
            }
        }
        return -1;
    }

    private static int findFluidFilter(IFluidHandler handler, FluidStack expected) {
        for (var tank = 0; tank < handler.getTanks(); tank++) {
            if (handler.getFluidInTank(tank).isEmpty() && handler.isFluidValid(tank, expected)) {
                return tank;
            }
        }
        return -1;
    }

    private static void require(GameTestHelper helper, boolean condition, String message, BlockPos pos) {
        if (!condition) {
            helper.fail(message, pos);
        }
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("tinactory", path));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("tinactory", path));
    }
}
