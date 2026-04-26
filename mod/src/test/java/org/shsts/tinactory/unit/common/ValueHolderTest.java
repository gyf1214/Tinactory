package org.shsts.tinactory.unit.common;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.common.ValueHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValueHolderTest {
    @Test
    void tryGetIsEmptyUntilValueIsSet() {
        var holder = ValueHolder.<String>create();

        assertTrue(holder.tryGet().isEmpty());

        holder.setValue("value");

        assertEquals("value", holder.tryGet().orElseThrow());
        assertEquals("value", holder.get());
    }

    @Test
    void getRequiresAValueWhenAssertionsAreEnabled() {
        var holder = ValueHolder.<String>create();

        assertThrows(AssertionError.class, holder::get);
    }
}
