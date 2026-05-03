package org.shsts.tinactory.gametest;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.gametest.dependency.DependencyChecker;

@GameTestHolder(TinactoryKeys.ID)
public final class TinactoryTest {
    @GameTest
    public static void testSucceed(GameTestHelper helper) {
        DependencyChecker.runRuntimeCheck(helper.getLevel());
        helper.succeed();
    }
}
