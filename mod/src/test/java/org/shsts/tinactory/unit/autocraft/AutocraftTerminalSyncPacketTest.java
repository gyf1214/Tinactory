package org.shsts.tinactory.unit.autocraft;

import com.mojang.serialization.Codec;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftEventPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.core.autocraft.api.ExecutionPhase;
import org.shsts.tinactory.core.autocraft.api.JobState;
import org.shsts.tinactory.core.autocraft.exec.ExecutionError;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.plan.PlanError;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;
import org.shsts.tinactory.unit.fixture.TestIngredientKey;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutocraftTerminalSyncPacketTest {
    @BeforeAll
    static void setUpBootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void cpuSyncPacketShouldRoundTripCanonicalCpuStatusEntries() {
        var packet = new AutocraftCpuSyncPacket(TestIngredientKey.CODEC, List.of(
            new CpuStatusEntry(
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

    @Test
    void eventPacketShouldRoundTripPreviewWithoutCpuSelection() {
        var key = ItemPortAdapter.INSTANCE.keyOf(new ItemStack(Items.IRON_INGOT));
        var packet = roundTrip(AutocraftEventPacket.preview(key, 3));

        assertEquals(AutocraftEventPacket.Action.PREVIEW, packet.action());
        assertEquals(key, packet.target());
        assertEquals(3L, packet.quantity());
        assertEquals(null, packet.cpuId());
    }

    @Test
    void eventPacketShouldKeepCpuSelectionOnlyForCpuActions() {
        var cpuId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        var execute = roundTrip(AutocraftEventPacket.execute(cpuId));
        var cancelCpu = roundTrip(AutocraftEventPacket.cancelCpu(cpuId));

        assertEquals(AutocraftEventPacket.Action.EXECUTE, execute.action());
        assertEquals(cpuId, execute.cpuId());
        assertEquals(AutocraftEventPacket.Action.CANCEL_CPU, cancelCpu.action());
        assertEquals(cpuId, cancelCpu.cpuId());
    }

    @Test
    void ingredientKeyCodecsShouldKeepStringIdField() {
        assertEncodedId(
            castCodec(ItemPortAdapter.keyCodec()),
            ItemPortAdapter.INSTANCE.keyOf(new ItemStack(Items.IRON_INGOT)),
            "minecraft:iron_ingot"
        );
        assertEncodedId(
            castCodec(FluidPortAdapter.keyCodec()),
            FluidPortAdapter.INSTANCE.keyOf(new FluidStack(Fluids.WATER, 250)),
            "minecraft:water"
        );
    }

    private static AutocraftPreviewSyncPacket roundTrip(AutocraftPreviewSyncPacket packet) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        var decoded = new AutocraftPreviewSyncPacket(TestIngredientKey.CODEC);

        packet.serializeToBuf(buf);
        decoded.deserializeFromBuf(buf);
        return decoded;
    }

    private static AutocraftEventPacket roundTrip(AutocraftEventPacket packet) {
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        var decoded = new AutocraftEventPacket();

        packet.serializeToBuf(buf);
        decoded.deserializeFromBuf(buf);
        return decoded;
    }

    private static void assertEncodedId(Codec<IIngredientKey> codec, IIngredientKey key, String expectedId) {
        var tag = (CompoundTag) CodecHelper.encodeTag(codec, key);

        assertEquals(expectedId, tag.getString("id"));
        assertEquals(key, CodecHelper.parseTag(codec, tag));
    }

    @SuppressWarnings("unchecked")
    private static Codec<IIngredientKey> castCodec(Codec<? extends IIngredientKey> codec) {
        return (Codec<IIngredientKey>) codec;
    }
}
