package org.shsts.tinactory.content.sound;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.DistExecutor;
import org.shsts.tinactory.integration.common.CapabilityProvider;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.function.Supplier;

import static org.shsts.tinactory.AllEvents.CLIENT_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSound extends CapabilityProvider implements IEventSubscriber {
    private static final String ID = "machine/sound";
    private final BlockEntity blockEntity;
    private final Supplier<SoundEvent> sound;
    @Nullable
    private Object instance = null;

    public MachineSound(BlockEntity blockEntity, Supplier<SoundEvent> sound) {
        this.blockEntity = blockEntity;
        this.sound = sound;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Supplier<SoundEvent> sound) {
        return builder -> builder.capability(ID, be -> new MachineSound(be, sound));
    }

    private void onClientTick(Level world) {
        if (world.isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::tickClient);
        }
    }

    private boolean isWorking(Level world) {
        if (blockEntity.isRemoved()) {
            return false;
        }
        var pos = blockEntity.getBlockPos();
        if (!world.isLoaded(pos)) {
            return false;
        }
        var state = world.getBlockState(pos);
        return state.hasProperty(MachineBlock.WORKING) && state.getValue(MachineBlock.WORKING);
    }

    @OnlyIn(Dist.CLIENT)
    private void tickClient() {
        if (instance instanceof MachineSoundInstance soundInstance && soundInstance.isStopped()) {
            instance = null;
        }
        if (instance != null) {
            return;
        }
        var world = blockEntity.getLevel();
        if (world == null || !world.isClientSide || !isWorking(world)) {
            return;
        }
        var soundInstance = new MachineSoundInstance(blockEntity, sound.get());
        Minecraft.getInstance().getSoundManager().play(soundInstance);
        instance = soundInstance;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(CLIENT_TICK.get(), this::onClientTick);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.empty();
    }
}
