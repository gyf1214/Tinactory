package org.shsts.tinactory.integration.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMenu extends LayoutMenu {
    protected ProcessingMenu(Properties properties, int extraHeight,
        Function<FluidStack, IPacket> fluidSyncPacketFactory) {
        super(properties, extraHeight, fluidSyncPacketFactory);
        addSlots();
    }

    /**
     * Called during constructor.
     */
    private void addSlots() {
        addLayoutSlots(layout);
        addFluidSlots();
        addProgressBar();
    }

    public Optional<Block> machineBlock() {
        return MACHINE.tryGet(blockEntity())
            .flatMap(IMachine::workBlock)
            .map(BlockState::getBlock);
    }

    public static Component getTitle(BlockEntity be) {
        return MACHINE.tryGet(be)
            .map(IMachine::title)
            .orElse(TextComponent.EMPTY);
    }

    public static Component portLabel(PortType type, int index) {
        var key = "tinactory.gui.portName." + type.name().toLowerCase() + "Label";
        return tr(key, index);
    }

    public static class Primitive extends ProcessingMenu {
        public Primitive(Properties properties, Function<FluidStack, IPacket> fluidSyncPacketFactory) {
            super(properties, SLOT_SIZE / 2, fluidSyncPacketFactory);
        }
    }

    public static ProcessingMenu primitive(Properties properties,
        Function<FluidStack, IPacket> fluidSyncPacketFactory) {
        return new Primitive(properties, fluidSyncPacketFactory);
    }
}
