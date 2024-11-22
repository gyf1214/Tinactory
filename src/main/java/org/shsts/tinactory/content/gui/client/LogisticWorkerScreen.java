package org.shsts.tinactory.content.gui.client;

import com.google.common.collect.ArrayListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.LogisticWorkerMenu;
import org.shsts.tinactory.content.gui.sync.LogisticWorkerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.LogisticWorkerConfig;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.logistics.LogisticWorkerConfig.PREFIX;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LogisticWorkerScreen extends MenuScreen<LogisticWorkerMenu> {
    private record MachineInfo(UUID id, Component name, ItemStack icon) {}

    private final int configSlots;
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

    private static final int BUTTON_SIZE = AbstractRecipeBook.BUTTON_SIZE;
    private static final int IMAGE_WIDTH = NetworkControllerScreen.WIDTH;
    private static final int IMAGE_HEIGHT = NetworkControllerScreen.HEIGHT;

    private class ConfigPanel extends ButtonPanel {
        private static final int WIDTH = BUTTON_SIZE * 3 + SPACING * 2;
        private static final Rect FROM_RECT = new Rect(0, 0, BUTTON_SIZE, BUTTON_SIZE);
        private static final Rect TO_RECT = FROM_RECT.offset(BUTTON_SIZE * 2 + SPACING * 2, 0);
        private static final Rect VALID_RECT = FROM_RECT.offset(BUTTON_SIZE + SPACING, 0);
        private static final Texture VALID_TEX = new Texture(
            gregtech("gui/widget/button_allow_import_export"), 20, 40);

        public ConfigPanel() {
            super(LogisticWorkerScreen.this, WIDTH, BUTTON_SIZE, SPACING);
        }

        @Override
        protected int getItemCount() {
            return configSlots;
        }

        private Optional<ItemStack> getIcon(LogisticComponent.PortKey key) {
            return machines.containsKey(key.machineId()) ?
                Optional.of(machines.get(key.machineId()).icon()) : Optional.empty();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index) {
            var config = getConfig(index);
            var z = getBlitOffset();

            var from = config.from().flatMap(this::getIcon).orElse(ItemStack.EMPTY);
            var to = config.to().flatMap(this::getIcon).orElse(ItemStack.EMPTY);

            var isFrom = selectedConfig == index && selectedFrom;
            var isTo = selectedConfig == index && !selectedFrom;
            var isValid = config.isValid();
            var fromRect = rect.resize(BUTTON_SIZE, BUTTON_SIZE);
            var toRect = rect.offset(BUTTON_SIZE * 2 + SPACING * 2, 0)
                .resize(BUTTON_SIZE, BUTTON_SIZE);
            var validRect = rect.offset(BUTTON_SIZE + SPACING, 1)
                .resize(VALID_TEX.width(), VALID_TEX.height() / 2);

            RenderUtil.blit(poseStack, Texture.RECIPE_BUTTON, z, fromRect, isFrom ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(poseStack, Texture.RECIPE_BUTTON, z, toRect, isTo ? BUTTON_SIZE : 0, 0);
            RenderUtil.blit(poseStack, VALID_TEX, z, validRect, 0, isValid ? VALID_TEX.height() / 2 : 0);
            RenderUtil.renderItem(from, fromRect.x() + 2, fromRect.y() + 2);
            RenderUtil.renderItem(to, toRect.x() + 2, toRect.y() + 2);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY) {
            var config = getConfig(index);

            if (FROM_RECT.in(mouseX, mouseY)) {
                selectedConfig = index;
                selectedFrom = true;
                config.from().ifPresent(p -> selectedMachine = p.machineId());
            } else if (TO_RECT.in(mouseX, mouseY)) {
                selectedConfig = index;
                selectedFrom = false;
                config.to().ifPresent(p -> selectedMachine = p.machineId());
            } else if (VALID_RECT.in(mouseX, mouseY)) {
                config.setValid(!config.isValid());
                var packet = SetMachineConfigPacket.builder()
                    .set(PREFIX + index, config.serializeNBT());
                menu.triggerEvent(MenuEventHandler.SET_MACHINE_CONFIG, packet);
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
            }

            return port.map(p -> List.of(p.machineName(), p.portName()));
        }
    }

    private class MachinePanel extends ButtonPanel {
        public MachinePanel() {
            super(LogisticWorkerScreen.this, BUTTON_SIZE, BUTTON_SIZE, SPACING);
        }

        @Override
        protected int getItemCount() {
            return machineList.size();
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index) {
            var machine = machineList.get(index);
            RenderUtil.blit(poseStack, Texture.RECIPE_BUTTON, getBlitOffset(), rect,
                machine.id.equals(selectedMachine) ? 21 : 0, 0);
            RenderUtil.renderItem(machine.icon, rect.x() + 2, rect.y() + 2);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY) {
            selectedMachine = machineList.get(index).id;
            portPanel.refresh();
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.of(List.of(machineList.get(index).name));
        }
    }

    private class PortPanel extends ButtonPanel {
        private static final int WIDTH = 42;
        private static final int X_OFFSET = SPACING;
        private static final int Y_OFFSET = (BUTTON_SIZE - 9) / 2;

        public PortPanel() {
            super(LogisticWorkerScreen.this, WIDTH, BUTTON_SIZE, SPACING);
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
            float partialTick, Rect rect, int index) {
            if (selectedMachine == null) {
                return;
            }
            var ports = machinePorts.get(selectedMachine);
            if (index >= ports.size()) {
                return;
            }
            var port = ports.get(index);
            var tex = Texture.SWITCH_BUTTON;
            var texRect = new Rect(0, isSelected(port) ? tex.height() / 2 : 0,
                tex.width(), tex.height() / 2);
            isSelected(port);
            StretchImage.render(poseStack, tex, getBlitOffset(), rect, texRect, 3);

            RenderUtil.renderText(poseStack, port.portName(),
                rect.x() + X_OFFSET, rect.y() + Y_OFFSET, 0xFF202020);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY) {
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
            menu.triggerEvent(MenuEventHandler.SET_MACHINE_CONFIG, packet);
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return Optional.empty();
        }
    }

    private final MachinePanel machinePanel;
    private final PortPanel portPanel;

    public LogisticWorkerScreen(LogisticWorkerMenu menu, Inventory inventory,
        Component title, int configSlots) {
        super(menu, inventory, title);
        this.configSlots = configSlots;

        var configPanel = new ConfigPanel();
        this.machinePanel = new MachinePanel();
        this.portPanel = new PortPanel();

        addPanel(RectD.corners(0d, 0d, 0d, 1d), new Rect(SPACING, 0, ConfigPanel.WIDTH, 0), configPanel);
        addPanel(RectD.corners(0d, 0d, 1d, 1d), Rect.corners(
            SPACING * 2 + ConfigPanel.WIDTH, 0, -SPACING * 2 - PortPanel.WIDTH, 0), machinePanel);
        addPanel(RectD.corners(1d, 0d, 1d, 1d), Rect.corners(
            -SPACING - PortPanel.WIDTH, 0, -SPACING, 0), portPanel);

        this.imageWidth = IMAGE_WIDTH;
        this.imageHeight = IMAGE_HEIGHT;

        menu.onSyncPacket(menu.syncSlot, this::refreshVisiblePorts);
    }

    private void refreshVisiblePorts(LogisticWorkerSyncPacket p) {
        machinePorts.clear();
        machines.clear();
        machineList.clear();
        ports.clear();

        for (var port : p.getPorts()) {
            if (!machinePorts.containsKey(port.machineId())) {
                var info = new MachineInfo(port.machineId(), port.machineName(), port.icon());
                machineList.add(info);
                machines.put(port.machineId(), info);
            }
            ports.put(new LogisticComponent.PortKey(port.machineId(), port.portIndex()), port);
            machinePorts.put(port.machineId(), port);
        }

        if (!machines.containsKey(selectedMachine)) {
            selectedMachine = null;
        }

        machinePanel.refresh();
        portPanel.refresh();
    }

    private LogisticWorkerConfig getConfig(int slot) {
        return menu.machineConfig.getCompound(PREFIX + slot)
            .map(LogisticWorkerConfig::fromTag)
            .orElseGet(LogisticWorkerConfig::new);
    }
}
