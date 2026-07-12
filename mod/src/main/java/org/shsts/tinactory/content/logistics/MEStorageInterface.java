package org.shsts.tinactory.content.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.fluids.FluidStack;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Collection;

import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageInterface extends MEStorageAccess {
    public static final String ID = "logistics/me_storage_interface";

    public MEStorageInterface(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.container(ID, be -> new MEStorageInterface(be, power));
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);

        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        logistics.registerPort(machine(), 0, combinedItem);
        logistics.registerPort(machine(), 1, combinedFluid);
    }

    public Collection<ItemStack> getAllItems() {
        return combinedItem.getAllStorages();
    }

    public Collection<FluidStack> getAllFluids() {
        return combinedFluid.getAllStorages();
    }
}
