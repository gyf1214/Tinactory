package org.shsts.tinactory.content.gui.client;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.logistics.LogisticWorkerConfig;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.BUTTON_SIZE;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.CONFIG_WIDTH;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.PORT_WIDTH;
import static org.shsts.tinactory.content.gui.NetworkControllerMenu.PANEL_BORDER;
import static org.shsts.tinactory.content.gui.client.TechPanel.BUTTON_PANEL_BG;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.core.gui.InventoryMenu.INVENTORY_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerScreen extends MenuScreen<LogisticWorkerMenu> {
    private record MachineInfo(UUID id, Component name, ItemStack icon) {}

    private final int workerSlots;
    private final IMachineConfig machineConfig;
    private final Map<UUID, MachineInfo> machines = new HashMap<>();
    private final List<MachineInfo> machineList = new ArrayList<>();
    private final Map<LogisticComponent.PortKey, LogisticWorkerSyncPacket.PortInfo> ports =
        new HashMap<>();
    private final ArrayListMultimap<UUID, LogisticWorkerSyncPacket.PortInfo> machinePorts =
        ArrayListMultimap.create();

    private int selectedConfig = -1;
    private boolean selectedFrom;
    @Nullable
    private UUID selectedMachine = null;

    private static final int TOP_MARGIN = FONT_HEIGHT + SPACING;

    private class ConfigPanel extends ButtonPanel {
        private static final Rect FROM_RECT = new Rect(1, 1, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect TO_RECT = new Rect(BUTTON_SIZE * 2 + 2, 1, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect VALID_RECT = new Rect(BUTTON_SIZE + 2, 1, 20, 20);
        private static final Rect FILTER_RECT = new Rect(BUTTON_SIZE * 3 + 4, 3, 16, 16);
        private static final Texture BACKGROUND_TEX = new Texture(
            mcLoc("gui/container/enchanting_table"), 256, 256);
        private static final Rect BG_TEX_RECT = new Rect(0, 185, 108, 19);

        public ConfigPanel() {
            super(LogisticWorkerScreen.this, CONFIG_WIDTH, BUTTON_SIZE + 1, 0);
        }

        @Override
        protected int getItemCount() {
            return workerSlots;
        }

        private Optional<ItemStack> getIcon(LogisticComponent.PortKey key) {
            return machines.containsKey(key.machineId()) ?
                Optional.of(machines.get(key.machineId()).icon()) : Optional.empty();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            var config = getConfig(index);
            var z = getBlitOffset();

            var from = config.from().flatMap(this::getIcon).orElse(ItemStack.EMPTY);
            var to = config.to().flatMap(this::getIcon).orElse(ItemStack.EMPTY);
            var filterType = config.filterType();

            var isFrom = selectedConfig == index && selectedFrom;
            var isTo = selectedConfig == index && !selectedFrom;
            var isValid = config.isValid();
            var fromRect = rect.offsetLike(FROM_RECT);
            var validRect = rect.offsetLike(VALID_RECT);
            var toRect = rect.offsetLike(TO_RECT);
            var filterRect = rect.offsetLike(FILTER_RECT);

            StretchImage.render(poseStack, BACKGROUND_TEX, z, rect, BG_TEX_RECT, 2);
            RenderUtil.blit(poseStack, RECIPE_BUTTON, z, fromRect, isFrom ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(poseStack, RECIPE_BUTTON, z, toRect, isTo ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(poseStack,
                ALLOW_ARROW_BUTTON, z, validRect, 0, isValid ? ALLOW_ARROW_BUTTON.height() / 2 : 0);
            RenderUtil.renderItem(from, fromRect.x() + 2, fromRect.y() + 2);
            RenderUtil.renderItem(to, toRect.x() + 2, toRect.y() + 2);

            if (filterType == PortType.ITEM) {
                RenderUtil.renderItem(config.itemFilter(), filterRect.x(), filterRect.y());
            } else if (filterType == PortType.FLUID) {
                RenderUtil.renderFluid(poseStack, config.fluidFilter(), filterRect, z);
            }

            if (FILTER_RECT.in(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(poseStack, filterRect);
            }
        }

        @Override
        protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
            if (FROM_RECT.in(mouseX, mouseY) || TO_RECT.in(mouseX, mouseY) ||
                VALID_RECT.in(mouseX, mouseY)) {
                return button == 0;
            } else if (FILTER_RECT.in(mouseX, mouseY)) {
                return button == 0 || button == 1;
            }
            return false;
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            var config = getConfig(index);

            if (FROM_RECT.in(mouseX, mouseY)) {
                selectedConfig = index;
                selectedFrom = true;
                config.from().ifPresent(p -> selectMachine(p.machineId()));
            } else if (TO_RECT.in(mouseX, mouseY)) {
                selectedConfig = index;
                selectedFrom = false;
                config.to().ifPresent(p -> selectMachine(p.machineId()));
            } else if (VALID_RECT.in(mouseX, mouseY)) {
                config.setValid(!config.isValid());
                var packet = SetMachineConfigPacket.builder()
                    .set(PREFIX + index, config.serializeNBT());
                menu.triggerEvent(SET_MACHINE_CONFIG, packet);
            } else if (FILTER_RECT.in(mouseX, mouseY)) {
                var carried = menu.getCarried();
                if (carried.isEmpty()) {
                    config.clearFilter();
                } else {
                    var fluid = StackHelper.getFluidHandlerFromItem(carried)
                        .filter($ -> button == 0)
                        .flatMap(handler -> {
                            var stack = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                            return stack.isEmpty() ? Optional.empty() :
                                Optional.of(StackHelper.copyWithAmount(stack, 1));
                        });
                    fluid.ifPresentOrElse(config::setFilter, () -> config.setFilter(carried));
                }

                var packet = SetMachineConfigPacket.builder()
                    .set(PREFIX + index, config.serializeNBT());
                menu.triggerEvent(SET_MACHINE_CONFIG, packet);
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            var config = getConfig(index);

            var port = Optional.<LogisticWorkerSyncPacket.PortInfo>empty();
            if (FROM_RECT.in(mouseX, mouseY)) {
                port = config.from().flatMap(k -> Optional.ofNullable(ports.get(k)));
            } else if (TO_RECT.in(mouseX, mouseY)) {
                port = config.to().flatMap(k -> Optional.ofNullable(ports.get(k)));
            } else if (FILTER_RECT.in(mouseX, mouseY)) {
                return switch (config.filterType()) {
                    case NONE -> Optional.empty();
                    case FLUID -> Optional.of(ClientUtil.fluidTooltip(config.fluidFilter(), false));
                    case ITEM -> Optional.of(ClientUtil.itemTooltip(config.itemFilter()));
                };
            }

            return port.map(p -> List.of(p.machineName(), p.portName()));
        }
    }

    private class MachineSelectPanel extends ButtonPanel {
        public MachineSelectPanel() {
            super(LogisticWorkerScreen.this, BUTTON_SIZE, BUTTON_SIZE, 1);
        }

        @Override
        protected int getItemCount() {
            return machineList.size();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            var machine = machineList.get(index);
            RenderUtil.blit(poseStack, RECIPE_BUTTON, getBlitOffset(), rect,
                machine.id.equals(selectedMachine) ? 21 : 0, 0);
            RenderUtil.renderItem(machine.icon, rect.x() + 2, rect.y() + 2);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            selectMachine(machineList.get(index).id);
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.of(List.of(machineList.get(index).name));
        }
    }

    private class PortSelectPanel extends ButtonPanel {
        private static final int X_OFFSET = SPACING;
        private static final int Y_OFFSET = (BUTTON_SIZE + 2 - FONT_HEIGHT) / 2 + 1;

        public PortSelectPanel() {
            super(LogisticWorkerScreen.this, PORT_WIDTH, BUTTON_SIZE + 2, 0);
        }

        @Override
        protected int getItemCount() {
            return selectedMachine == null ? 0 : machinePorts.get(selectedMachine).size();
        }

        private boolean isSelected(LogisticWorkerSyncPacket.PortInfo port) {
            if (selectedConfig < 0) {
                return false;
            }
            var config = getConfig(selectedConfig);
            var port1 = selectedFrom ? config.from() : config.to();
            return port1.filter(portKey -> port.machineId().equals(portKey.machineId()) &&
                port.portIndex() == portKey.portIndex()).isPresent();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            if (selectedMachine == null) {
                return;
            }
            var ports = machinePorts.get(selectedMachine);
            if (index >= ports.size()) {
                return;
            }
            var port = ports.get(index);
            var texRect = new Rect(0, isSelected(port) ? SWITCH_BUTTON.height() / 2 : 0,
                SWITCH_BUTTON.width(), SWITCH_BUTTON.height() / 2);
            isSelected(port);
            StretchImage.render(poseStack, SWITCH_BUTTON, getBlitOffset(), rect, texRect, 3);

            RenderUtil.renderText(poseStack, port.portName(),
                rect.x() + X_OFFSET, rect.y() + Y_OFFSET, PortPanel.TEXT_COLOR);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (selectedConfig == -1 || selectedMachine == null) {
                return;
            }
            var port = machinePorts.get(selectedMachine).get(index);

            var config = getConfig(selectedConfig);
            if (selectedFrom) {
                config.setFrom(port.machineId(), port.portIndex());
            } else {
                config.setTo(port.machineId(), port.portIndex());
            }
            var packet = SetMachineConfigPacket.builder()
                .set(PREFIX + selectedConfig, config.serializeNBT());
            menu.triggerEvent(SET_MACHINE_CONFIG, packet);
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.empty();
        }
    }

    private final MachineSelectPanel machineSelectPanel;
    private final PortSelectPanel portSelectPanel;

    private static Component tr(String key) {
        return I18n.tr("tinactory.gui.logisticWorker." + key);
    }

    public LogisticWorkerScreen(LogisticWorkerMenu menu, Component title) {
        super(menu, title);
        this.contentWidth = LogisticWorkerMenu.WIDTH;
        this.contentHeight = menu.endY();

        var blockEntity = menu.blockEntity();
        this.machineConfig = MACHINE.get(blockEntity).config();
        this.workerSlots = LogisticWorker.get(blockEntity).workerSlots;

        var configPanel = new ConfigPanel();
        this.machineSelectPanel = new MachineSelectPanel();
        this.portSelectPanel = new PortSelectPanel();

        var offset1 = Rect.corners(0, TOP_MARGIN, CONFIG_WIDTH, 0);
        var offset2 = Rect.corners(MARGIN_X + CONFIG_WIDTH + 1, TOP_MARGIN + 1,
            -MARGIN_X - PORT_WIDTH - 1, -1 - INVENTORY_HEIGHT);
        var offset3 = Rect.corners(-PORT_WIDTH, TOP_MARGIN, 0, 0);
        var anchor1 = RectD.corners(0d, 0d, 0d, 1d);
        var anchor2 = RectD.corners(0d, 0d, 1d, 1d);
        var anchor3 = RectD.corners(1d, 0d, 1d, 1d);

        addWidget(new Label(menu, tr("configLabel")));
        addWidget(new Rect(offset2.x() - 1, 0, 0, 0), new Label(menu, tr("machineLabel")));
        addWidget(RectD.corners(1d, 0d, 1d, 0d), Rect.corners(offset3.x(), 0, 0, 0),
            new Label(menu, tr("portLabel")));

        var bg = new StretchImage(menu, RECIPE_BOOK_BG, BUTTON_PANEL_BG, PANEL_BORDER);
        addWidget(anchor2, offset2.offset(-2, -2).enlarge(4, 4), bg);

        addPanel(anchor1, offset1, configPanel);
        addPanel(anchor2, offset2, machineSelectPanel);
        addPanel(anchor3, offset3, portSelectPanel);

        menu.onSyncPacket("info", this::refreshVisiblePorts);
    }

    private void refreshVisiblePorts(LogisticWorkerSyncPacket p) {
        machinePorts.clear();
        machines.clear();
        machineList.clear();
        ports.clear();

        for (var port : p.ports()) {
            if (!machinePorts.containsKey(port.machineId())) {
                var info = new MachineInfo(port.machineId(), port.machineName(), port.icon());
                machineList.add(info);
                machines.put(port.machineId(), info);
            }
            ports.put(port.getKey(), port);
            machinePorts.put(port.machineId(), port);
        }

        for (var machine : machinePorts.keySet()) {
            var l = machinePorts.get(machine);
            l.sort(Comparator.comparingInt(LogisticWorkerSyncPacket.PortInfo::portIndex));
        }

        if (!machines.containsKey(selectedMachine)) {
            selectedMachine = null;
        }

        machineSelectPanel.refresh();
        portSelectPanel.refresh();
    }

    private void selectMachine(UUID id) {
        selectedMachine = machines.containsKey(id) ? id : null;
        portSelectPanel.refresh();
    }

    private LogisticWorkerConfig getConfig(int slot) {
        return machineConfig.getCompound(PREFIX + slot)
            .map(LogisticWorkerConfig::fromTag)
            .orElseGet(LogisticWorkerConfig::new);
    }
}
