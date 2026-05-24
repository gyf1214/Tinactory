package org.shsts.tinactory.content.autocraft;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.content.logistics.MEStorageAccess;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.service.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.service.CpuStatusEntry;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.AUTOCRAFT_COMPONENT;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SCHEDULING;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftCpu extends MEStorageAccess implements INBTSerializable<CompoundTag> {
    public static final String ID = "autocraft/cpu";
    private static final String SNAPSHOT_KEY = "snapshot";

    private final PatternNbtCodec snapshotCodec =
        new PatternNbtCodec(MachineConstraintHelper.CODEC, StackHelper.KEY_CODEC);
    private final long transmissionBandwidth;
    private final int executionIntervalTicks;
    @Nullable
    private AutocraftJobService service;
    @Nullable
    private CompoundTag pendingSnapshot;

    public record Properties(double power, long transmissionBandwidth, int executionIntervalTicks) {}

    public MECraftCpu(BlockEntity blockEntity, Properties properties) {
        super(blockEntity, properties.power);
        this.transmissionBandwidth = properties.transmissionBandwidth;
        this.executionIntervalTicks = properties.executionIntervalTicks;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Properties properties) {
        return $ -> $.capability(ID, be -> new MECraftCpu(be, properties));
    }

    public CpuStatusEntry status() {
        var cpuId = machine.uuid();
        if (service == null) {
            return CpuStatusEntry.offline(cpuId);
        }
        var job = service.getJob();
        if (job.isEmpty()) {
            return CpuStatusEntry.idle(cpuId);
        }
        var current = job.get();
        return new CpuStatusEntry(
            cpuId,
            current.state(),
            current.targets(),
            current.completedSteps(),
            current.totalSteps(),
            current.error());
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);

        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        var autocraft = network.getComponent(AUTOCRAFT_COMPONENT.get());
        service = AutocraftServiceBootstrap.create(
            logistics,
            combinedItem,
            combinedFluid,
            transmissionBandwidth,
            executionIntervalTicks);
        if (pendingSnapshot != null) {
            service.restoreRunningSnapshot(pendingSnapshot, snapshotCodec);
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

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(BUILD_SCHEDULING.get(), builder ->
            builder.add(LOGISTICS_SCHEDULING.get(), (world, network) -> onTick()));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var snapshot = service == null ? pendingSnapshot :
            service.serializeRunningSnapshot(snapshotCodec).orElse(null);
        if (snapshot != null) {
            tag.put(SNAPSHOT_KEY, snapshot);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
            pendingSnapshot = tag.getCompound(SNAPSHOT_KEY).copy();
        } else {
            pendingSnapshot = null;
        }
    }
}
