package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalCpuStatusSyncSlot implements IPacket {
    private final List<Row> rows = new ArrayList<>();

    public record Row(
        UUID cpuId,
        boolean available,
        String targetSummary,
        String currentStep,
        String blockedReason,
        boolean cancellable) {}

    public AutocraftTerminalCpuStatusSyncSlot() {}

    public AutocraftTerminalCpuStatusSyncSlot(List<Row> rows) {
        this.rows.addAll(rows);
    }

    public List<Row> rows() {
        return List.copyOf(rows);
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        buf.writeCollection(rows, (buf1, row) -> {
            buf1.writeUUID(row.cpuId());
            buf1.writeBoolean(row.available());
            buf1.writeUtf(row.targetSummary());
            buf1.writeUtf(row.currentStep());
            buf1.writeUtf(row.blockedReason());
            buf1.writeBoolean(row.cancellable());
        });
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        rows.clear();
        rows.addAll(buf.readList(buf1 -> new Row(
            buf1.readUUID(),
            buf1.readBoolean(),
            buf1.readUtf(),
            buf1.readUtf(),
            buf1.readUtf(),
            buf1.readBoolean())));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AutocraftTerminalCpuStatusSyncSlot other)) {
            return false;
        }
        return rows.equals(other.rows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rows);
    }
}
