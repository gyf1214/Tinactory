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
        assertEquals(ResourceLocation.withDefaultNamespace("diamond"), LocHelper.mcLoc("diamond"));
        assertEquals(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "machine"), LocHelper.modLoc("machine"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("gregtech", "wire"), LocHelper.gregtech("wire"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("appliedenergistics2", "cell"), LocHelper.ae2("cell"));
        assertEquals(ResourceLocation.fromNamespaceAndPath("ic2", "cable"), LocHelper.ic2("cable"));
    }

    @Test
    void pathHelpersPreserveIdentityWhenPrefixOrSuffixIsEmpty() {
        var base = ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "base/path");

        assertSame(base, LocHelper.extend(base, ""));
        assertSame(base, LocHelper.prepend(base, ""));
        assertEquals(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "base/path/extra"),
            LocHelper.extend(base, "extra"));
        assertEquals(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "base/path_suffix"),
            LocHelper.suffix(base, "_suffix"));
        assertEquals(ResourceLocation.fromNamespaceAndPath(TinactoryKeys.ID, "prefix/base/path"),
            LocHelper.prepend(base, "prefix"));
    }

    @Test
    void nameSelectsForwardOrBackwardPathSegments() {
        assertEquals("machine", LocHelper.name("machine/basic/ulv", 0));
        assertEquals("basic", LocHelper.name("machine/basic/ulv", 1));
        assertEquals("ulv", LocHelper.name("machine/basic/ulv", -1));
        assertEquals("basic", LocHelper.name("machine/basic/ulv", -2));
    }
}
