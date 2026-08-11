package org.shsts.tinactory.compat.jei.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.inputs.IJeiGuiEventListener;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MultiblockStructureViewer implements IRecipeWidget, IJeiGuiEventListener {
    private static final float DEFAULT_YAW = -45f;
    private static final float DEFAULT_PITCH = 30f;
    private static final float MIN_PITCH = -80f;
    private static final float MAX_PITCH = 80f;
    private static final float WIDTH_SCALE = (float) Math.sqrt(2) / 2;
    private static final float HEIGHT_SCALE1 = (float) Math.sqrt(3) / 2;
    private static final float HEIGHT_SCALE2 = WIDTH_SCALE / 2f;
    private final MultiblockSet set;
    private final IMultiblockDisplay display;
    private final Rect viewport;
    private final Rect layerButton;
    private final Rect resetButton;
    private final Rect detailLabel;
    private final ScreenRectangle area;
    private final Map<IBlockIngredient, BlockState> blockStates;
    @Nullable
    private BlockState hovered = null;
    private boolean dragging = false;
    private float yaw = DEFAULT_YAW;
    private float pitch = DEFAULT_PITCH;
    private float zoom = 1f;
    private int selectedLayer = 0;

    public MultiblockStructureViewer(MultiblockSet set, Rect viewport, Rect controls) {
        this.set = set;
        this.display = set.display();
        this.viewport = viewport;

        this.layerButton = controls.resize(controls.width(), BUTTON_HEIGHT);
        var y = BUTTON_HEIGHT + SPACING;
        this.resetButton = controls.offset(0, y).resize(controls.width(), BUTTON_HEIGHT);
        y += BUTTON_HEIGHT + SPACING;
        this.detailLabel = controls.offset(0, y).resize(0, 0);

        var width = Math.max(viewport.endX(), controls.endX());
        var height = Math.max(viewport.endY(), controls.endY());
        this.area = new ScreenRectangle(0, 0, width, height);
        this.blockStates = createStates();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(0, 0);
    }

    @Override
    public ScreenRectangle getArea() {
        return area;
    }

    private Map<IBlockIngredient, BlockState> createStates() {
        var result = new IdentityHashMap<IBlockIngredient, BlockState>();
        for (var y = 0; y < display.height(); y++) {
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    display.getIngredient(x, y, z).ifPresent(ingredient ->
                        result.computeIfAbsent(ingredient, $ -> $.display(ClientUtil.registryAccess())));
                }
            }
        }
        return result;
    }

    private static BlockState controllerState(MultiblockSet set) {
        var state = set.controller().get().defaultBlockState();
        if (state.hasProperty(PrimitiveBlock.FACING)) {
            state = state.setValue(PrimitiveBlock.FACING, Direction.SOUTH);
        }
        return state;
    }

    private boolean visible(int y) {
        return selectedLayer == 0 || y == selectedLayer - 1;
    }

    private void reset() {
        yaw = DEFAULT_YAW;
        pitch = DEFAULT_PITCH;
        zoom = 1f;
        selectedLayer = 0;
    }

    private void changeLayer(boolean forward) {
        var height = display.height();
        if (forward) {
            selectedLayer = selectedLayer == height ? 0 : selectedLayer + 1;
        } else {
            selectedLayer = selectedLayer == 0 ? height : selectedLayer - 1;
        }
    }

    private static MutableComponent tr(String key, Object... args) {
        return I18n.tr("tinactory.jei.multiblock." + key, args);
    }

    private void drawButton(GuiGraphics graphics, Rect rect, Component text, double mouseX, double mouseY) {
        var texture = rect.in(mouseX, mouseY) ? Texture.VANILLA_BUTTON_HOVERED : Texture.VANILLA_BUTTON;
        VanillaButton.renderButton(graphics, texture, rect, text);
    }

    private void drawControls(GuiGraphics graphics, double mouseX, double mouseY) {
        var font = ClientUtil.getFont();

        var layerLabel = selectedLayer == 0 ? tr("layer.all") : tr("layer", selectedLayer);
        drawButton(graphics, layerButton, layerLabel, mouseX, mouseY);
        drawButton(graphics, resetButton, tr("reset"), mouseX, mouseY);

        var detailLines = display.getDetailLines();
        for (var i = 0; i < display.getDetailLines().size(); i++) {
            graphics.drawString(font, detailLines.get(i), detailLabel.x(),
                detailLabel.y() + FONT_HEIGHT * i, RenderUtil.TEXT_COLOR);
        }
    }

    private float fittedZoom() {
        var wd = display.width() + display.depth();
        var width = wd * WIDTH_SCALE;
        var height = display.height() * HEIGHT_SCALE1 + wd * HEIGHT_SCALE2;
        return Math.min(viewport.width() / width, viewport.height() / height) * .9f;
    }

    private void transformView(PoseStack poseStack) {
        poseStack.translate(viewport.x() + viewport.width() / 2f, viewport.y() + viewport.height() / 2f, 100f);
        var scale = fittedZoom() * zoom;
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.translate(-display.width() / 2f, -display.height() / 2f, -display.depth() / 2f);
    }

    private static void renderBlock(PoseStack poseStack, GuiGraphics graphics, BlockState state,
        int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderUtil.renderBlockInGui(poseStack, graphics.bufferSource(), state,
            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private void renderStructure(GuiGraphics graphics, Consumer<PoseStack.Pose> poseCons) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
        var guiPose = poseStack.last().pose();
        var guiX = Math.round(guiPose.m30());
        var guiY = Math.round(guiPose.m31());
        transformView(poseStack);
        poseCons.accept(poseStack.last());
        graphics.enableScissor(guiX + viewport.x(), guiY + viewport.y(),
            guiX + viewport.endX(), guiY + viewport.endY());
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        for (var y = 0; y < display.height(); y++) {
            if (!visible(y)) {
                continue;
            }
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var state = display.getIngredient(x, y, z).map(blockStates::get).orElse(null);
                    if (state != null) {
                        renderBlock(poseStack, graphics, state, x, y, z);
                    }
                }
            }
        }
        var controller = display.controllerPosition();
        if (visible(controller.getY())) {
            renderBlock(poseStack, graphics, controllerState(set), controller.getX(), controller.getY(),
                controller.getZ());
        }
        graphics.flush();
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        graphics.disableScissor();
        poseStack.popPose();
    }

    @Override
    public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
        var inverse = new Matrix4f();
        renderStructure(graphics, pose -> pose.pose().invert(inverse));
        hovered = viewport.in(mouseX, mouseY) ? pick(inverse, mouseX, mouseY) : null;
        drawControls(graphics, mouseX, mouseY);
    }

    private static float axisMin(float origin, float direction, int bound) {
        return direction == 0 ? (origin < bound ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY) :
            (bound - origin) / direction;
    }

    private static float axisMax(float origin, float direction, int bound) {
        return direction == 0 ? (origin > bound ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY) :
            (bound - origin) / direction;
    }

    private static float hit(Vector4f origin, float dx, float dy, float dz, int x, int y, int z) {
        var min = Math.max(Math.max(axisMin(origin.x(), dx, x), axisMin(origin.y(), dy, y)),
            axisMin(origin.z(), dz, z));
        var max = Math.min(Math.min(axisMax(origin.x(), dx, x + 1), axisMax(origin.y(), dy, y + 1)),
            axisMax(origin.z(), dz, z + 1));
        return min <= max && max >= 0 ? Math.max(min, 0) : Float.POSITIVE_INFINITY;
    }

    @Nullable
    private BlockState pick(Matrix4f inverse, double mouseX, double mouseY) {
        var near = inverse.transform(new Vector4f((float) mouseX, (float) mouseY, 0f, 1f));
        var far = inverse.transform(new Vector4f((float) mouseX, (float) mouseY, 200f, 1f));
        near.div(near.w());
        far.div(far.w());
        var dx = far.x() - near.x();
        var dy = far.y() - near.y();
        var dz = far.z() - near.z();
        var nearest = Float.POSITIVE_INFINITY;
        BlockState result = null;
        for (var y = 0; y < display.height(); y++) {
            if (!visible(y)) {
                continue;
            }
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var blockState = display.getIngredient(x, y, z).map(blockStates::get).orElse(null);
                    var hit = blockState == null ? Float.POSITIVE_INFINITY : hit(near, dx, dy, dz, x, y, z);
                    if (hit < nearest) {
                        nearest = hit;
                        result = blockState;
                    }
                }
            }
        }
        var controller = display.controllerPosition();
        if (visible(controller.getY())) {
            var hit = hit(near, dx, dy, dz, controller.getX(), controller.getY(), controller.getZ());
            if (hit < nearest) {
                result = controllerState(set);
            }
        }
        return result;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (hovered != null) {
            var stack = new ItemStack(hovered.getBlock());
            tooltip.addAll(stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player,
                TooltipFlag.NORMAL));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && resetButton.in(mouseX, mouseY)) {
            reset();
            return true;
        }
        if (layerButton.in(mouseX, mouseY) && (button == 0 || button == 1)) {
            changeLayer(button == 0);
            return true;
        }
        dragging = button == 0 && viewport.in(mouseX, mouseY);
        return dragging;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        var result = dragging && button == 0;
        dragging = false;
        return result;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (!dragging || button != 0) {
            return false;
        }
        yaw += (float) dragX;
        pitch = Math.clamp(pitch + (float) dragY, MIN_PITCH, MAX_PITCH);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!viewport.in(mouseX, mouseY)) {
            return false;
        }
        zoom = Math.clamp(zoom * (deltaY > 0 ? 1.1f : .9f), .5f, 3f);
        return true;
    }
}
