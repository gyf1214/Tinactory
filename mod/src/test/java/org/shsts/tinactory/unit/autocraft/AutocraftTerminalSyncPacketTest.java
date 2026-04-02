package org.shsts.tinactory.unit.autocraft;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinactory.unit.fixture.TestIngredientKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutocraftTerminalSyncPacketTest {
    @Test
    void cpuSyncPacketShouldRoundTripCanonicalCpuStatusEntries() {
        var packet = new AutocraftCpuSyncPacket(TestIngredientKey.CODEC, List.of(
            new AutocraftTerminalService.CpuStatusEntry(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                false,
                List.of(new CraftAmount(TestIngredientKey.item("minecraft:iron_ingot", ""), 3)),
                JobState.BLOCKED,
                ExecutionPhase.FLUSHING,
                1,
                2,
                ExecutionError.FLUSH_BACKPRESSURE,
                true)));
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        var decoded = new AutocraftCpuSyncPacket(TestIngredientKey.CODEC);

        packet.serializeToBuf(buf);
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.entries(), decoded.entries());
    }

    @Test
    void previewSyncPacketShouldRoundTripAllPreviewStates() {
        var key = TestIngredientKey.item("minecraft:iron_ingot", "");
        var empty = roundTrip(AutocraftPreviewSyncPacket.empty(TestIngredientKey.CODEC));
        var ready = roundTrip(
            AutocraftPreviewSyncPacket.ready(TestIngredientKey.CODEC, List.of(new CraftAmount(key, 3))));
        var failed = roundTrip(
            AutocraftPreviewSyncPacket.failed(TestIngredientKey.CODEC, PlanError.missingPattern(key)));

        assertEquals(AutocraftPreviewSyncPacket.PreviewState.EMPTY, empty.state());
        assertEquals(AutocraftPreviewSyncPacket.PreviewState.PREVIEW_READY, ready.state());
        assertEquals(List.of(new CraftAmount(key, 3)), ready.targets());
        assertEquals(AutocraftPreviewSyncPacket.PreviewState.PREVIEW_FAILED, failed.state());
        assertEquals(PlanError.Code.MISSING_PATTERN, failed.error().code());
        assertEquals(key, failed.error().targetKey());
    }

    private static AutocraftPreviewSyncPacket roundTrip(AutocraftPreviewSyncPacket packet) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        var decoded = new AutocraftPreviewSyncPacket(TestIngredientKey.CODEC);

        packet.serializeToBuf(buf);
        decoded.deserializeFromBuf(buf);
        return decoded;
    }
}
