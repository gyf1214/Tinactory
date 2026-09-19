package org.shsts.tinactory.unit.gui.sync;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;
import org.shsts.tinactory.unit.fixture.TestCodecHelper;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.shsts.tinactory.unit.fixture.TestMachine.AUTO_VOID;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_CONFIGS;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_LIMIT;
import static org.shsts.tinactory.unit.fixture.TestMachine.MACHINE_NAME;
import static org.shsts.tinactory.unit.fixture.TestMachine.TARGET_RECIPE;

class SetMachineConfigPacketTest {
    private <T> MachineConfig.Entry<T> entry(IEntry<IMachineConfigType<T>> type, T value) {
        return new MachineConfig.Entry<>(type.loc(), type.get(), value);
    }

    @Test
    void roundTripsMixedSetAndResetValues() {
        var packet = SetMachineConfigPacket.builder()
            .set(AUTO_VOID, true)
            .set(MACHINE_LIMIT, 12)
            .reset(MACHINE_NAME)
            .reset(TARGET_RECIPE)
            .get();
        var buf = TestCodecHelper.buf(TestCodecHelper.createRegistry(MACHINE_CONFIGS));

        packet.serializeToBuf(buf);
        var decoded = new SetMachineConfigPacket();
        decoded.deserializeFromBuf(buf);

        assertEquals(packet.getSets(), decoded.getSets());
        assertEquals(packet.getResets(), decoded.getResets());
        assertEquals(List.of(entry(AUTO_VOID, true), entry(MACHINE_LIMIT, 12)), decoded.getSets());
        assertEquals(List.of(MACHINE_NAME.get(), TARGET_RECIPE.get()), decoded.getResets());
    }
}
