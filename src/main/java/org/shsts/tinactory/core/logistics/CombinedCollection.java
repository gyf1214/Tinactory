package org.shsts.tinactory.core.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedCollection {
    @Nullable
    private Runnable updateListener = null;

    public void onUpdate(Runnable listener) {
        updateListener = listener;
    }

    protected void invokeUpdate() {
        if (updateListener != null) {
            updateListener.run();
        }
    }
}
