package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageEventPacket implements IPacket {
    public static final int QUICK_MOVE_BUTTON = -1;

    @Nullable
    private IStackKey key;
    private int button;
    private boolean isQuickMove;

    public StorageEventPacket(ItemStack item, int button) {
        this.key = StackHelper.ITEM_ADAPTER.keyOf(item);
        this.button = button;
    }

    public StorageEventPacket(FluidStack fluid, int button) {
        this.key = StackHelper.FLUID_ADAPTER.keyOf(fluid);
        this.button = button;
    }

    public StorageEventPacket(IStackKey key, int button, boolean isQuickMove) {
        this.key = key;
        this.button = button;
        this.isQuickMove = isQuickMove;
    }

    public StorageEventPacket(int button) {
        this.key = null;
        this.button = button;
        this.isQuickMove = false;
    }

    public StorageEventPacket() {}

    public int button() {
        return button;
    }

    public boolean isQuickMove() {
        return isQuickMove;
    }

    public boolean isEmpty() {
        return key == null;
    }

    public boolean isItem() {
        return key != null && key.type() == PortType.ITEM;
    }

    public boolean isFluid() {
        return key != null && key.type() == PortType.FLUID;
    }

    public IStackKey key() {
        assert key != null;
        return key;
    }

    public ItemStack item() {
        assert key != null;
        return StackHelper.ITEM_ADAPTER.stackOf(key);
    }

    @Override
    public void serializeToBuf(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(key != null);
        if (key != null) {
            StackHelper.KEY_STREAM_CODEC.encode(buf, key);
        }
        buf.writeVarInt(button);
        buf.writeBoolean(isQuickMove);
    }

    @Override
    public void deserializeFromBuf(RegistryFriendlyByteBuf buf) {
        key = buf.readBoolean() ? StackHelper.KEY_STREAM_CODEC.decode(buf) : null;
        button = buf.readVarInt();
        isQuickMove = buf.readBoolean();
    }
}
