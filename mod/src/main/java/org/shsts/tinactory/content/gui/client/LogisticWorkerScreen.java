package org.shsts.tinactory.content.gui.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.LogisticWorker;
import org.shsts.tinactory.content.logistics.LogisticWorkerConfig;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.StretchImage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.LOGISTIC_WORKER_CONFIGS;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.CONFIG_WIDTH;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.SLOT_SYNC;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.PANEL_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.PORT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.PORT_PADDING_TEXT;
import static org.shsts.tinactory.core.gui.Menu.PORT_TEXT_COLOR;
import static org.shsts.tinactory.core.gui.Menu.PORT_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.ALLOW_ARROW_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.integration.gui.InventoryMenu.INVENTORY_HEIGHT;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerScreen extends MenuScreen<LogisticWorkerMenu> {
    private static final int TOP_MARGIN = FONT_HEIGHT + SPACING;
    private static final int WIDTH = CONFIG_WIDTH + PANEL_WIDTH + PORT_WIDTH + 2 * MARGIN_X;

    private final int workerSlots;
    private final IMachineConfig machineConfig;
    private final LogisticWorker worker;
    private final Map<LogisticComponent.PortKey, LogisticWorkerSyncPacket.PortInfo> ports =
        new HashMap<>();
    private final ListMultimap<UUID, LogisticWorkerSyncPacket.PortInfo> machinePorts =
        ArrayListMultimap.create();

    private int selectedConfig = -1;
    private boolean selectedFrom;
    private final Runnable onConfigUpdate = this::refreshConfig;

    private class ConfigPanel extends ButtonPanel {
        private static final Texture BACKGROUND = new Texture(
            mcLoc("gui/sprites/container/enchanting_table/enchantment_slot_disabled"), 108, 19);
        private static final Rect FROM_RECT = new Rect(1, 0, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect TO_RECT = new Rect(BUTTON_SIZE * 2 + 2, 0, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect VALID_RECT = new Rect(BUTTON_SIZE + 2, 0, 20, 20);
        private static final Rect FILTER_RECT = new Rect(BUTTON_SIZE * 3 + 4, 2, 16, 16);
        private static final Rect BACKGROUND_TEX_RECT = new Rect(0, 1, 108, 18);

        private final FilterEntry.ClickHelper clickHelper = new FilterEntry.ClickHelper();

        public ConfigPanel() {
            super(LogisticWorkerScreen.this, CONFIG_WIDTH, BUTTON_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            var count = (int) machineConfig.get(LOGISTIC_WORKER_CONFIGS)
                .map(List::size).orElse(0);
            return Math.min(count + 1, workerSlots);
        }

        private Optional<ItemStack> getIcon(LogisticComponent.PortKey key) {
            return Optional.ofNullable(ports.get(key))
                .map(LogisticWorkerSyncPacket.PortInfo::icon);
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            var config = getConfig(index);

            var from = config.optionalFrom().flatMap(this::getIcon).orElse(ItemStack.EMPTY);
            var to = config.optionalTo().flatMap(this::getIcon).orElse(ItemStack.EMPTY);

            var isFrom = selectedConfig == index && selectedFrom;
            var isTo = selectedConfig == index && !selectedFrom;
            var isValid = config.valid();
            var fromRect = rect.offsetLike(FROM_RECT);
            var validRect = rect.offsetLike(VALID_RECT);
            var toRect = rect.offsetLike(TO_RECT);
            var filterRect = rect.offsetLike(FILTER_RECT);

            if (index - page * gridViewGroup.getSlotCount() == 0) {
                StretchImage.render(graphics, BACKGROUND, rect.offset(0, -1).enlarge(0, 1), 1);
            } else {
                StretchImage.render(graphics, BACKGROUND, rect, BACKGROUND_TEX_RECT, 1);
            }
            RenderUtil.blit(graphics, RECIPE_BUTTON, fromRect, isFrom ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(graphics, RECIPE_BUTTON, toRect, isTo ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(graphics,
                ALLOW_ARROW_BUTTON, validRect, 0, isValid ? ALLOW_ARROW_BUTTON.height() / 2 : 0);
            RenderUtil.renderItem(graphics, from, fromRect.offset(2, 2).resize(16, 16));
            RenderUtil.renderItem(graphics, to, toRect.offset(2, 2).resize(16, 16));

            RenderUtil.renderDescriptor(graphics, config.filter().display(), filterRect);
            if (FILTER_RECT.in(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(graphics, filterRect);
            }
        }

        @Override
        protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
            if (FROM_RECT.in(mouseX, mouseY) || TO_RECT.in(mouseX, mouseY) ||
                VALID_RECT.in(mouseX, mouseY) || FILTER_RECT.in(mouseX, mouseY)) {
                return button == 0 || button == 1;
            }
            return false;
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            updateConfig(index, config -> {
                if (FROM_RECT.in(mouseX, mouseY)) {
                    if (button == 0) {
                        selectedConfig = index;
                        selectedFrom = true;
                        config.optionalFrom().ifPresent(p -> machinePanel.select(p.machineId()));
                    } else {
                        return new UpdateConfigAction(false, config.resetFrom());
                    }
                } else if (TO_RECT.in(mouseX, mouseY)) {
                    if (button == 0) {
                        selectedConfig = index;
                        selectedFrom = false;
                        config.optionalTo().ifPresent(p -> machinePanel.select(p.machineId()));
                    } else {
                        return new UpdateConfigAction(false, config.resetTo());
                    }
                } else if (VALID_RECT.in(mouseX, mouseY)) {
                    if (button == 0) {
                        return new UpdateConfigAction(false, config.setValid(!config.valid()));
                    } else {
                        if (selectedConfig == index) {
                            selectedConfig = -1;
                        }
                        return new UpdateConfigAction(true, null);
                    }
                } else if (FILTER_RECT.in(mouseX, mouseY)) {
                    var filter = config.filter();
                    var filter1 = filter.click(index, clickHelper, button, menu.getCarried(),
                        true, true, true);
                    if (filter1.isPresent() && !filter1.get().equals(filter)) {
                        return new UpdateConfigAction(false, config.setFilter(filter1.get()));
                    }
                }
                return new UpdateConfigAction(false, null);
            });
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            var config = getConfig(index);

            var port = Optional.<LogisticWorkerSyncPacket.PortInfo>empty();
            if (FROM_RECT.in(mouseX, mouseY)) {
                port = config.optionalFrom().flatMap(k -> Optional.ofNullable(ports.get(k)));
            } else if (TO_RECT.in(mouseX, mouseY)) {
                port = config.optionalTo().flatMap(k -> Optional.ofNullable(ports.get(k)));
            } else if (FILTER_RECT.in(mouseX, mouseY)) {
                return config.filter().tooltip();
            }

            return port.map(p -> List.of(p.machineName(), p.portName()));
        }
    }

    private class PortSelectPanel extends ButtonPanel {
        public PortSelectPanel() {
            super(LogisticWorkerScreen.this, PORT_WIDTH, PORT_HEIGHT, 0);
        }

        @Override
        protected int getItemCount() {
            return machinePanel.getSelected()
                .map($ -> machinePorts.get($).size())
                .orElse(0);
        }

        private boolean isSelected(LogisticWorkerSyncPacket.PortInfo port) {
            if (selectedConfig < 0) {
                return false;
            }
            var config = getConfig(selectedConfig);
            var port1 = selectedFrom ? config.optionalFrom() : config.optionalTo();
            return port1.filter(portKey -> port.machineId().equals(portKey.machineId()) &&
                port.portIndex() == portKey.portIndex()).isPresent();
        }

        private Optional<LogisticWorkerSyncPacket.PortInfo> getPort(int index) {
            return machinePanel.getSelected()
                .flatMap(selected -> {
                    if (!machinePorts.containsKey(selected)) {
                        return Optional.empty();
                    }
                    var ports = machinePorts.get(selected);
                    return index >= ports.size() ? Optional.empty() : Optional.of(ports.get(index));
                });
        }

        @Override
        protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            getPort(index).ifPresent(port -> {
                var bgW = SWITCH_BUTTON.width();
                var bgH = SWITCH_BUTTON.height() / 2;
                var bg = new Rect(0, isSelected(port) ? bgH : 0, bgW, bgH);
                StretchImage.render(graphics, SWITCH_BUTTON, rect, bg, 3);

                RenderUtil.renderText(graphics, port.portName(),
                    rect.x() + SPACING, rect.y() + PORT_PADDING_TEXT,
                    PORT_TEXT_COLOR);
            });
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (selectedConfig == -1) {
                return;
            }
            getPort(index).ifPresent(port -> updateConfig(selectedConfig, config ->
                new UpdateConfigAction(false, selectedFrom ?
                    config.setFrom(port.machineId(), port.portIndex()) :
                    config.setTo(port.machineId(), port.portIndex()))));
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.empty();
        }
    }

    private final ConfigPanel configPanel;
    private final MachineSelectPanel<Void> machinePanel;
    private final PortSelectPanel portPanel;

    private static Component tr(String key) {
        return I18n.tr("tinactory.gui.logisticWorker." + key);
    }

    public LogisticWorkerScreen(LogisticWorkerMenu menu, Component title) {
        super(menu, title);
        this.contentWidth = WIDTH;
        this.contentHeight = menu.endY();

        var blockEntity = menu.blockEntity();
        this.machineConfig = menu.machine.config();
        this.worker = LogisticWorker.get(blockEntity);
        this.workerSlots = worker.workerSlots;

        this.configPanel = new ConfigPanel();
        this.machinePanel = new MachineSelectPanel<>(this) {
            @Override
            public void select(UUID machine) {
                super.select(machine);
                portPanel.refresh();
            }
        };
        this.portPanel = new PortSelectPanel();

        var offset1 = Rect.corners(0, TOP_MARGIN + 1, CONFIG_WIDTH, 0);
        var offset2 = Rect.corners(MARGIN_X + CONFIG_WIDTH + 1, TOP_MARGIN + 1,
            -MARGIN_X - PORT_WIDTH - 1, -1 - INVENTORY_HEIGHT);
        var offset3 = Rect.corners(-PORT_WIDTH, TOP_MARGIN, 0, 0);
        var anchor1 = RectD.corners(0d, 0d, 0d, 1d);
        var anchor3 = RectD.corners(1d, 0d, 1d, 1d);

        rootPanel.addChild(new Label(menu, tr("configLabel")));
        rootPanel.addChild(new Rect(offset2.x() - 1, 0, 0, 0), new Label(menu, tr("machineLabel")));
        rootPanel.addChild(RectD.corners(1d, 0d, 1d, 0d), Rect.corners(offset3.x(), 0, 0, 0),
            new Label(menu, tr("portLabel")));

        rootPanel.addChild(anchor1, offset1, configPanel);
        rootPanel.addGroup(offset2, machinePanel);
        rootPanel.addChild(anchor3, offset3, portPanel);

        menu.onSyncPacket(SLOT_SYNC, this::refreshVisiblePorts);
        worker.onConfigUpdate(onConfigUpdate);
    }

    @Override
    public void removed() {
        worker.unregisterConfigUpdateCallback(onConfigUpdate);
        super.removed();
    }

    private record UpdateConfigAction(boolean delete, @Nullable LogisticWorkerConfig newConfig) {}

    private void updateConfig(int index, Function<LogisticWorkerConfig, UpdateConfigAction> actionFunc) {
        var configList = machineConfig.get(LOGISTIC_WORKER_CONFIGS)
            .map(ArrayList::new)
            .orElseGet(ArrayList::new);
        var hasIndex = index >= 0 && index < configList.size();
        var oldConfig = hasIndex ? configList.get(index) : LogisticWorkerConfig.EMPTY;
        var action = actionFunc.apply(oldConfig);

        var updated = false;
        if (hasIndex && action.delete) {
            configList.remove(index);
            updated = true;
        } else if (action.newConfig != null) {
            if (hasIndex) {
                configList.set(index, action.newConfig);
                updated = true;
            } else if (configList.size() < workerSlots) {
                configList.add(index, action.newConfig);
                updated = true;
            }
        }

        if (updated) {
            var packet = SetMachineConfigPacket.builder()
                .set(LOGISTIC_WORKER_CONFIGS, configList);
            menu.triggerEvent(SET_MACHINE_CONFIG, packet);
        }
    }

    private void refreshConfig() {
        configPanel.refresh();
        if (selectedConfig >= configPanel.getItemCount()) {
            selectedConfig = -1;
        }
        portPanel.refresh();
    }

    private void refreshVisiblePorts(LogisticWorkerSyncPacket p) {
        machinePorts.clear();
        machinePanel.clearList();
        ports.clear();

        for (var port : p.ports()) {
            if (!machinePorts.containsKey(port.machineId())) {
                machinePanel.add(port.machineId(), port.machineName(), port.icon());
            }
            ports.put(port.getKey(), port);
            machinePorts.put(port.machineId(), port);
        }

        for (var machine : machinePorts.keySet()) {
            var l = machinePorts.get(machine);
            l.sort(Comparator.comparingInt(LogisticWorkerSyncPacket.PortInfo::portIndex));
        }

        machinePanel.refreshDisplayMachines();
        portPanel.refresh();
    }

    private LogisticWorkerConfig getConfig(int slot) {
        return machineConfig.get(LOGISTIC_WORKER_CONFIGS)
            .filter(list -> slot >= 0 && slot < list.size())
            .map(list -> list.get(slot))
            .orElse(LogisticWorkerConfig.EMPTY);
    }
}
