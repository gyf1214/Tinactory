package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerProcessor extends CapabilityProvider implements IEventSubscriber,
    INBTSerializable<CompoundTag> {
    private static final String ID = "machine/boiler";

    private final BlockEntity blockEntity;
    private final FireBoiler boiler;
    private final LazyOptional<IProcessor> processorCap;

    protected BoilerProcessor(BlockEntity blockEntity, FireBoiler.Properties properties) {
        this.blockEntity = blockEntity;
        this.boiler = new FireBoiler(properties) {
            @Override
            protected Optional<IMachine> machine() {
                return MACHINE.tryGet(blockEntity);
            }

            @Override
            protected double boilParallel() {
                return 1;
            }

            @Override
            protected int burnParallel() {
                return 1;
            }

            @Override
            protected void setChanged() {
                blockEntity.setChanged();
            }
        };
        this.processorCap = LazyOptional.of(() -> boiler);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(FireBoiler.Properties properties) {
        return $ -> $.capability(ID, be -> new BoilerProcessor(be, properties));
    }

    private void onLoad() {
        CONTAINER.tryGet(blockEntity).ifPresent(boiler::setContainer);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), network ->
            Machine.registerStopSignal(network, MACHINE.get(blockEntity), boiler::setStopped));
        eventManager.subscribe(CONTAINER_CHANGE.get(), boiler::onUpdateContainer);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return processorCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return boiler.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boiler.deserializeNBT(tag);
    }
}
