package org.shsts.tinactory.unit.gui.sync;

class SetMachineConfigPacketTest {
    /*
    @Test
    void roundTripsMixedSetAndResetValues() {
        var nested = new CompoundTag();
        nested.putString("child", "value");
        var packet = SetMachineConfigPacket.builder()
            .set("enabled", true)
            .set("speed", 12)
            .set("capacity", 3000000000L)
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
        assertEquals(3000000000L, decoded.getSets().getLong("capacity"));
        assertEquals("washer", decoded.getSets().getString("name"));
        assertEquals("value", decoded.getSets().getCompound("nested").getString("child"));
        assertEquals(2, decoded.getResets().size());
    }
     */
}
