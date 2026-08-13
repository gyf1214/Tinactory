package org.shsts.tinactory.content.gui.sync;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WorkbenchTransferEventPacket implements IPacket {
    private ResourceLocation recipeId;
    private boolean maxTransfer;

    public WorkbenchTransferEventPacket() {
        this.recipeId = ResourceLocation.withDefaultNamespace("air");
    }

    public WorkbenchTransferEventPacket(ResourceLocation recipeId, boolean maxTransfer) {
        this.recipeId = recipeId;
        this.maxTransfer = maxTransfer;
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(recipeId);
        buf.writeBoolean(maxTransfer);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        recipeId = buf.readResourceLocation();
        maxTransfer = buf.readBoolean();
    }

    public ResourceLocation getRecipeId() {
        return recipeId;
    }

    public boolean isMaxTransfer() {
        return maxTransfer;
    }
}
