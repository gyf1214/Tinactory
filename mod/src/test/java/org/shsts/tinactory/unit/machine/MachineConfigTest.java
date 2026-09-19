package org.shsts.tinactory.unit.machine;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestMachine.AUTO_VOID;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_CONFIGS;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_LIMIT;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_NAME;

class MachineConfigTest {
    private static final RegistryAccess REGISTRY = TestCodecHelper.createRegistry(MACHINE_CONFIGS);

    @Test
    void shouldApplySetAndResetPacketValues() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder()
            .set(AUTO_VOID, true)
            .set(MACHINE_LIMIT, 42)
            .set(MACHINE_NAME, "machine")
            .get());

        config.apply(SetMachineConfigPacket.builder()
            .set(MACHINE_LIMIT, 7)
            .reset(MACHINE_NAME)
            .get());

        assertEquals(Optional.of(true), config.get(AUTO_VOID));
        assertEquals(Optional.of(7), config.get(MACHINE_LIMIT));
        assertEquals(Optional.empty(), config.get(MACHINE_NAME));
    }

    @Test
    void shouldRoundTripSerialization() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder()
            .set(AUTO_VOID, true)
            .set(MACHINE_LIMIT, 42)
            .set(MACHINE_NAME, "machine")
            .get());

        var serialized = config.serializeNBT(REGISTRY);
        var config1 = new MachineConfig();
        config1.deserializeNBT(REGISTRY, serialized);

        assertEquals(Optional.of(true), config1.get(AUTO_VOID));
        assertEquals(Optional.of(42), config1.get(MACHINE_LIMIT));
        assertEquals(Optional.of("machine"), config1.get(MACHINE_NAME));
    }

    @Test
    void shouldDeserializeLegacyKeyAsWellAsNormalKey() {
        var serialized = new CompoundTag();
        serialized.putBoolean("void", true);
        serialized.putInt("tinactory:limit", 42);
        serialized.putString("tinactory:name", "machine");

        var config = new MachineConfig();
        config.deserializeNBT(REGISTRY, serialized);

        assertEquals(Optional.of(true), config.get(AUTO_VOID));
        assertEquals(Optional.of(42), config.get(MACHINE_LIMIT));
        assertEquals(Optional.of("machine"), config.get(MACHINE_NAME));
    }

    @Test
    void shouldFixLegacyKeyWhilePreserveUnknownKey() {
        var serialized = new CompoundTag();
        serialized.putBoolean("void", true);
        serialized.putInt("tinactory:limit", 42);
        serialized.putString("tinactory:name", "machine");
        serialized.putDouble("unknown", 0.5);

        var config = new MachineConfig();
        config.deserializeNBT(REGISTRY, serialized);
        var serialized1 = config.serializeNBT(REGISTRY);

        assertEquals(4, serialized1.size());
        assertTrue(serialized1.getBoolean("tinactory:auto_void"));
        assertEquals(42, serialized1.getInt("tinactory:limit"));
        assertEquals("machine", serialized1.getString("tinactory:name"));
        assertEquals(0.5, serialized1.getDouble("unknown"));
    }
}
