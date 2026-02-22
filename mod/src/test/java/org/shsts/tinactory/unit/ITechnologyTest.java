package org.shsts.tinactory.unit;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.tech.ITechnology;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ITechnologyTest {
    @Test
    void descriptionAndDetailsIdsShouldFollowConventions() {
        var loc = new ResourceLocation("tinactory", "multiblock/large_turbine");

        assertEquals("tinactory.technology.multiblock.large_turbine", ITechnology.getDescriptionId(loc));
        assertEquals("tinactory.technology.multiblock.large_turbine.details", ITechnology.getDetailsId(loc));
    }
}
