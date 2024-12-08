package org.shsts.tinactory.core.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ProcessingMenu extends Menu<BlockEntity, ProcessingMenu> {
    @Nullable
    public final Layout layout;
    private final Map<Layout.SlotInfo, Integer> fluidSyncIndex = new HashMap<>();
    private final int progressBarIndex;

    public ProcessingMenu(SmartMenuType<?, ?> type, int id, Inventory inventory,
        BlockEntity blockEntity, @Nullable Layout layout) {
        super(type, id, inventory, blockEntity);
        var isPrimitive = blockEntity.getBlockState().getBlock() instanceof PrimitiveBlock<?>;

        this.layout = layout;
        if (layout != null) {
            var xOffset = layout.getXOffset();
            for (var slot : layout.slots) {
                var x = xOffset + slot.x();
                var y = slot.y();
                switch (slot.type().portType) {
                    case ITEM -> addSlot(slot.index(), x, y);
                    case FLUID -> fluidSyncIndex.put(slot, addFluidSlot(slot.index()));
                }
            }
            if (layout.progressBar != null) {
                this.progressBarIndex = addSyncSlot(MenuSyncPacket.Double::new,
                    be -> Machine.getProcessor(be)
                        .map(IProcessor::getProgress)
                        .orElse(0d));
            } else {
                this.progressBarIndex = -1;
            }
            this.height = layout.rect.endY() + (isPrimitive ? SLOT_SIZE / 2 : 0);
        } else {
            this.progressBarIndex = -1;
        }

        AllCapabilities.MACHINE.tryGet(blockEntity).ifPresent(Machine::sendUpdate);
    }

    public abstract Optional<RecipeType<?>> getRecipeType();

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen1<ProcessingMenu> createScreen(Inventory inventory, Component title) {
        var screen = new MenuScreen1<>(this, inventory, title);

        if (layout != null) {
            var layoutPanel = new Panel(screen);

            for (var slot : layout.slots) {
                if (slot.type().portType == PortType.FLUID) {
                    var syncSlot = fluidSyncIndex.get(slot);
                    var rect = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
                    var rect1 = rect.offset(1, 1).enlarge(-2, -2);
                    layoutPanel.addWidget(rect, new StaticWidget(this, Texture.SLOT_BACKGROUND));
                    layoutPanel.addWidget(rect1, new FluidSlot(this, slot.index(), syncSlot));
                }
            }

            for (var image : layout.images) {
                layoutPanel.addWidget(image.rect(), new StaticWidget(this, image.texture()));
            }

            var progressBar = layout.progressBar;
            if (progressBar != null) {
                var widget = new ProgressBar(this, progressBar.texture(), progressBarIndex);
                layoutPanel.addWidget(progressBar.rect(), widget);
            }

            screen.addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
        }

        return screen;
    }

    public static <T extends BlockEntity> Menu.Factory<T, ProcessingMenu> machine(Layout layout) {
        return (type, id, inventory, be) -> new ProcessingMenu(type, id, inventory, be, layout) {
            @Override
            public Optional<RecipeType<?>> getRecipeType() {
                return Optional.empty();
            }
        };
    }

    public static <T extends BlockEntity> Menu.Factory<T, ProcessingMenu> machine(
        Layout layout, RecipeTypeEntry<?, ?> recipeType) {
        return (type, id, inventory, be) -> new ProcessingMenu(type, id, inventory, be, layout) {
            @Override
            public Optional<RecipeType<?>> getRecipeType() {
                return Optional.of(recipeType.get());
            }
        };
    }

    public static <T extends BlockEntity> Menu.Factory<T, ProcessingMenu> multiBlock() {
        return (type, id, inventory, be) -> {
            var multiBlockInterface = (MultiBlockInterface) AllCapabilities.MACHINE.get(be);
            return new ProcessingMenu(type, id, inventory, be,
                multiBlockInterface.getLayout().orElse(null)) {
                @Override
                public Optional<RecipeType<?>> getRecipeType() {
                    return multiBlockInterface.getRecipeType();
                }
            };
        };
    }

    public static Component getTitle(BlockEntity be) {
        return AllCapabilities.MACHINE.tryGet(be)
            .map(Machine::getTitle)
            .orElseGet(() -> I18n.name(be.getBlockState().getBlock()));
    }

    public static Component portLabel(PortType type, int index) {
        var key = "tinactory.gui.portName." + type.name().toLowerCase() + "Label";
        return tr(key, index);
    }
}
