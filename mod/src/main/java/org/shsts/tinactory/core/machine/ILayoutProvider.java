package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.Layout;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ILayoutProvider {
    Layout getLayout();
}
