package org.shsts.tinactory.unit.autocraft;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftRequestablesSyncPacket;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.service.AutocraftPreviewResult;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftTerminalMenuContractTest {
    @Test
    void menuShouldExposeRequestablesAndAvailableCpuSlots() {
        var requestables = new AutocraftRequestablesSyncPacket(List.of(
            new CraftKey(CraftKey.Type.ITEM, "minecraft:iron_ingot", "")));
        var cpus = new AutocraftCpuSyncPacket(List.of(
            new AutocraftCpuSyncPacket.Row(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                true,
                "Idle",
                "N/A",
                "",
                false)));

        var requestablesDecoded = roundTrip(requestables, new AutocraftRequestablesSyncPacket());
        var cpusDecoded = roundTrip(cpus, new AutocraftCpuSyncPacket());

        assertEquals(1, requestablesDecoded.requestables().size());
        assertEquals("minecraft:iron_ingot", requestablesDecoded.requestables().get(0).id());
        assertEquals(1, cpusDecoded.rows().stream()
            .filter(AutocraftCpuSyncPacket.Row::available)
            .count());
    }

    @Test
    void previewActionShouldPublishFailedPreviewToSyncPacket() {
        var preview = AutocraftPreviewSyncPacket.preview(new AutocraftPreviewResult(
            null,
            AutocraftPreviewResult.Code.PLAN_FAILED));

        var decoded = roundTrip(preview, new AutocraftPreviewSyncPacket());

        assertNull(decoded.targets());
        assertEquals(AutocraftPreviewResult.Code.PLAN_FAILED, decoded.previewError());
        assertNull(decoded.executeError());
    }

    @Test
    void cancelActionShouldClearPreviewState() {
        var cleared = AutocraftPreviewSyncPacket.cancel();

        var decoded = roundTrip(cleared, new AutocraftPreviewSyncPacket());

        assertNull(decoded.targets());
        assertNull(decoded.previewError());
        assertNull(decoded.executeError());
    }

    @Test
    void cpuStatusSyncShouldRoundTripCpuRows() {
        var cpu = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var src = new AutocraftCpuSyncPacket(List.of(
            new AutocraftCpuSyncPacket.Row(cpu, false,
                "4x minecraft:iron_ingot", "2/3", "MISSING_INPUT_BUFFER", true)));

        var decoded = roundTrip(src, new AutocraftCpuSyncPacket());

        assertEquals(1, decoded.rows().size());
        assertEquals(cpu, decoded.rows().get(0).cpuId());
        assertEquals("2/3", decoded.rows().get(0).currentStep());
        assertEquals("MISSING_INPUT_BUFFER", decoded.rows().get(0).blockedReason());
        assertTrue(decoded.rows().get(0).cancellable());
    }

    private static <T extends org.shsts.tinycorelib.api.network.IPacket> T roundTrip(T src, T dst) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        src.serializeToBuf(buf);
        dst.deserializeFromBuf(buf);
        return dst;
    }
}
