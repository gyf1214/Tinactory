package org.shsts.tinactory.content.autocraft;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.MEStorageAccess;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllNetworks.AUTOCRAFT_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminal extends MEStorageAccess {
    public static final String ID = "autocraft/pattern_terminal";

    @Nullable
    private AutocraftComponent autocraft;

    public MEPatternTerminal(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.container(ID, be -> new MEPatternTerminal(be, power));
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);
        autocraft = network.getComponent(AUTOCRAFT_COMPONENT.get());
    }

    @Nullable
    public IPatternRepository patternRepository() {
        return autocraft == null ? null : autocraft.patternRepository();
    }
}
