package org.shsts.tinactory.api.tech;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinycorelib.api.core.ILoc;

import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITechnology extends Comparable<ITechnology>, ILoc {
    List<ITechnology> getDepends();

    Map<String, Integer> getModifiers();

    long getMaxProgress();

    IRenderDescriptor getDisplay();

    Component getDescription();

    Component getDetails();
}
