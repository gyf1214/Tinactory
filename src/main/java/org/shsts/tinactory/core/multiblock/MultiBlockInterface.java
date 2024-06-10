package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.IFlexibleContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MultiBlockInterface extends Machine {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final Voltage voltage;
    private IFlexibleContainer container;
    @Nullable
    private MultiBlock multiBlock = null;
    @Nullable
    private IProcessor processor = null;
    @Nullable
    private IElectricMachine electricMachine = null;
    @Nullable
    private RecipeType<?> recipeType = null;

    public MultiBlockInterface(SmartBlockEntity be) {
        super(be);
        this.voltage = RecipeProcessor.getBlockVoltage(be);
    }

    private void onMultiBlockUpdate() {
        var world = blockEntity.getLevel();
        if (world == null || world.isClientSide) {
            return;
        }
        if (network != null) {
            network.invalidate();
        }
        sendUpdate(blockEntity);
    }

    public void setMultiBlock(MultiBlock target) {
        if (multiBlock == target) {
            return;
        }
        LOGGER.debug("{} set multiBlock = {}", this, target);
        multiBlock = target;
        processor = target.getProcessor();
        electricMachine = target.getElectric();
        recipeType = processor instanceof RecipeProcessor<?> recipeProcessor ?
                recipeProcessor.recipeType : null;
        container.setLayout(target.layout);
        onMultiBlockUpdate();
    }

    public void resetMultiBlock() {
        if (multiBlock == null) {
            return;
        }
        LOGGER.debug("{} reset multiBlock", this);
        multiBlock = null;
        processor = null;
        electricMachine = null;
        container.resetLayout();
        onMultiBlockUpdate();
    }

    private void onLoad() {
        container = (IFlexibleContainer) AllCapabilities.CONTAINER.get(blockEntity);
    }

    private void onContainerChange() {
        if (multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.CONTAINER_CHANGE);
        }
    }

    @Override
    public void setConfig(SetMachinePacket packet) {
        super.setConfig(packet);
        if (multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.SET_MACHINE_CONFIG);
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

    public Optional<Layout> getLayout() {
        return multiBlock == null ? Optional.empty() : Optional.of(multiBlock.layout);
    }

    public Optional<RecipeType<?>> getRecipeType() {
        return Optional.ofNullable(recipeType);
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiBlock != null) {
            var pos = multiBlock.blockEntity.getBlockPos();
            tag.put("multiBlockPos", CodecHelper.serializeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        super.deserializeOnUpdate(tag);
        var world = blockEntity.getLevel();
        assert world != null;

        var oldMultiBlock = multiBlock;
        if (tag.contains("multiBlockPos", Tag.TAG_COMPOUND)) {
            var pos = CodecHelper.deserializeBlockPos(tag.getCompound("multiBlockPos"));
            var be1 = world.getBlockEntity(pos);
            if (be1 == null) {
                return;
            }
            AllCapabilities.MULTI_BLOCK.tryGet(be1).ifPresent(this::setMultiBlock);
        } else {
            resetMultiBlock();
        }

        if (oldMultiBlock == multiBlock && multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.SET_MACHINE_CONFIG);
        }
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> basic(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID, MultiBlockInterface::new);
    }
}
