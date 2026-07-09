package org.shsts.tinactory.content.autocraft;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.MEStorageAccess;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.core.machine.SimpleElectricConsumer;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.AUTOCRAFT_COMPONENT;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.integration.autocraft.PatternHelper.PATTERN_CODECS;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftCpu extends MEStorageAccess implements INBTSerializable<CompoundTag> {
    public static final String ID = "autocraft/cpu";
    private static final String SNAPSHOT_KEY = "snapshot";

    private final long itemBandwidth;
    private final long fluidBandwidth;
    private final int executionIntervalTicks;
    private final long memoryLimit;
    private final int parallelism;
    @Nullable
    private AutocraftJobService service;
    @Nullable
    private CompoundTag pendingSnapshot;

    public record Properties(double power, long itemBandwidth, long fluidBandwidth, int executionIntervalTicks,
        long memoryLimit, int parallelism) {}

    public MECraftCpu(BlockEntity blockEntity, Properties properties) {
        super(blockEntity);
        this.itemBandwidth = properties.itemBandwidth;
        this.fluidBandwidth = properties.fluidBandwidth;
        this.executionIntervalTicks = properties.executionIntervalTicks;
        this.memoryLimit = properties.memoryLimit;
        this.parallelism = properties.parallelism;

        var voltage = getBlockVoltage(blockEntity);
        electric = new SimpleElectricConsumer(voltage.value, properties.power) {
            @Override
            public ElectricMachineType getMachineType() {
                return isConsumingPower() ? super.getMachineType() : ElectricMachineType.NONE;
            }

            @Override
            public double getPowerCons() {
                return isConsumingPower() ? super.getPowerCons() : 0;
            }
        };
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.container(ID, be -> new MECraftCpu(be, properties));
    }

    public CpuStatusEntry status() {
        var cpuId = machine.uuid();
        if (service == null) {
            return CpuStatusEntry.offline(cpuId);
        }
        var job = service.getJob();
        if (job.isEmpty()) {
            return CpuStatusEntry.idle(cpuId, service.memoryLimit());
        }
        var current = job.get();
        return new CpuStatusEntry(
            cpuId,
            current.state(),
            current.targets(),
            current.completedSteps(),
            current.totalSteps(),
            current.error(),
            service.memoryLimit(),
            current.memoryUsage());
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);

        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var autocraft = network.getComponent(AUTOCRAFT_COMPONENT.get());
        var snapshot = pendingSnapshot;
        var provider = machine.registryAccess();
        if (service != null) {
            snapshot = service.serializeRunningSnapshot(provider, PATTERN_CODECS).orElse(snapshot);
        }
        service = AutocraftServiceBootstrap.create(
            logistics,
            machine,
            combinedItem,
            combinedFluid,
            itemBandwidth,
            fluidBandwidth,
            executionIntervalTicks,
            memoryLimit,
            parallelism);
        if (snapshot != null) {
            service.restoreRunningSnapshot(provider, snapshot, PATTERN_CODECS);
            pendingSnapshot = null;
        }
        autocraft.registerCpu(machine, service);
        blockEntity.setChanged();
    }

    private void onTick() {
        if (service == null) {
            return;
        }
        if (service.tick()) {
            blockEntity.setChanged();
        }
    }

    private boolean isConsumingPower() {
        return service != null && service.isBusy();
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(BUILD_SCHEDULING.get(), builder ->
            builder.add(LOGISTICS_SCHEDULING.get(), (world, network) -> onTick()));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var snapshot = service == null ? pendingSnapshot :
            service.serializeRunningSnapshot(provider, PATTERN_CODECS).orElse(null);
        if (snapshot != null) {
            tag.put(SNAPSHOT_KEY, snapshot);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
            pendingSnapshot = tag.getCompound(SNAPSHOT_KEY).copy();
        } else {
            pendingSnapshot = null;
        }
    }
}
