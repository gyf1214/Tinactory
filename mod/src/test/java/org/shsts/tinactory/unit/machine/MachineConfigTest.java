package org.shsts.tinactory.unit.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineConfigTest {
    @Test
    void shouldApplySetAndResetPacketValues() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder()
            .set("enabled", true)
            .set("limit", 42)
            .set("name", "machine")
            .get());

        config.apply(SetMachineConfigPacket.builder()
            .set("limit", 7)
            .reset("name")
            .get());

        assertEquals(Optional.of(true), config.getBoolean("enabled"));
        assertEquals(Optional.of(7), config.getInt("limit"));
        assertEquals(Optional.empty(), config.getString("name"));
    }

    @Test
    void shouldDefensivelyCopyAppliedAndSerializedTags() {
        var config = new MachineConfig();
        var child = new CompoundTag();
        child.putString("value", "original");
        config.apply(SetMachineConfigPacket.builder()
            .set("child", child)
            .get());

        child.putString("value", "mutated");
        var serialized = config.serializeNBT();
        serialized.getCompound("child").putString("value", "serialized-mutation");

        assertEquals("original", config.getCompound("child").orElseThrow().getString("value"));
    }

    @Test
    void shouldDefensivelyCopyDeserializedTag() {
        var config = new MachineConfig();
        var source = new CompoundTag();
        source.putString("name", "before");

        config.deserializeNBT(source);
        source.putString("name", "after");

        assertEquals(Optional.of("before"), config.getString("name"));
    }

    @Test
    void shouldReadTypedOptionalValues() {
        var config = new MachineConfig();
        var child = new CompoundTag();
        child.putInt("nested", 3);
        config.apply(SetMachineConfigPacket.builder()
            .set("enabled", true)
            .set("limit", 42)
            .set("name", "machine")
            .set("child", child)
            .get());

        assertTrue(config.contains("enabled", Tag.TAG_BYTE));
        assertEquals(Optional.of(true), config.getBoolean("enabled"));
        assertEquals(Optional.of(42), config.getInt("limit"));
        assertEquals(Optional.of("machine"), config.getString("name"));
        assertEquals(3, config.getCompound("child").orElseThrow().getInt("nested"));
        assertFalse(config.getBoolean("missing").isPresent());
    }
}
