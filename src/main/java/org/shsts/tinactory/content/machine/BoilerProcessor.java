package org.shsts.tinactory.content.machine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.List;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.CONNECT;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.core.machine.ProcessingMachine.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerProcessor extends CapabilityProvider implements
    IProcessor, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final String ID = "machine/boiler";

    private final BlockEntity blockEntity;
    private final double burnSpeed;
    private final double burnHeat;
    private final Boiler boiler;

    private IItemCollection fuelPort;
    private long maxBurn = 0L;
    private long currentBurn = 0L;
    private boolean needUpdate = true;
    private boolean stopped = false;

    public record Properties(double baseHeat, double baseDecay, double burnSpeed, double burnHeat) {}

    private BoilerProcessor(BlockEntity blockEntity, Properties properties) {
        this.blockEntity = blockEntity;
        this.burnSpeed = properties.burnSpeed;
        this.burnHeat = properties.burnHeat;
        this.boiler = new Boiler(properties.baseHeat, properties.baseDecay);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.capability(ID, be -> new BoilerProcessor(be, properties));
    }

    public static double getHeat(IProcessor processor) {
        return ((BoilerProcessor) processor).boiler.getHeat();
    }

    private void onLoad() {
        var container = AllCapabilities.CONTAINER.get(blockEntity);

        fuelPort = container.getPort(0, ContainerAccess.INTERNAL).asItem();
        fuelPort.asItemFilter().setFilters(List.of(item ->
            ForgeHooks.getBurnTime(item, null) > 0 && !item.hasContainerItem()));

        var inputPort = container.getPort(1, ContainerAccess.INTERNAL).asFluid();
        var outputPort = container.getPort(2, ContainerAccess.INTERNAL).asFluid();
        boiler.setContainer(inputPort, outputPort);
    }

    protected double boilParallel() {
        return 1d;
    }

    protected int burnParallel() {
        return 1;
    }

    private void setUpdate() {
        if (maxBurn == 0) {
            needUpdate = true;
        }
    }

    @Override
    public void onPreWork() {
        if (maxBurn > 0 || !needUpdate) {
            return;
        }

        currentBurn = 0;
        if (stopped) {
            return;
        }

        var maxParallel = burnParallel();
        for (var stack : fuelPort.getAllItems()) {
            if (ForgeHooks.getBurnTime(stack, null) > 0) {
                var stack1 = StackHelper.copyWithCount(stack, maxParallel);
                var extracted = fuelPort.extractItem(stack1, false);
                if (!extracted.isEmpty()) {
                    maxBurn = ForgeHooks.getBurnTime(extracted, null) * PROGRESS_PER_TICK * extracted.getCount();
                    break;
                }
            }
        }

        needUpdate = false;
        blockEntity.setChanged();
    }

    @Override
    public void onWorkTick(double partial) {
        var heatInput = 0d;
        if (maxBurn > 0) {
            currentBurn += (long) (burnSpeed * (double) PROGRESS_PER_TICK);
            if (currentBurn >= maxBurn) {
                maxBurn = 0;
                needUpdate = true;
            }
            heatInput = burnHeat;
        }

        var world = blockEntity.getLevel();
        assert world != null;
        boiler.tick(world, heatInput, boilParallel());

        stopped = false;
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        if (maxBurn <= 0) {
            return currentBurn > 0 ? 1 : 0;
        }
        return (double) currentBurn / (double) maxBurn;
    }

    private void onConnect(INetwork network) {
        Machine.registerStopSignal(network, MACHINE.get(blockEntity), $ -> stopped = $);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(CONTAINER_CHANGE.get(), this::setUpdate);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("boiler", boiler.serializeNBT());
        tag.putLong("maxBurn", maxBurn);
        tag.putLong("currentBurn", currentBurn);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        boiler.deserializeNBT(tag.getCompound("boiler"));
        maxBurn = tag.getLong("maxBurn");
        currentBurn = tag.getLong("currentBurn");
    }
}
