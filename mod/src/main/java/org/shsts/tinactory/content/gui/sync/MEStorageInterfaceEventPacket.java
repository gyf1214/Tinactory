package org.shsts.tinactory.content.gui.sync;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.network.IPacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterfaceEventPacket implements IPacket {
    public static final int QUICK_MOVE_BUTTON = -1;

    @Nullable
    private ItemStack item;
    @Nullable
    private FluidStack fluid;
    private int button;

    public MEStorageInterfaceEventPacket(ItemStack item, int button) {
        this.item = item;
        this.fluid = null;
        this.button = button;
    }

    public MEStorageInterfaceEventPacket(FluidStack fluid, int button) {
        this.item = null;
        this.fluid = fluid;
        this.button = button;
    }

    public MEStorageInterfaceEventPacket(int button) {
        this.item = null;
        this.fluid = null;
        this.button = button;
    }

    public MEStorageInterfaceEventPacket() {}

    public int button() {
        return button;
    }

    public boolean isEmpty() {
        return item == null && fluid == null;
    }

    public boolean isFluid() {
        return fluid != null;
    }

    public FluidStack fluid() {
        assert fluid != null;
        return fluid;
    }

    public boolean isItem() {
        return item != null;
    }

    public ItemStack item() {
        assert item != null;
        return item;
    }

    @Override
    public void serializeToBuf(FriendlyByteBuf buf) {
        if (item == null && fluid == null) {
            buf.writeBoolean(true);
        } else if (item != null) {
            buf.writeBoolean(false);
            buf.writeBoolean(false);
            StackHelper.serializeStackToBuf(buf, item);
        } else {
            buf.writeBoolean(false);
            buf.writeBoolean(true);
            fluid.writeToPacket(buf);
        }
        buf.writeVarInt(button);
    }

    @Override
    public void deserializeFromBuf(FriendlyByteBuf buf) {
        if (buf.readBoolean()) {
            // isEmpty
            item = null;
            fluid = null;
        } else if (buf.readBoolean()) {
            // isFluid
            fluid = FluidStack.readFromPacket(buf);
            item = null;
        } else {
            item = StackHelper.deserializeStackFromBuf(buf);
            fluid = null;
        }
        button = buf.readVarInt();
    }
}
