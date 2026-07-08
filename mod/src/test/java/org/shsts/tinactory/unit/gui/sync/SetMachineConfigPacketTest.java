package org.shsts.tinactory.unit.gui.sync;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SetMachineConfigPacketTest {
    @Test
    void builderReportsEmptyOnlyWhenNoSetsOrResetsExist() {
        var builder = new SetMachineConfigPacket.Builder();

        assertTrue(builder.isEmpty());

        builder.reset("alpha");
        assertFalse(builder.isEmpty());
    }

    @Test
    void roundTripsMixedSetAndResetValues() {
        var nested = new CompoundTag();
        nested.putString("child", "value");
        var packet = SetMachineConfigPacket.builder()
            .set("enabled", true)
            .set("speed", 12)
            .set("name", "washer")
            .set("nested", nested)
            .reset("obsolete")
            .reset("legacy")
            .get();
        var buf = TestCodecHelper.buf();

        packet.serializeToBuf(buf);
        var decoded = new SetMachineConfigPacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.getSets(), decoded.getSets());
        assertEquals(packet.getResets(), decoded.getResets());
        assertTrue(decoded.getSets().getBoolean("enabled"));
        assertEquals(12, decoded.getSets().getInt("speed"));
        assertEquals("washer", decoded.getSets().getString("name"));
        assertEquals("value", decoded.getSets().getCompound("nested").getString("child"));
        assertEquals(2, decoded.getResets().size());
    }
}
