package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

import static org.shsts.tinactory.content.machine.RecipeProcessor.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ElectricFurnace extends CapabilityProvider
        implements IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final BlockEntity blockEntity;
    private final Voltage voltage;
    private final double workFactor;

    private IContainer container;
    private IItemCollection inputPort;
    private RecipeWrapper inputWrapper;
    private IItemCollection outputPort;

    @Nullable
    private ResourceLocation currentRecipeLoc = null;
    @Nullable
    private SmeltingRecipe currentRecipe = null;
    @Nullable
    private SmeltingRecipe targetRecipe = null;
    private long workProgress = 0L;
    private boolean needUpdate = true;

    public ElectricFurnace(BlockEntity blockEntity, Voltage voltage) {
        this.blockEntity = blockEntity;
        this.voltage = voltage;
        this.workFactor = 1 << (voltage.rank - 1);
    }

    private void setUpdateRecipe() {
        if (currentRecipe == null) {
            needUpdate = true;
        }
    }

    private boolean canSmelt(SmeltingRecipe recipe) {
        var result = recipe.assemble(inputWrapper);
        return outputPort.acceptInput(result) && outputPort.insertItem(result, true).isEmpty();
    }

    private void updateRecipe() {
        if (currentRecipe != null || !needUpdate) {
            return;
        }
        var world = blockEntity.getLevel();
        assert world != null;
        assert currentRecipeLoc == null;
        currentRecipe = world.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, inputWrapper, world)
                .filter(this::canSmelt)
                .orElse(null);

        workProgress = 0;
        if (currentRecipe != null) {
            var ingredient = currentRecipe.getIngredients().get(0);
            ItemHelper.consumeItemCollection(inputPort, ingredient, 1, false);
        }
        needUpdate = false;
        blockEntity.setChanged();
    }

    private void setTargetRecipe(ResourceLocation loc) {
        var world = blockEntity.getLevel();
        assert world != null;
        var recipe = world.getRecipeManager().byKey(loc);
        if (recipe.isEmpty() || !(recipe.get() instanceof SmeltingRecipe recipe1) ||
                recipe1.getType() != RecipeType.SMELTING) {
            resetTargetRecipe();
            return;
        }
        targetRecipe = recipe1;

        container.setItemFilter(0, targetRecipe.getIngredients().get(0));
    }

    private void resetTargetRecipe() {
        targetRecipe = null;
        container.resetFilter(0);
    }

    private void updateTargetRecipe() {
        var loc = AllCapabilities.MACHINE.tryGet(blockEntity)
                .flatMap(m -> m.config.getLoc("targetRecipe"))
                .orElse(null);
        LOGGER.debug("update target recipe = {}", loc);
        if (loc == null) {
            resetTargetRecipe();
        } else {
            setTargetRecipe(loc);
        }
    }

    private void onLoad() {
        container = AllCapabilities.CONTAINER.get(blockEntity);
        inputPort = container.getPort(0, false).asItem();
        outputPort = container.getPort(1, true).asItem();
        inputWrapper = new RecipeWrapper((IItemHandlerModifiable)
                ((ItemHandlerCollection) inputPort).itemHandler);
    }

    private void onServerLoad(Level world) {
        onLoad();

        var recipeManager = world.getRecipeManager();
        currentRecipe = (SmeltingRecipe) Optional.ofNullable(currentRecipeLoc)
                .flatMap(recipeManager::byKey)
                .filter(r -> r.getType() == RecipeType.SMELTING)
                .orElse(null);
        currentRecipeLoc = null;
        if (currentRecipe != null) {
            needUpdate = false;
        }

        updateTargetRecipe();
    }

    private void onMachineConfig() {
        updateTargetRecipe();
        setUpdateRecipe();
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.CLIENT_LOAD, $ -> onLoad());
        eventManager.subscribe(AllEvents.CONTAINER_CHANGE, $ -> setUpdateRecipe());
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, this::onMachineConfig);
    }

    @Override
    public long getVoltage() {
        return voltage.value;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.CONSUMER;
    }

    @Override
    public double getPowerGen() {
        return 0;
    }

    @Override
    public double getPowerCons() {
        return currentRecipe == null ? 0d : voltage.value * 0.625d;
    }

    @Override
    public void onPreWork() {
        updateRecipe();
    }

    private long getMaxProgress() {
        assert currentRecipe != null;
        return currentRecipe.getCookingTime() * PROGRESS_PER_TICK;
    }

    @Override
    public void onWorkTick(double partial) {
        if (currentRecipe == null) {
            return;
        }
        var progress = (long) Math.floor(partial * workFactor * (double) PROGRESS_PER_TICK);
        workProgress += progress;
        if (workProgress >= getMaxProgress()) {
            outputPort.insertItem(currentRecipe.assemble(inputWrapper), false);
            currentRecipe = null;
            needUpdate = true;
        }
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        return currentRecipe == null ? 0d : (double) workProgress / (double) getMaxProgress();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get() || cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (currentRecipe != null) {
            tag.putString("currentRecipe", currentRecipe.getId().toString());
            tag.putLong("workProgress", workProgress);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        currentRecipe = null;
        if (tag.contains("currentRecipe", Tag.TAG_STRING)) {
            currentRecipeLoc = new ResourceLocation(tag.getString("currentRecipe"));
            workProgress = tag.getLong("workProgress");
        } else {
            currentRecipeLoc = null;
        }
    }
}
