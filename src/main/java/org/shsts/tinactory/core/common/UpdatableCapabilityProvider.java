package org.shsts.tinactory.core.common;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class UpdatableCapabilityProvider extends CapabilityProvider
    implements INBTUpdatable<CompoundTag> {
    private boolean isUpdateForced = true;

    protected void sendUpdate(SmartBlockEntity be) {
        forceUpdate();
        be.sendUpdate();
    }

    protected void forceUpdate() {
        isUpdateForced = true;
    }

    @Override
    public boolean shouldSendUpdate() {
        if (isUpdateForced) {
            isUpdateForced = false;
            return true;
        }
        return false;
    }

    @Override
    public abstract CompoundTag serializeOnUpdate();

    @Override
    public abstract void deserializeOnUpdate(CompoundTag tag);
}
