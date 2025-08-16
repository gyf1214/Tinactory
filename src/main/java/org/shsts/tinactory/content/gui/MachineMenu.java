package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.gui.LayoutMenu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.machine.Boiler.getHeat;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.machine.Machine.getProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineMenu extends ProcessingMenu {
    public MachineMenu(Properties properties) {
        super(properties, SLOT_SIZE + SPACING);
        onEventPacket(SET_MACHINE_CONFIG, p -> MACHINE.get(blockEntity).setConfig(p));
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && canPlayerInteract(blockEntity, player);
    }

    public static boolean canPlayerInteract(BlockEntity blockEntity, Player player) {
        return MACHINE.tryGet(blockEntity)
            .filter($ -> $.canPlayerInteract(player))
            .isPresent();
    }

    public static class Simple extends LayoutMenu {
        private Simple(Properties properties) {
            super(properties, 0);
            addLayoutSlots(layout);
        }

        @Override
        public boolean stillValid(Player player) {
            return super.stillValid(player) && canPlayerInteract(blockEntity, player);
        }
    }

    public static class Multiblock extends MachineMenu {
        public Multiblock(Properties properties) {
            super(properties);
        }

        @Override
        public Optional<IRecipeType<?>> recipeType() {
            var multiblockInterface = (MultiblockInterface) MACHINE.get(blockEntity);
            return multiblockInterface.getRecipeType();
        }
    }

    public static class Boiler extends MachineMenu {
        public Boiler(Properties properties) {
            super(properties);
            addSyncSlot("burn", () -> new SyncPackets.Double(
                getProcessor(blockEntity)
                    .map(IProcessor::getProgress)
                    .orElse(0d)));
            addSyncSlot("heat", () -> new SyncPackets.Double(
                getProcessor(blockEntity)
                    .map($ -> getHeat($) / 500d)
                    .orElse(0d)));
        }
    }

    public static LayoutMenu simple(Properties properties) {
        return new Simple(properties);
    }

    public static ProcessingMenu machine(Properties properties) {
        return new MachineMenu(properties);
    }

    public static ProcessingMenu multiblock(Properties properties) {
        return new Multiblock(properties);
    }

    public static ProcessingMenu boiler(Properties properties) {
        return new Boiler(properties);
    }
}
