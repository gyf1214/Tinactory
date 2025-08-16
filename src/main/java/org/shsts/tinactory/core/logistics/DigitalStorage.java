package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.IPortNotifier;
import org.shsts.tinactory.core.common.ItemCapabilityProvider;

import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class DigitalStorage extends ItemCapabilityProvider implements IPortNotifier {
    public static final ResourceLocation ID = modLoc("logistics/me_storage_cell");

    protected final int bytesLimit;
    protected int bytesRemaining;
    private final Set<Runnable> updateListeners = new HashSet<>();

    public DigitalStorage(ItemStack stack, int bytesLimit) {
        super(stack, ID);
        this.bytesLimit = bytesLimit;
        this.bytesRemaining = bytesLimit;
    }

    public int bytesLimit() {
        return bytesLimit;
    }

    public int bytesUsed() {
        return bytesLimit - bytesRemaining;
    }

    @Override
    public void onUpdate(Runnable listener) {
        updateListeners.add(listener);
    }

    @Override
    public void unregisterListener(Runnable listener) {
        updateListeners.remove(listener);
    }

    protected void invokeUpdate() {
        for (var cb : updateListeners) {
            cb.run();
        }
        syncTag();
    }
}
