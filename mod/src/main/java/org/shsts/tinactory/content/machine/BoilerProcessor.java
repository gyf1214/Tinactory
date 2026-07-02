package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllNetworks.PRE_SIGNAL_SCHEDULING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerProcessor extends CapabilityProvider implements IEventSubscriber,
    INBTSerializable<CompoundTag> {
    private static final String ID = "machine/boiler";

    private final BlockEntity blockEntity;
    private final FireBoiler boiler;

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
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(FireBoiler.Properties properties) {
        return $ -> $.container(ID, be -> new BoilerProcessor(be, properties));
    }

    private void onLoad() {
        CONTAINER.tryGet(blockEntity).ifPresent(boiler::setContainer);
    }

    private void buildScheduling(ISchedulingRegister builder) {
        builder.add(PRE_SIGNAL_SCHEDULING.get(), (world, network) -> boiler.setStopped(false));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), network ->
            Machine.registerStopSignal(network, MACHINE.get(blockEntity), boiler::setStopped));
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(CONTAINER_CHANGE.get(), boiler::onUpdateContainer);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(PROCESSOR, boiler);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return boiler.serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        boiler.deserializeNBT(provider, tag);
    }
}
