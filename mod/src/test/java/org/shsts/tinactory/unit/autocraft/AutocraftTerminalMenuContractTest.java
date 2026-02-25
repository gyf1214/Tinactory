package org.shsts.tinactory.unit.autocraft;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalPreviewSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalRequestablesSyncSlot;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableEntry;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableKey;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AutocraftTerminalMenuContractTest {
    @Test
    void menuShouldExposeRequestablesAndAvailableCpuSlots() {
        var requestables = new AutocraftTerminalRequestablesSyncSlot(List.of(
            new AutocraftRequestableEntry(
                new AutocraftRequestableKey(CraftKey.Type.ITEM, "minecraft:iron_ingot", ""), 2L)));
        var cpus = new AutocraftTerminalCpuSyncSlot(List.of(
            UUID.fromString("11111111-1111-1111-1111-111111111111")));

        var requestablesDecoded = roundTrip(requestables, new AutocraftTerminalRequestablesSyncSlot());
        var cpusDecoded = roundTrip(cpus, new AutocraftTerminalCpuSyncSlot());

        assertEquals(1, requestablesDecoded.requestables().size());
        assertEquals("minecraft:iron_ingot", requestablesDecoded.requestables().get(0).key().id());
        assertEquals(1, cpusDecoded.availableCpus().size());
    }

    @Test
    void previewActionShouldPublishPreviewStateToSyncSlot() {
        var planId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var preview = new AutocraftTerminalPreviewSyncSlot(
            planId,
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 4)),
            AutocraftPreviewErrorCode.PREVIEW_FAILED,
            AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS);

        var decoded = roundTrip(preview, new AutocraftTerminalPreviewSyncSlot());

        assertEquals(planId, decoded.planId());
        assertEquals(1, decoded.summaryOutputs().size());
        assertEquals(AutocraftPreviewErrorCode.PREVIEW_FAILED, decoded.previewError());
        assertEquals(AutocraftExecuteErrorCode.PREFLIGHT_MISSING_INPUTS, decoded.executeError());
    }

    @Test
    void cancelActionShouldClearPreviewState() {
        var cleared = new AutocraftTerminalPreviewSyncSlot(null, List.of(), null, null);

        var decoded = roundTrip(cleared, new AutocraftTerminalPreviewSyncSlot());

        assertNull(decoded.planId());
        assertEquals(0, decoded.summaryOutputs().size());
        assertNull(decoded.previewError());
        assertNull(decoded.executeError());
    }

    private static <T extends org.shsts.tinycorelib.api.network.IPacket> T roundTrip(T src, T dst) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        src.serializeToBuf(buf);
        dst.deserializeFromBuf(buf);
        return dst;
    }
}
