package org.shsts.tinactory.content.autocraft;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.MEStorageAccess;
import org.shsts.tinactory.core.autocraft.integration.AutocraftServiceBootstrap;
import org.shsts.tinactory.core.autocraft.integration.AutocraftTerminalService;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminal extends MEStorageAccess {
    public static final String ID = "autocraft/terminal";

    @Nullable
    private AutocraftTerminalService service;

    public AutocraftTerminal(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new AutocraftTerminal(be, power));
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);
        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        service = AutocraftServiceBootstrap.createTerminalService(
            blockEntity,
            network,
            logistics,
            combinedItem,
            combinedFluid);
    }

    @Nullable
    public AutocraftTerminalService service() {
        return service;
    }
}
