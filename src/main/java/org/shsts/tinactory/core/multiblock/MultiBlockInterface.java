package org.shsts.tinactory.core.multiblock;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket1;
import org.shsts.tinactory.content.logistics.IFlexibleContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.util.CodecHelper;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

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

    /**
     * Only called on server.
     */
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

    private void setJoined(Level world, boolean value) {
        blockEntity.getRealBlockState().ifPresent(state -> {
            var newState = state.setValue(MultiBlockInterfaceBlock.JOINED, value);
            world.setBlock(blockEntity.getBlockPos(), newState, 3);
        });
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
        var world = blockEntity.getLevel();
        assert world != null;
        setJoined(world, true);
        onMultiBlockUpdate();
    }

    public void resetMultiBlock() {
        if (multiBlock == null) {
            return;
        }
        LOGGER.debug("{} reset multiBlock", this);
        var world = blockEntity.getLevel();
        assert world != null;
        updateWorkBlock(world, false);
        setJoined(world, false);
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
    public void setConfig(SetMachineConfigPacket1 packet, boolean invokeEvent) {
        super.setConfig(packet, invokeEvent);
        if (invokeEvent && multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.SET_MACHINE_CONFIG);
        }
    }

    @Override
    public void setConfig(SetMachineConfigPacket packet, boolean invokeEvent) {
        super.setConfig(packet, invokeEvent);
        if (invokeEvent && multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.SET_MACHINE_CONFIG);
        }
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return super.canPlayerInteract(player) && multiBlock != null;
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(AllEvents.SERVER_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, this::onContainerChange);
    }

    @Override
    protected Optional<BlockState> getWorkBlock(Level world) {
        if (multiBlock == null) {
            return Optional.empty();
        }
        return multiBlock.blockEntity.getRealBlockState();
    }

    @Override
    protected void setWorkBlock(Level world, BlockState state) {
        if (multiBlock == null) {
            return;
        }
        world.setBlock(multiBlock.blockEntity.getBlockPos(), state, 3);
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

    @Override
    public Component getTitle() {
        if (config.hasString("name") || multiBlock == null) {
            return super.getTitle();
        }
        return I18n.name(multiBlock.blockEntity.getBlockState().getBlock());
    }

    public Optional<Layout> getLayout() {
        return multiBlock == null ? Optional.empty() : Optional.of(multiBlock.layout);
    }

    public Optional<RecipeType<?>> getRecipeType() {
        return Optional.ofNullable(recipeType);
    }

    public Optional<BlockState> getAppearanceBlock() {
        return multiBlock == null ? Optional.empty() : Optional.of(multiBlock.getAppearanceBlock());
    }

    @Override
    public ItemStack getIcon() {
        if (multiBlock == null) {
            return super.getIcon();
        }
        var block = multiBlock.blockEntity.getBlockState().getBlock();
        return new ItemStack(block);
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = super.serializeOnUpdate();
        if (multiBlock != null) {
            var pos = multiBlock.blockEntity.getBlockPos();
            tag.put("multiBlockPos", CodecHelper.encodeBlockPos(pos));
        }
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        super.deserializeOnUpdate(tag);
        var world = blockEntity.getLevel();
        assert world != null;

        if (tag.contains("multiBlockPos", Tag.TAG_COMPOUND)) {
            var pos = CodecHelper.parseBlockPos(tag.getCompound("multiBlockPos"));
            var be1 = world.getBlockEntity(pos);
            if (be1 == null) {
                return;
            }
            AllCapabilities.MULTI_BLOCK.tryGet(be1)
                .ifPresentOrElse(this::setMultiBlock, this::resetMultiBlock);
        } else {
            resetMultiBlock();
        }

        if (multiBlock != null) {
            EventManager.invoke(multiBlock.blockEntity, AllEvents.SET_MACHINE_CONFIG);
        }
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> basic(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, ID, MultiBlockInterface::new);
    }
}
