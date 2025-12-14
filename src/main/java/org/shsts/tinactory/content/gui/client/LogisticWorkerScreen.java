package org.shsts.tinactory.content.gui.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
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
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.CONFIG_WIDTH;
import static org.shsts.tinactory.content.gui.LogisticWorkerMenu.SLOT_SYNC;
import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.core.gui.InventoryMenu.INVENTORY_HEIGHT;
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

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerScreen extends MenuScreen<LogisticWorkerMenu> {
    private static final int TOP_MARGIN = FONT_HEIGHT + SPACING;
    private static final int WIDTH = CONFIG_WIDTH + PANEL_WIDTH + PORT_WIDTH + 2 * MARGIN_X;

    private final int workerSlots;
    private final IMachineConfig machineConfig;
    private final Map<LogisticComponent.PortKey, LogisticWorkerSyncPacket.PortInfo> ports =
        new HashMap<>();
    private final ListMultimap<UUID, LogisticWorkerSyncPacket.PortInfo> machinePorts =
        ArrayListMultimap.create();

    private int selectedConfig = -1;
    private boolean selectedFrom;

    private class ConfigPanel extends ButtonPanel {
        private static final Rect FROM_RECT = new Rect(1, 1, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect TO_RECT = new Rect(BUTTON_SIZE * 2 + 2, 1, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect VALID_RECT = new Rect(BUTTON_SIZE + 2, 1, 20, 20);
        private static final Rect FILTER_RECT = new Rect(BUTTON_SIZE * 3 + 4, 3, 16, 16);
        private static final Texture BACKGROUND_TEX = new Texture(
            mcLoc("gui/container/enchanting_table"), 256, 256);
        private static final Rect BG_TEX_RECT = new Rect(0, 185, 108, 19);

        @Nullable
        private TagKey<Item> tagFilter = null;
        @Nullable
        private List<ItemStack> tagFilterItems = null;
        @Nullable
        private List<TagKey<Item>> tagSelectList = null;
        private int nextSelectTag = 0;

        public ConfigPanel() {
            super(LogisticWorkerScreen.this, CONFIG_WIDTH, BUTTON_SIZE + 1, 0);
        }

        @Override
        protected int getItemCount() {
            return workerSlots;
        }

        private Optional<ItemStack> getIcon(LogisticComponent.PortKey key) {
            return Optional.ofNullable(ports.get(key))
                .map(LogisticWorkerSyncPacket.PortInfo::icon);
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

            switch (filterType) {
                case ITEM -> RenderUtil.renderItem(config.itemFilter(), filterRect.x(), filterRect.y());
                case FLUID -> RenderUtil.renderFluid(poseStack, config.fluidFilter(), filterRect, z);
                case TAG -> {
                    if (tagFilterItems == null || config.tagFilter() != tagFilter) {
                        var tags = ForgeRegistries.ITEMS.tags();
                        assert tags != null;
                        tagFilter = config.tagFilter();
                        tagFilterItems = tags.getTag(tagFilter).stream()
                            .map(ItemStack::new)
                            .toList();
                    }
                    RenderUtil.selectItemFromItems(tagFilterItems).ifPresent(stack ->
                        RenderUtil.renderItem(stack, filterRect.x(), filterRect.y()));
                }
            }

            if (FILTER_RECT.in(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(poseStack, filterRect);
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
            var config = getConfig(index);
            var needUpdate = false;

            if (FROM_RECT.in(mouseX, mouseY)) {
                if (button == 0) {
                    selectedConfig = index;
                    selectedFrom = true;
                    config.from().ifPresent(p -> machinePanel.select(p.machineId()));
                } else {
                    config.resetFrom();
                    needUpdate = true;
                }
            } else if (TO_RECT.in(mouseX, mouseY)) {
                if (button == 0) {
                    selectedConfig = index;
                    selectedFrom = false;
                    config.to().ifPresent(p -> machinePanel.select(p.machineId()));
                } else {
                    config.resetTo();
                    needUpdate = true;
                }
            } else if (VALID_RECT.in(mouseX, mouseY)) {
                if (button == 0) {
                    config.setValid(!config.isValid());
                } else {
                    config.setValid(false);
                    config.resetFrom();
                    config.resetTo();
                }
                needUpdate = true;
            } else if (FILTER_RECT.in(mouseX, mouseY)) {
                var carried = menu.getCarried();
                if (carried.isEmpty()) {
                    var tagSet = false;
                    if (button == 1) {
                        var filterType = config.filterType();
                        if (filterType == LogisticWorkerConfig.FilterType.ITEM) {
                            var tags = ForgeRegistries.ITEMS.tags();
                            assert tags != null;
                            var tagList = tags.getReverseTag(config.itemFilter().getItem())
                                .map($ -> $.getTagKeys()
                                    .sorted(Comparator.comparing(TagKey::location,
                                        ResourceLocation::compareNamespaced))
                                    .toList())
                                .orElse(Collections.emptyList());
                            if (!tagList.isEmpty()) {
                                tagSelectList = tagList;
                                config.setFilter(tagSelectList.get(0));
                                nextSelectTag = tagList.size() == 1 ? 0 : 1;
                                tagSet = true;
                            }
                        } else if (filterType == LogisticWorkerConfig.FilterType.TAG && tagSelectList != null) {
                            config.setFilter(tagSelectList.get(nextSelectTag));
                            nextSelectTag = (nextSelectTag + 1) % tagSelectList.size();
                            tagSet = true;
                        }
                    }
                    if (!tagSet) {
                        config.clearFilter();
                        tagSelectList = null;
                    }
                } else {
                    var fluid = StackHelper.getFluidHandlerFromItem(carried)
                        .filter($ -> button == 0)
                        .flatMap(handler -> {
                            var stack = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                            return stack.isEmpty() ? Optional.empty() :
                                Optional.of(StackHelper.copyWithAmount(stack, 1));
                        });
                    fluid.ifPresentOrElse(config::setFilter, () ->
                        config.setFilter(StackHelper.copyWithCount(carried, 1)));
                    tagSelectList = null;
                }

                tagFilterItems = null;
                needUpdate = true;
            }

            if (needUpdate) {
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
                    case ITEM -> Optional.of(ClientUtil.itemTooltip(config.itemFilter()));
                    case TAG -> Optional.of(ClientUtil.tagTooltip(config.tagFilter()));
                    case FLUID -> Optional.of(ClientUtil.fluidTooltip(config.fluidFilter(), false));
                };
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
            var port1 = selectedFrom ? config.from() : config.to();
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
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            getPort(index).ifPresent(port -> {
                var bgW = SWITCH_BUTTON.width();
                var bgH = SWITCH_BUTTON.height() / 2;
                var bg = new Rect(0, isSelected(port) ? bgH : 0, bgW, bgH);
                StretchImage.render(poseStack, SWITCH_BUTTON, getBlitOffset(), rect, bg, 3);

                RenderUtil.renderText(poseStack, port.portName(),
                    rect.x() + SPACING, rect.y() + PORT_PADDING_TEXT,
                    PORT_TEXT_COLOR);
            });
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (selectedConfig == -1) {
                return;
            }
            getPort(index).ifPresent(port -> {
                var config = getConfig(selectedConfig);
                if (selectedFrom) {
                    config.setFrom(port.machineId(), port.portIndex());
                } else {
                    config.setTo(port.machineId(), port.portIndex());
                }
                var packet = SetMachineConfigPacket.builder()
                    .set(PREFIX + selectedConfig, config.serializeNBT());
                menu.triggerEvent(SET_MACHINE_CONFIG, packet);
            });
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.empty();
        }
    }

    private final MachineSelectPanel machinePanel;
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
        this.workerSlots = LogisticWorker.get(blockEntity).workerSlots;

        var configPanel = new ConfigPanel();
        this.machinePanel = new MachineSelectPanel(this) {
            @Override
            public void select(UUID machine) {
                super.select(machine);
                portPanel.refresh();
            }
        };
        this.portPanel = new PortSelectPanel();

        var offset1 = Rect.corners(0, TOP_MARGIN, CONFIG_WIDTH, 0);
        var offset2 = Rect.corners(MARGIN_X + CONFIG_WIDTH + 1, TOP_MARGIN + 1,
            -MARGIN_X - PORT_WIDTH - 1, -1 - INVENTORY_HEIGHT);
        var offset3 = Rect.corners(-PORT_WIDTH, TOP_MARGIN, 0, 0);
        var anchor1 = RectD.corners(0d, 0d, 0d, 1d);
        var anchor3 = RectD.corners(1d, 0d, 1d, 1d);

        addWidget(new Label(menu, tr("configLabel")));
        addWidget(new Rect(offset2.x() - 1, 0, 0, 0), new Label(menu, tr("machineLabel")));
        addWidget(RectD.corners(1d, 0d, 1d, 0d), Rect.corners(offset3.x(), 0, 0, 0),
            new Label(menu, tr("portLabel")));

        addPanel(anchor1, offset1, configPanel);
        addPanel(offset2, machinePanel);
        addPanel(anchor3, offset3, portPanel);

        menu.onSyncPacket(SLOT_SYNC, this::refreshVisiblePorts);
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

        machinePanel.refresh();
        portPanel.refresh();
    }

    private LogisticWorkerConfig getConfig(int slot) {
        return machineConfig.getCompound(PREFIX + slot)
            .map(LogisticWorkerConfig::fromTag)
            .orElseGet(LogisticWorkerConfig::new);
    }
}
