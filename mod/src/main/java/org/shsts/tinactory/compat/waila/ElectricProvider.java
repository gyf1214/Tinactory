package org.shsts.tinactory.compat.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.compat.waila.Waila.ELECTRIC;
import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricProvider extends ProviderBase implements IServerDataProvider<BlockEntity> {
    public static final ElectricProvider INSTANCE = new ElectricProvider();

    public ElectricProvider() {
        super(modLoc("electric"));
    }

    private void addTr(CompoundTag tag, String key) {
        var tagKey = "tinactoryElectric" + key.substring(0, 1).toUpperCase() + key.substring(1);
        var id = "electric." + key;
        if (tag.contains(tagKey, Tag.TAG_DOUBLE)) {
            add(helper.text(tr(id, NUMBER_FORMAT.format(tag.getDouble(tagKey)))));
        }
    }

    @Override
    protected void doAppendTooltip(CompoundTag tag, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(ELECTRIC)) {
            return;
        }

        addTr(tag, "consume");
        addTr(tag, "generate");
        addTr(tag, "bufferCons");
        addTr(tag, "bufferGen");
    }

    private Optional<ElectricComponent> getComponent(BlockEntity blockEntity) {
        return MACHINE.tryGet(blockEntity)
            .flatMap(IMachine::network)
            .or(() -> Multiblock.tryGet(blockEntity)
                .flatMap(Multiblock::getInterface)
                .flatMap(IMachine::network))
            .map($ -> $.getComponent(ELECTRIC_COMPONENT.get()));
    }

    private Optional<IElectricMachine> getElectric(BlockEntity blockEntity) {
        return ELECTRIC_MACHINE.tryGet(blockEntity)
            .or(() -> MACHINE.tryGet(blockEntity).flatMap(IMachine::electric));
    }

    @Override
    public void appendServerData(CompoundTag tag, ServerPlayer player, Level world,
        BlockEntity blockEntity, boolean showDetails) {
        var component = getComponent(blockEntity);
        var electric = getElectric(blockEntity);

        if (component.isEmpty() || electric.isEmpty()) {
            return;
        }

        var component1 = component.get();
        var workFactor = component1.getWorkFactor();
        var bufferFactor = component1.getBufferFactor();
        var electric1 = electric.get();

        switch (electric1.getMachineType()) {
            case CONSUMER -> tag.putDouble("tinactoryElectricConsume", workFactor * electric1.getPowerCons());
            case GENERATOR -> tag.putDouble("tinactoryElectricGenerate", electric1.getPowerGen());
            case BUFFER -> {
                var sign = MathUtil.compare(bufferFactor);
                if (sign > 0) {
                    tag.putDouble("tinactoryElectricBufferCons", bufferFactor * electric1.getPowerCons());
                } else if (sign < 0) {
                    tag.putDouble("tinactoryElectricBufferGen", -bufferFactor * electric1.getPowerGen());
                }
            }
        }
    }
}
