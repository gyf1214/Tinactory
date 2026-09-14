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
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.logistics.ElectricChest;
import org.shsts.tinactory.content.logistics.ElectricStorage;
import org.shsts.tinactory.content.logistics.ElectricTank;
import org.shsts.tinactory.content.machine.IBoiler;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.Arrays;

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
        var capacities = new long[]{4L << 20, 16L << 20, 64L << 20, 256L << 20};
        var patternLimits = new long[]{16L, 64L, 256L, 1024L};
        for (var index = 0; index < capacities.length; index++) {
            var tier = "tier_" + (index + 1);
            var itemCell = new ItemStack(item("logistics/item_storage_cell/" + tier));
            var fluidCell = new ItemStack(item("logistics/fluid_storage_cell/" + tier));
            var patternCell = new ItemStack(item("logistics/pattern_cell/" + tier));
            require(helper,
                BYTES_PROVIDER_ITEM.tryGet(itemCell).orElseThrow().bytesCapacity() == capacities[index] &&
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
        var families = new String[]{
            "component/storage_component", "logistics/item_storage_cell",
            "logistics/fluid_storage_cell", "logistics/pattern_cell"
        };
        var legacyNames = new String[]{"1m", "4m", "16m", "64m"};
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
    public static void testElectricChestMapStorage(GameTestHelper helper) {
        var sourcePos = new BlockPos(1, 1, 1);
        var destinationPos = new BlockPos(3, 1, 1);
        var block = block("logistics/ulv/electric_chest");
        helper.setBlock(sourcePos, block);
        var sourceEntity = helper.getBlockEntity(sourcePos);
        var chest = getContainer(sourceEntity, ElectricChest.ID, ElectricChest.class);
        var provider = helper.getLevel().registryAccess();
        chest.deserializeNBT(provider, storageTag(
            StackHelper.serializeItemStack(provider, new ItemStack(Items.DIAMOND, 65))));

        var handler = ITEM_HANDLER.get(sourceEntity);
        require(helper, handler.getSlots() == 54, "ULV Electric Chest did not expose 54 virtual slots", sourcePos);
        require(helper, chest.amountSignal() == 1,
            "Electric Chest amount signal did not count unfiltered stored content", sourcePos);
        require(helper, handler.getStackInSlot(0).is(Items.DIAMOND) && handler.getStackInSlot(0).getCount() == 64,
            "Electric Chest did not split its first deterministic virtual stack", sourcePos);
        require(helper, handler.getStackInSlot(1).is(Items.DIAMOND) && handler.getStackInSlot(1).getCount() == 1,
            "Electric Chest did not split its second deterministic virtual stack", sourcePos);

        var persisted = chest.serializeNBT(provider);
        require(helper, persisted.getInt("version") == 2 && persisted.getList("entries", 10).size() == 1,
            "Electric Chest did not write versioned map entries", sourcePos);

        helper.setBlock(destinationPos, block);
        var restoredEntity = helper.getBlockEntity(destinationPos);
        getContainer(restoredEntity, ElectricChest.ID, ElectricChest.class).deserializeNBT(provider, persisted);
        var restored = ITEM_HANDLER.get(restoredEntity);
        require(helper, restored.getStackInSlot(0).is(Items.DIAMOND) &&
                restored.getStackInSlot(0).getCount() == 64 && restored.getStackInSlot(1).getCount() == 1,
            "Electric Chest map-format round trip changed content", destinationPos);
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
        tank.deserializeNBT(provider, storageTag(
            fluidStorageTag(provider, new FluidStack(Fluids.LAVA, 16001))));

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

        var persisted = tank.serializeNBT(provider);
        require(helper, persisted.getInt("version") == 2 && persisted.getList("entries", 10).size() == 1,
            "Electric Tank did not write versioned map entries", sourcePos);

        helper.setBlock(destinationPos, block);
        var restoredEntity = helper.getBlockEntity(destinationPos);
        getContainer(restoredEntity, ElectricTank.ID, ElectricTank.class).deserializeNBT(provider, persisted);
        var restored = FLUID_HANDLER.get(restoredEntity);
        require(helper, restored.getFluidInTank(0).is(Fluids.LAVA) &&
                restored.getFluidInTank(0).getAmount() == 16000 &&
                restored.getFluidInTank(1).getAmount() == 1,
            "Electric Tank map-format round trip changed content", destinationPos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestClampsMapEntryToVirtualCapacity(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var provider = helper.getLevel().registryAccess();
        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        chest.deserializeNBT(provider, storageTag(
            StackHelper.serializeItemStack(provider, new ItemStack(Items.DIAMOND, 54 * 64 + 1))));
        var persisted = chest.serializeNBT(provider);
        var entry = persisted.getList("entries", 10).getCompound(0);
        require(helper, entry.getInt("count") == 54 * 64,
            "Electric Chest did not clamp a map entry to its virtual capacity", pos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestExposesPartialVirtualStackAmount(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var provider = helper.getLevel().registryAccess();
        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        chest.deserializeNBT(provider, storageTag(
            StackHelper.serializeItemStack(provider, new ItemStack(Items.DIAMOND, 32))));

        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        require(helper, handler.getStackInSlot(0).is(Items.DIAMOND) && handler.getStackInSlot(0).getCount() == 32,
            "Electric Chest did not expose the stored partial stack amount", pos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricChestKeepsDistinctComponentStacksSeparate(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var provider = helper.getLevel().registryAccess();
        var plain = new ItemStack(Items.DIAMOND);
        var named = new ItemStack(Items.DIAMOND);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("named"));
        var chest = getContainer(helper.getBlockEntity(pos), ElectricChest.ID, ElectricChest.class);
        chest.deserializeNBT(provider, storageTag(
            StackHelper.serializeItemStack(provider, plain), StackHelper.serializeItemStack(provider, named)));

        var handler = ITEM_HANDLER.get(helper.getBlockEntity(pos));
        var first = handler.getStackInSlot(0);
        var second = handler.getStackInSlot(1);
        require(helper, !first.isEmpty() && !second.isEmpty() &&
                first.has(DataComponents.CUSTOM_NAME) != second.has(DataComponents.CUSTOM_NAME),
            "Electric Chest merged distinct component-bearing item identities", pos);
        helper.succeed();
    }

    @GameTest
    public static void testElectricStorageUpdatesConfigBeforeNetworkRegistration(GameTestHelper helper) {
        var pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, block("logistics/ulv/electric_chest"));
        var machine = MACHINE.get(helper.getBlockEntity(pos));

        machine.setConfig(SetMachineConfigPacket.builder()
            .set(ElectricStorage.PRIORITY_KEY, 3).get());

        helper.succeed();
    }

    private static CompoundTag storageTag(CompoundTag... entries) {
        var tag = new CompoundTag();
        tag.putInt("version", 2);
        var list = new ListTag();
        list.addAll(Arrays.asList(entries));
        tag.put("entries", list);
        return tag;
    }

    private static CompoundTag fluidStorageTag(HolderLookup.Provider provider, FluidStack stack) {
        return (CompoundTag) stack.save(provider, new CompoundTag());
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
