package org.shsts.tinactory.compat.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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
import org.shsts.tinactory.integration.machine.Machine;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.blockentity.IBlockEntityContainer;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.MACHINE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CreateKineticGeneratorBlockEntity extends KineticBlockEntity {
    private static final String MACHINE_CONTAINER_ID = "network/machine";

    private final IBlockEntityContainer container;
    private final float stress;
    private final int rpm;
    private final double power;

    public CreateKineticGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
        IBlockEntityContainer container, float stress, int rpm, double power) {
        super(type, pos, state);
        this.container = container;
        this.stress = stress;
        this.rpm = rpm;
        this.power = power;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        float stress, int rpm, double power) {
        return builder -> builder
            .factory((type, pos, state, container) ->
                new CreateKineticGeneratorBlockEntity(type, pos, state, container, stress, rpm, power),
                CreateKineticGeneratorBlockEntity::container)
            .container(MACHINE_CONTAINER_ID, blockEntity ->
                new GeneratorMachine((CreateKineticGeneratorBlockEntity) blockEntity))
            .capability(MACHINE, ELECTRIC_MACHINE);
    }

    public IBlockEntityContainer container() {
        return container;
    }

    @Override
    public float calculateStressApplied() {
        return stress;
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
        return container.createUpdatePacket(registries -> super.getUpdateTag(registries))
            .map(ClientboundBlockEntityDataPacket.class::cast)
            .orElseGet(super::getUpdatePacket);
    }

    private static final class GeneratorMachine extends Machine implements IElectricMachine {
        private final CreateKineticGeneratorBlockEntity generator;

        private GeneratorMachine(CreateKineticGeneratorBlockEntity generator) {
            super(generator);
            this.generator = generator;
        }

        @Override
        public long getVoltage() {
            return MachineBlock.getBlockVoltage(generator).value;
        }

        @Override
        public ElectricMachineType getMachineType() {
            return ElectricMachineType.GENERATOR;
        }

        @Override
        public double getPowerGen() {
            return generator.power * Math.min(Math.abs(generator.getSpeed()) / generator.rpm, 1d);
        }

        @Override
        public double getPowerCons() {
            return 0d;
        }

        @Override
        public void attachCapability(ICapabilityBuilder builder) {
            super.attachCapability(builder);
            builder.attach(ELECTRIC_MACHINE, this);
        }
    }
}
