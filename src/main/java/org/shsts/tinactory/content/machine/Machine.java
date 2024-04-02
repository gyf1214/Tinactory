package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllBlockEntityEvents;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.CompositeNetwork;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

import static org.shsts.tinactory.core.util.GeneralUtil.optionalCastor;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends SmartBlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();

    @Nullable
    protected CompositeNetwork network;

    protected boolean autoDumpItem;
    protected boolean autoDumpFluid;
    @Nullable
    protected ResourceLocation targetRecipeLoc;
    @Nullable
    protected ProcessingRecipe<?> targetRecipe;

    public Machine(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static Machine primitive(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        return new PrimitiveMachine(type, pos, state);
    }

    public static BlockEntityBuilder.Factory<Machine> factory(Voltage voltage) {
        return voltage == Voltage.PRIMITIVE ? Machine::primitive : Machine::new;
    }

    public boolean isAutoDumpItem() {
        return autoDumpItem;
    }

    public void setAutoDumpItem(boolean autoDumpItem) {
        this.autoDumpItem = autoDumpItem;
    }

    public boolean isAutoDumpFluid() {
        return autoDumpFluid;
    }

    public void setAutoDumpFluid(boolean autoDumpFluid) {
        this.autoDumpFluid = autoDumpFluid;
    }

    @Nullable
    public ResourceLocation getTargetRecipeLoc() {
        return this.targetRecipe == null ? null : this.targetRecipe.getId();
    }

    public void setTargetRecipeLoc(@Nullable ResourceLocation loc) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (loc == null) {
            this.targetRecipe = null;
            return;
        }
        var recipeManager = this.level.getRecipeManager();
        this.targetRecipe = recipeManager.byKey(loc)
                .flatMap(optionalCastor(ProcessingRecipe.class))
                .orElse(null);
    }

    @Override
    protected void onLoad(Level world) {
        super.onLoad(world);
        if (!world.isClientSide && this.targetRecipeLoc != null) {
            var recipeManager = world.getRecipeManager();
            this.targetRecipe = recipeManager.byKey(this.targetRecipeLoc)
                    .flatMap(optionalCastor(ProcessingRecipe.class))
                    .orElse(null);
        }
        LOGGER.debug("machine {}: loaded", this);
    }

    @Override
    protected void onRemovedInWorld(Level world) {
        if (this.network != null) {
            this.network.invalidate();
        }
        super.onRemovedInWorld(world);
        LOGGER.debug("machine {}: removed in world", this);
    }

    @Override
    protected void onRemovedByChunk(Level world) {
        if (this.network != null) {
            this.network.invalidate();
        }
        LOGGER.debug("machine {}: removed by chunk unload", this);
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(CompositeNetwork network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
        EventManager.invoke(this, AllBlockEntityEvents.CONNECT, network);
    }

    protected void onPreWork(Level world, Network network) {
        assert this.network == network;
        this.getProcessor().ifPresent(IProcessor::onPreWork);
        var logistics = this.network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        if (this.autoDumpItem) {
            EventManager.invoke(this, AllBlockEntityEvents.DUMP_ITEM_OUTPUT, logistics);
        }
        if (this.autoDumpFluid) {
            EventManager.invoke(this, AllBlockEntityEvents.DUMP_FLUID_OUTPUT, logistics);
        }
    }

    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = this.network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        this.getProcessor().ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(Component.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EventManager.invoke(this, AllBlockEntityEvents.BUILD_SCHEDULING, builder);
    }

    public Optional<IProcessor> getProcessor() {
        return this.getCapability(AllCapabilities.PROCESSOR.get()).resolve();
    }

    public Optional<IElectricMachine> getElectric() {
        return this.getCapability(AllCapabilities.ELECTRIC_MACHINE.get()).resolve();
    }

    public Optional<IContainer> getContainer() {
        return this.getCapability(AllCapabilities.CONTAINER.get()).resolve();
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        this.network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }

    @Override
    protected void serializeOnSave(CompoundTag tag) {
        tag.putBoolean("autoDumpItem", this.autoDumpItem);
        tag.putBoolean("autoDumpFluid", this.autoDumpFluid);
        var recipeLoc = this.targetRecipeLoc != null ? this.targetRecipeLoc :
                (this.targetRecipe != null ? this.targetRecipe.getId() : null);
        if (recipeLoc != null) {
            tag.putString("targetRecipe", recipeLoc.toString());
        }
    }

    @Override
    protected void deserializeOnSave(CompoundTag tag) {
        this.autoDumpItem = tag.getBoolean("autoDumpItem");
        this.autoDumpFluid = tag.getBoolean("autoDumpFluid");
        if (tag.contains("targetRecipe", Tag.TAG_STRING)) {
            this.targetRecipeLoc = new ResourceLocation(tag.getString("targetRecipe"));
        } else {
            this.targetRecipeLoc = null;
            this.targetRecipe = null;
        }
    }
}
