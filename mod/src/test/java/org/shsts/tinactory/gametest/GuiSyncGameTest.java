package org.shsts.tinactory.gametest;

import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.FilterEventPacket;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
import org.shsts.tinactory.content.gui.sync.MECraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftRequestSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.OpenTechPacket;
import org.shsts.tinactory.content.gui.sync.RenameEventPacket;
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.content.gui.sync.WorkbenchTransferEventPacket;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.plan.PlanSummary;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@GameTestHolder(TinactoryKeys.ID)
public final class GuiSyncGameTest {
    @GameTest
    public static void testGuiPacketRoundTripsAndSchedulers(GameTestHelper helper) {
        var diamond = new ItemStack(Items.DIAMOND);
        var key = StackHelper.ITEM_ADAPTER.keyOf(diamond);
        var registryAccess = helper.getLevel().registryAccess();

        var storageEvent = roundTrip(new StorageEventPacket(key, 13L, 1, true), StorageEventPacket::new,
            registryAccess);
        if (storageEvent.isEmpty() || !storageEvent.isItem() || storageEvent.amount() != 13 ||
            storageEvent.button() != 1 || !storageEvent.shiftPressed() || !storageEvent.item().is(Items.DIAMOND)) {
            helper.fail("Storage event packet did not preserve the item click payload");
            return;
        }
        var emptyStorageEvent = roundTrip(new StorageEventPacket(0), StorageEventPacket::new, registryAccess);
        if (!emptyStorageEvent.isEmpty() || emptyStorageEvent.amount() != 0) {
            helper.fail("Empty storage click packet did not preserve its empty state");
            return;
        }

        var storageSync = roundTrip(new StorageSyncPacket(List.of(new StorageEntry(key, 23))),
            StorageSyncPacket::new, registryAccess);
        if (storageSync.entries().size() != 1 || storageSync.entries().getFirst().amount() != 23) {
            helper.fail("Storage sync packet did not preserve its entries");
            return;
        }
        var filter = FilterEntry.fromItem(diamond);
        var filterEvent = roundTrip(new FilterEventPacket(false, 2, filter), FilterEventPacket::new,
            registryAccess);
        if (filterEvent.remove().isPresent() || filterEvent.append().type() != FilterEntry.Type.ITEM ||
            !filterEvent.append().asItem().is(Items.DIAMOND)) {
            helper.fail("Filter event packet did not preserve its appended filter");
            return;
        }
        var removedFilter = roundTrip(new FilterEventPacket(true, 1, FilterEntry.EMPTY), FilterEventPacket::new,
            registryAccess);
        if (removedFilter.remove().orElse(-1) != 1) {
            helper.fail("Filter event packet did not preserve its removal index");
            return;
        }

        var rename = roundTrip(new RenameEventPacket("Copper Label"), RenameEventPacket::new, registryAccess);
        var transfer = roundTrip(new WorkbenchTransferEventPacket(
            ResourceLocation.withDefaultNamespace("oak_planks"), true), WorkbenchTransferEventPacket::new,
            registryAccess);
        if (!rename.getName().equals("Copper Label") || !transfer.getRecipeId()
            .equals(ResourceLocation.withDefaultNamespace("oak_planks")) || !transfer.isMaxTransfer()) {
            helper.fail("Rename or workbench transfer packet did not preserve its payload");
            return;
        }

        var preview = roundTrip(MECraftEventPacket.preview(key, 8), MECraftEventPacket::new, registryAccess);
        var executeId = UUID.randomUUID();
        var execute = roundTrip(MECraftEventPacket.execute(executeId), MECraftEventPacket::new, registryAccess);
        var cancel = roundTrip(MECraftEventPacket.cancel(executeId), MECraftEventPacket::new, registryAccess);
        if (preview.action() != MECraftEventPacket.Action.PREVIEW || preview.quantity() != 8 ||
            !preview.target().equals(key) || execute.action() != MECraftEventPacket.Action.EXECUTE ||
            !executeId.equals(execute.cpuId()) || cancel.action() != MECraftEventPacket.Action.CANCEL ||
            !executeId.equals(cancel.cpuId())) {
            helper.fail("Autocraft event packet did not preserve its action payload");
            return;
        }
        var requestSync = roundTrip(new MECraftRequestSyncPacket(List.of(key)), MECraftRequestSyncPacket::new,
            registryAccess);
        if (!requestSync.equals(new MECraftRequestSyncPacket(List.of(key))) ||
            requestSync.hashCode() != new MECraftRequestSyncPacket(List.of(key)).hashCode()) {
            helper.fail("Autocraft request packet did not preserve its requestables");
            return;
        }
        var emptyPreview = roundTrip(MECraftPreviewSyncPacket.empty(), MECraftPreviewSyncPacket::new,
            registryAccess);
        if (emptyPreview.state() != MECraftPreviewSyncPacket.PreviewState.EMPTY ||
            !emptyPreview.summary().entries().isEmpty()) {
            helper.fail("Empty autocraft preview packet did not preserve its empty state");
            return;
        }
        var summary = new PlanSummary(Map.of(key, new PlanSummary.Entry(2, 1, 4)));
        var readyPreview = roundTrip(MECraftPreviewSyncPacket.ready(summary, 512),
            MECraftPreviewSyncPacket::new, registryAccess);
        var failedPreview = roundTrip(MECraftPreviewSyncPacket.failed(PlanError.missingPattern(key), summary),
            MECraftPreviewSyncPacket::new, registryAccess);
        if (readyPreview.state() != MECraftPreviewSyncPacket.PreviewState.PREVIEW_READY ||
            readyPreview.memoryUsage() != 512 || !readyPreview.summary().equals(summary) ||
            failedPreview.state() != MECraftPreviewSyncPacket.PreviewState.PREVIEW_FAILED ||
            failedPreview.error() == null || failedPreview.error().code() != PlanError.Code.MISSING_PATTERN ||
            !failedPreview.summary().equals(summary)) {
            helper.fail("Autocraft preview packets did not preserve ready and failed results");
            return;
        }

        var cpuId = UUID.randomUUID();
        var cpuStatus = new CpuStatusEntry(cpuId, JobState.IDLE, List.of(), 0, 0, ExecutionError.NONE, 2048, 0);
        var cpuInfo = new MECraftCpuSyncPacket.CpuInfo(cpuStatus, Component.literal("CPU"),
            new ItemStack(Items.CHEST));
        var cpuSync = roundTrip(new MECraftCpuSyncPacket(List.of(cpuInfo)), MECraftCpuSyncPacket::new,
            registryAccess);
        if (!cpuSync.equals(new MECraftCpuSyncPacket(List.of(cpuInfo))) ||
            cpuSync.hashCode() != new MECraftCpuSyncPacket(List.of(cpuInfo)).hashCode()) {
            helper.fail("Autocraft CPU sync packet did not preserve status equality");
            return;
        }

        var patternId = UUID.randomUUID();
        var pattern = new CraftPattern(patternId, List.of(new CraftAmount(key, 2)), List.of(
            new CraftAmount(StackHelper.ITEM_ADAPTER.keyOf(new ItemStack(Items.EMERALD)), 1)), List.of());
        var createPattern = roundTrip(MEPatternEventPacket.create(pattern), MEPatternEventPacket::new,
            registryAccess);
        var updatePattern = roundTrip(MEPatternEventPacket.update(patternId, pattern), MEPatternEventPacket::new,
            registryAccess);
        var deletePattern = roundTrip(MEPatternEventPacket.delete(patternId), MEPatternEventPacket::new,
            registryAccess);
        var patterns = roundTrip(new MEPatternSyncPacket(List.of(pattern)), MEPatternSyncPacket::new,
            registryAccess);
        if (createPattern.action() != MEPatternEventPacket.Action.CREATE || createPattern.pattern() == null ||
            updatePattern.action() != MEPatternEventPacket.Action.UPDATE ||
            !patternId.equals(updatePattern.patternUuid()) ||
            deletePattern.action() != MEPatternEventPacket.Action.DELETE ||
            deletePattern.pattern() != null || !patternId.equals(deletePattern.patternUuid()) ||
            !patterns.patterns().equals(List.of(pattern))) {
            helper.fail("Pattern event or sync packets did not preserve pattern payloads");
            return;
        }

        var portInfo = new LogisticWorkerSyncPacket.PortInfo(UUID.randomUUID(), 3,
            Component.literal("Assembler"), new ItemStack(Items.CRAFTING_TABLE), Component.literal("Input"));
        var workerSync = roundTrip(new LogisticWorkerSyncPacket(List.of(portInfo)), LogisticWorkerSyncPacket::new,
            registryAccess);
        if (workerSync.ports().size() != 1 || !workerSync.ports().iterator().next().getKey()
            .equals(new LogisticComponent.PortKey(portInfo.machineId(), 3))) {
            helper.fail("Logistic worker packet did not preserve its visible port key");
            return;
        }
        var signal = new MESignalControllerSyncPacket.SignalInfo(UUID.randomUUID(), Component.literal("Sensor"),
            new ItemStack(Items.COMPARATOR), "progress", true);
        var signalSync = roundTrip(new MESignalControllerSyncPacket(List.of(signal)),
            MESignalControllerSyncPacket::new, registryAccess);
        if (signalSync.signals().size() != 1 || !signalSync.signals().getFirst().key().equals("progress") ||
            !signalSync.signals().getFirst().isWrite()) {
            helper.fail("Signal controller packet did not preserve its signal entry");
            return;
        }

        var emptyOpen = roundTrip(OpenTechPacket.INSTANCE, () -> OpenTechPacket.INSTANCE, registryAccess);
        if (emptyOpen != OpenTechPacket.INSTANCE) {
            helper.fail("Open technology packet did not preserve its singleton value");
            return;
        }
        var revision = new long[] { 1 };
        var scheduler = new RevisionScheduler<>(AllMenus.WORKBENCH_TRANSFER, () -> revision[0],
            () -> new WorkbenchTransferEventPacket(ResourceLocation.withDefaultNamespace("stick"), false));
        if (!scheduler.shouldSend()) {
            helper.fail("Revision scheduler skipped its first update");
            return;
        }
        scheduler.createPacket();
        if (scheduler.shouldSend()) {
            helper.fail("Revision scheduler resent an unchanged revision");
            return;
        }
        revision[0]++;
        if (!scheduler.shouldSend()) {
            helper.fail("Revision scheduler missed a changed revision");
            return;
        }
        var active = new ActiveScheduler<>(AllMenus.RENAME, () -> new RenameEventPacket("Name"));
        if (!active.shouldSend() || !active.createPacket().getName().equals("Name") || active.shouldSend()) {
            helper.fail("Active scheduler did not clear activation after packet creation");
            return;
        }
        active.invokeUpdate();
        if (!active.shouldSend()) {
            helper.fail("Active scheduler did not reactivate after an update");
            return;
        }
        helper.succeed();
    }

    private static <P extends IPacket> P roundTrip(P packet, Supplier<P> factory,
        RegistryAccess registryAccess) {
        var buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), registryAccess);
        packet.serializeToBuf(buf);
        var result = factory.get();
        result.deserializeFromBuf(buf);
        buf.release();
        return result;
    }
}
