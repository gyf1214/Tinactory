package org.shsts.tinactory.unit.machine;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shsts.tinactory.unit.fixture.TestMachine.AUTO_VOID;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_CONFIGS;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_LIMIT;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_LIST;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_NAME;
import static org.shsts.tinactory.unit.fixture.TestMachine.TARGET_RECIPE;

class MachineConfigTest {
    private static final RegistryAccess REGISTRY = TestCodecHelper.createRegistry(MACHINE_CONFIGS);

    @Test
    void shouldResolveTypedValueByProviderAndLocation() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder().set(AUTO_VOID, true).get());

        assertEquals(Optional.of(true), config.get(REGISTRY, AUTO_VOID.loc()));
        assertTrue(config.contains(AUTO_VOID));
    }

    @Test
    void shouldApplySetAndResetPacketValues() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder()
            .set(AUTO_VOID, true)
            .set(MACHINE_LIMIT, 42)
            .set(MACHINE_NAME, Component.literal("machine"))
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
            .set(MACHINE_NAME, Component.literal("machine"))
            .get());

        var serialized = config.serializeNBT(REGISTRY);
        var config1 = new MachineConfig();
        config1.deserializeNBT(REGISTRY, serialized);

        assertEquals(Optional.of(true), config1.get(AUTO_VOID));
        assertEquals(Optional.of(42), config1.get(MACHINE_LIMIT));
        assertEquals(Optional.of(Component.literal("machine")), config1.get(MACHINE_NAME));
    }

    @Test
    void shouldRoundTripResourceLocationComponentAndListCodecs() {
        var target = ResourceLocation.fromNamespaceAndPath("tinactory", "target");
        var values = List.of(3, 5, 8);
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder()
            .set(TARGET_RECIPE, target)
            .set(MACHINE_NAME, Component.literal("machine"))
            .set(MACHINE_LIST, values)
            .get());

        var serialized = config.serializeNBT(REGISTRY);
        var config1 = new MachineConfig();
        config1.deserializeNBT(REGISTRY, serialized);

        assertEquals(Optional.of(target), config1.get(TARGET_RECIPE));
        assertEquals(Optional.of(Component.literal("machine")), config1.get(MACHINE_NAME));
        assertEquals(Optional.of(values), config1.get(MACHINE_LIST));
    }

    @Test
    void shouldDeserializeLegacyKeyAsWellAsNormalKey() {
        var serialized = new CompoundTag();
        serialized.putBoolean("void", true);
        serialized.putInt("tinactory:limit", 42);
        serialized.put("tinactory:name", CodecHelper.encodeTag(REGISTRY, MACHINE_NAME.get().codec(),
            Component.literal("machine")));

        var config = new MachineConfig();
        config.deserializeNBT(REGISTRY, serialized);

        assertEquals(Optional.of(true), config.get(AUTO_VOID));
        assertEquals(Optional.of(42), config.get(MACHINE_LIMIT));
        assertEquals(Optional.of(Component.literal("machine")), config.get(MACHINE_NAME));
    }

    @Test
    void shouldFixLegacyKeyWhilePreserveUnknownKey() {
        var serialized = new CompoundTag();
        serialized.putBoolean("void", true);
        serialized.putInt("tinactory:limit", 42);
        serialized.put("tinactory:name", CodecHelper.encodeTag(REGISTRY, MACHINE_NAME.get().codec(),
            Component.literal("machine")));
        serialized.putDouble("unknown", 0.5);
        serialized.putString("tinactory:unknown", "opaque");

        var config = new MachineConfig();
        config.deserializeNBT(REGISTRY, serialized);
        var serialized1 = config.serializeNBT(REGISTRY);

        assertEquals(5, serialized1.size());
        assertTrue(serialized1.getBoolean("tinactory:auto_void"));
        assertEquals(42, serialized1.getInt("tinactory:limit"));
        assertEquals(Component.literal("machine"), config.get(MACHINE_NAME).orElseThrow());
        assertEquals(0.5, serialized1.getDouble("unknown"));
        assertEquals("opaque", serialized1.getString("tinactory:unknown"));
        assertFalse(serialized1.contains("void"));
        assertFalse(serialized1.contains("name"));
    }

    @Test
    void shouldPreferNamespacedValueAndConsumeRecognizedLegacyAlias() {
        var serialized = new CompoundTag();
        serialized.putBoolean("tinactory:auto_void", false);
        serialized.putBoolean("void", true);

        var config = new MachineConfig();
        config.deserializeNBT(REGISTRY, serialized);
        var serialized1 = config.serializeNBT(REGISTRY);

        assertEquals(Optional.of(false), config.get(AUTO_VOID));
        assertEquals(false, serialized1.getBoolean("tinactory:auto_void"));
        assertFalse(serialized1.contains("void"));
    }

    @Test
    void shouldRemoveValueFromTypedAndSerializedConfigurationAfterReset() {
        var config = new MachineConfig();
        config.apply(SetMachineConfigPacket.builder().set(AUTO_VOID, true).get());
        config.apply(SetMachineConfigPacket.builder().reset(AUTO_VOID).get());

        assertEquals(Optional.empty(), config.get(AUTO_VOID));
        assertEquals(Optional.empty(), config.get(REGISTRY, AUTO_VOID.loc()));
        assertFalse(config.serializeNBT(REGISTRY).contains(AUTO_VOID.loc().toString()));
    }
}
