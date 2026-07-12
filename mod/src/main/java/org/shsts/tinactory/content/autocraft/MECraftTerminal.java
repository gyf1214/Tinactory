package org.shsts.tinactory.content.autocraft;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.MEStorageAccess;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllNetworks.AUTOCRAFT_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftTerminal extends MEStorageAccess {
    public static final String ID = "autocraft/terminal";

    @Nullable
    private AutocraftComponent autocraft;

    public MECraftTerminal(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.container(ID, be -> new MECraftTerminal(be, power));
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);
        autocraft = network.getComponent(AUTOCRAFT_COMPONENT.get());
    }

    @Nullable
    public AutocraftTerminalService createService() {
        if (autocraft == null) {
            return null;
        }
        return AutocraftServiceBootstrap.createTerminalService(
            autocraft,
            combinedItem,
            combinedFluid);
    }

    @Nullable
    public ICpuRuntime cpuRuntime() {
        return autocraft;
    }
}
