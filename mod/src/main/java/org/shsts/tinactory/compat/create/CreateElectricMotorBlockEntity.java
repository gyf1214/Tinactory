package org.shsts.tinactory.compat.create;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.blockentity.IBlockEntityContainer;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.SERVER_TICK;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateElectricMotorBlockEntity extends GeneratingKineticBlockEntity {
    private static final String MACHINE_CONTAINER_ID = "network/machine";

    private final IBlockEntityContainer container;
    private final float stress;
    private final int rpm;
    private double lastWorkFactor = Double.NaN;

    public CreateElectricMotorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
        IBlockEntityContainer container, float stress, int rpm) {
        super(type, pos, state);
        this.container = container;
        this.stress = stress;
        this.rpm = rpm;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(float stress, int rpm, double power) {
        return builder -> builder
            .factory((type, pos, state, container) ->
                    new CreateElectricMotorBlockEntity(type, pos, state, container, stress, rpm),
                CreateElectricMotorBlockEntity::container)
            .container(MACHINE_CONTAINER_ID, blockEntity ->
                new MotorMachine((CreateElectricMotorBlockEntity) blockEntity, power))
            .capability(MACHINE, ELECTRIC_MACHINE);
    }

    public IBlockEntityContainer container() {
        return container;
    }

    public double workFactor() {
        return MACHINE.tryGet(this)
            .flatMap(IMachine::network)
            .map(network -> network.getComponent(ELECTRIC_COMPONENT.get()).getWorkFactor())
            .orElse(0d);
    }

    @Override
    public float getGeneratedSpeed() {
        return workFactor() > 0d ? rpm : 0f;
    }

    @Override
    public float calculateAddedStressCapacity() {
        return stress * (float) workFactor();
    }

    private void updateStressCapacity() {
        var currentWorkFactor = workFactor();
        if (Double.compare(currentWorkFactor, lastWorkFactor) != 0) {
            lastWorkFactor = currentWorkFactor;
            if (hasNetwork()) {
                notifyStressCapacityChange(stress * (float) currentWorkFactor);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        container.onLoad();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        container.onChunkUnloaded();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        container.onRemoved();
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        if (clientPacket) {
            container.writeUpdateTag(tag, true, registries);
        } else {
            container.saveAdditional(tag, registries);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        if (clientPacket) {
            container.handleUpdateTag(tag, registries);
        } else {
            container.loadAdditional(tag, registries);
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return container.createUpdatePacket(super::getUpdateTag)
            .map(ClientboundBlockEntityDataPacket.class::cast)
            .orElseGet(super::getUpdatePacket);
    }

    private static final class MotorMachine extends Machine implements IElectricMachine {
        private final CreateElectricMotorBlockEntity motor;
        private final double power;

        private MotorMachine(CreateElectricMotorBlockEntity motor, double power) {
            super(motor);
            this.motor = motor;
            this.power = power;
        }

        @Override
        public void subscribeEvents(IEventManager eventManager) {
            super.subscribeEvents(eventManager);
            eventManager.subscribe(SERVER_TICK.get(), level -> {
                motor.updateGeneratedRotation();
                motor.updateStressCapacity();
            });
        }

        @Override
        public long getVoltage() {
            return MachineBlock.getBlockVoltage(motor).value;
        }

        @Override
        public ElectricMachineType getMachineType() {
            return ElectricMachineType.CONSUMER;
        }

        @Override
        public double getPowerGen() {
            return 0d;
        }

        @Override
        public double getPowerCons() {
            return power;
        }

        @Override
        public void attachCapability(ICapabilityBuilder builder) {
            super.attachCapability(builder);
            builder.attach(ELECTRIC_MACHINE, this);
        }
    }
}
