package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.MEStorageDetectorMenu;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.logistics.MEStorageDetector;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.Widgets;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.util.ClientUtil;

import java.util.Optional;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.logistics.MEStorageDetector.TARGET_AMOUNT_KEY;
import static org.shsts.tinactory.content.logistics.MEStorageDetector.TARGET_FLUID_KEY;
import static org.shsts.tinactory.content.logistics.MEStorageDetector.TARGET_ITEM_KEY;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageDetectorScreen extends MenuScreen<MEStorageDetectorMenu> {
    private static final int EDIT_WIDTH = 64;
    private static final int X_OFFSET = (SLOT_SIZE + MARGIN_X + EDIT_WIDTH) / 2;
    private static final int SLOT_Y_OFFSET = SLOT_SIZE / 2;
    private static final int EDIT_Y_OFFSET = SLOT_Y_OFFSET + (SLOT_SIZE - EDIT_HEIGHT) / 2;

    private final IMachineConfig config;

    private class MarkerSlot extends MenuWidget {
        public MarkerSlot() {
            super(MEStorageDetectorScreen.this.menu);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var z = getBlitOffset();
            RenderUtil.blit(poseStack, SLOT_BACKGROUND, z, rect);

            var targetItem = MEStorageDetector.targetItem(config);
            var targetFluid = MEStorageDetector.targetFluid(config);

            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            if (!targetItem.isEmpty()) {
                RenderUtil.renderItem(targetItem, rect1.x(), rect1.y());
            } else if (!targetFluid.isEmpty()) {
                RenderUtil.renderFluid(poseStack, targetFluid, rect1, z);
            }

            if (isHovering(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(poseStack, rect1);
            }
        }

        @Override
        protected boolean canHover() {
            return true;
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return button == 0 || button == 1;
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            ClientUtil.playSound(SoundEvents.UI_BUTTON_CLICK);

            var carried = menu.getCarried();
            var packet = SetMachineConfigPacket.builder();

            if (carried.isEmpty()) {
                packet.reset(TARGET_ITEM_KEY)
                    .reset(TARGET_FLUID_KEY);
            } else {
                var fluid = StackHelper.getFluidHandlerFromItem(carried)
                    .filter($ -> button == 0)
                    .flatMap(handler -> {
                        var stack = handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE);
                        return stack.isEmpty() ? Optional.empty() :
                            Optional.of(StackHelper.copyWithAmount(stack, 1));
                    });
                fluid.ifPresentOrElse($ -> packet
                        .set(TARGET_FLUID_KEY, StackHelper.serializeFluidStack($))
                        .reset(TARGET_ITEM_KEY),
                    () -> packet
                        .set(TARGET_ITEM_KEY, StackHelper.copyWithCount(carried, 1).serializeNBT())
                        .reset(TARGET_FLUID_KEY));
            }

            menu.triggerEvent(SET_MACHINE_CONFIG, packet);
        }
    }

    private final EditBox targetAmountEdit;

    public MEStorageDetectorScreen(MEStorageDetectorMenu menu, Component title) {
        super(menu, title);
        this.config = menu.machine.config();

        var slot = new MarkerSlot();
        this.targetAmountEdit = Widgets.editBox();
        targetAmountEdit.setResponder(this::onEditChange);

        var anchor = RectD.corners(0.5d, 0d, 0.5d, 0d);
        var offset1 = new Rect(-X_OFFSET, SLOT_Y_OFFSET, SLOT_SIZE, SLOT_SIZE);
        var offset2 = new Rect(X_OFFSET - EDIT_WIDTH, EDIT_Y_OFFSET, EDIT_WIDTH, EDIT_HEIGHT);

        addWidget(anchor, offset1, slot);
        addWidget(anchor, offset2, targetAmountEdit);

        this.contentHeight = menu.endY();
    }

    private void resetEditText() {
        var amount = config.getInt(TARGET_AMOUNT_KEY, 0);
        targetAmountEdit.setValue(Integer.toString(amount));
    }

    private void onEditChange(String str) {
        var val = -1;
        try {
            val = Integer.parseInt(str);
        } catch (NumberFormatException ignored) {
        }
        var oldVal = config.getInt(TARGET_AMOUNT_KEY, 0);
        if (val >= 0 && val != oldVal) {
            menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder()
                .set(TARGET_AMOUNT_KEY, val));
        }
    }

    @Override
    protected void init() {
        super.init();
        resetEditText();
    }
}
