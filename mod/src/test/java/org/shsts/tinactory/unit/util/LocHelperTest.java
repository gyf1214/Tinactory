package org.shsts.tinactory.unit.util;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.core.util.LocHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LocHelperTest {
    @Test
    void locationFactoriesUseExpectedNamespaces() {
        assertEquals(new ResourceLocation("minecraft", "diamond"), LocHelper.mcLoc("diamond"));
        assertEquals(new ResourceLocation(TinactoryKeys.ID, "machine"), LocHelper.modLoc("machine"));
        assertEquals(new ResourceLocation("gregtech", "wire"), LocHelper.gregtech("wire"));
        assertEquals(new ResourceLocation("appliedenergistics2", "cell"), LocHelper.ae2("cell"));
        assertEquals(new ResourceLocation("ic2", "cable"), LocHelper.ic2("cable"));
    }

    @Test
    void pathHelpersPreserveIdentityWhenPrefixOrSuffixIsEmpty() {
        var base = new ResourceLocation("tinactory", "base/path");

        assertSame(base, LocHelper.extend(base, ""));
        assertSame(base, LocHelper.prepend(base, ""));
        assertEquals(new ResourceLocation("tinactory", "base/path/extra"), LocHelper.extend(base, "extra"));
        assertEquals(new ResourceLocation("tinactory", "base/path_suffix"), LocHelper.suffix(base, "_suffix"));
        assertEquals(new ResourceLocation("tinactory", "prefix/base/path"), LocHelper.prepend(base, "prefix"));
    }

    @Test
    void nameSelectsForwardOrBackwardPathSegments() {
        assertEquals("machine", LocHelper.name("machine/basic/ulv", 0));
        assertEquals("basic", LocHelper.name("machine/basic/ulv", 1));
        assertEquals("ulv", LocHelper.name("machine/basic/ulv", -1));
        assertEquals("basic", LocHelper.name("machine/basic/ulv", -2));
    }
}
