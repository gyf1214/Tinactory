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
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.integration.AutocraftServiceBootstrap;
import org.shsts.tinactory.core.autocraft.integration.PatternNbtCodec;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Objects;

import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_COMPONENT;
import static org.shsts.tinactory.AllNetworks.LOGISTICS_SCHEDULING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpu extends MEStorageAccess implements INBTSerializable<CompoundTag> {
    private static final String ID = "autocraft/cpu";
    private static final String SNAPSHOT_KEY = "autocraftRunningSnapshot";

    private final PatternNbtCodec snapshotCodec = new PatternNbtCodec(new MachineConstraintRegistry());
    @Nullable
    private AutocraftJobService service;
    @Nullable
    private CompoundTag pendingSnapshot;
    @Nullable
    private CompoundTag lastSnapshot;

    public AutocraftCpu(BlockEntity blockEntity, double power) {
        super(blockEntity, power);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(double power) {
        return $ -> $.capability(ID, be -> new AutocraftCpu(be, power));
    }

    @Override
    protected void onConnect(INetwork network) {
        super.onConnect(network);

        var logistics = network.getComponent(LOGISTIC_COMPONENT.get());
        service = AutocraftServiceBootstrap.create(
            blockEntity, network, logistics, combinedItem, combinedFluid, machine.uuid());
        if (pendingSnapshot != null && service != null) {
            service.restoreRunningSnapshot(pendingSnapshot, snapshotCodec);
            pendingSnapshot = null;
        }
        if (service != null) {
            logistics.registerAutocraftCpu(machine, network.getSubnet(blockEntity.getBlockPos()), service);
            lastSnapshot = service.serializeRunningSnapshot(snapshotCodec).orElse(null);
        }
        blockEntity.setChanged();
    }

    private void onTick() {
        if (service == null) {
            return;
        }
        service.tick();
        var currentSnapshot = service.serializeRunningSnapshot(snapshotCodec).orElse(null);
        if (!Objects.equals(lastSnapshot, currentSnapshot)) {
            lastSnapshot = currentSnapshot;
            blockEntity.setChanged();
        }
    }

    private void unregisterFromLogistics() {
        machine.network().ifPresent(network ->
            network.getComponent(LOGISTIC_COMPONENT.get()).unregisterAutocraftCpu(machine.uuid()));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        super.subscribeEvents(eventManager);
        eventManager.subscribe(BUILD_SCHEDULING.get(), builder ->
            builder.add(LOGISTICS_SCHEDULING.get(), (world, network) -> onTick()));
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), world -> unregisterFromLogistics());
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world -> unregisterFromLogistics());
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        var snapshot = service == null ? lastSnapshot : service.serializeRunningSnapshot(snapshotCodec).orElse(null);
        if (snapshot != null) {
            tag.put(SNAPSHOT_KEY, snapshot.copy());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains(SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
            pendingSnapshot = tag.getCompound(SNAPSHOT_KEY).copy();
            lastSnapshot = pendingSnapshot.copy();
        } else {
            pendingSnapshot = null;
            lastSnapshot = null;
        }
    }
}
