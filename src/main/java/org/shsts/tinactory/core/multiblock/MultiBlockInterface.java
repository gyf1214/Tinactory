package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.IFlexibleContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockInterface extends Machine {
    private static final Logger LOGGER = LogUtils.getLogger();

    private IFlexibleContainer container;
    @Nullable
    private MultiBlock multiBlock = null;
    @Nullable
    private IProcessor processor = null;
    @Nullable
    private IElectricMachine electricMachine = null;

    public MultiBlockInterface(SmartBlockEntity be) {
        super(be);
    }

    public void setMultiBlock(MultiBlock target) {
        LOGGER.debug("{} set multiBlock = {}", this, target);
        multiBlock = target;
        processor = target.getProcessor();
        electricMachine = target.getElectric();
        container.setLayout(target.layout);
        if (network != null) {
            network.invalidate();
        }
    }

    public void resetMultiBlock() {
        LOGGER.debug("{} reset multiBlock", this);
        multiBlock = null;
        processor = null;
        electricMachine = null;
        container.resetLayout();
        if (network != null) {
            network.invalidate();
        }
    }

    @Override
    public void setConfig(SetMachinePacket packet) {
        super.setConfig(packet);
    }

    private void onLoad() {
        container = (IFlexibleContainer) AllCapabilities.CONTAINER.get(blockEntity);
    }

    private void onContainerChange(boolean input) {
        if (multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.CONTAINER_CHANGE, input);
        }
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(AllEvents.SERVER_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, this::onContainerChange);
    }

    @Override
    public Optional<IProcessor> getProcessor() {
        return Optional.ofNullable(processor);
    }

    @Override
    public Optional<IContainer> getContainer() {
        return Optional.of(container);
    }

    @Override
    public Optional<IElectricMachine> getElectric() {
        return Optional.ofNullable(electricMachine);
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> basic(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID, MultiBlockInterface::new);
    }
}
