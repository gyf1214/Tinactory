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
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.IdentityHashMap;
import java.util.Map;

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
    private final Rect details;
    private final Rect reset;
    private final Rect layer;
    private final Rect area;
    private final Map<IBlockIngredient, BlockState> states;
    private final ViewState view = new ViewState();
    @Nullable
    private BlockState hovered = null;
    private boolean dragging = false;

    private static final class ViewState {
        private float yaw = DEFAULT_YAW;
        private float pitch = DEFAULT_PITCH;
        private float zoom = 1f;
        private int selectedLayer = 0;
    }

    public MultiblockStructureViewer(MultiblockSet set, Rect viewport, Rect details, Rect reset, Rect layer) {
        this.set = set;
        this.display = set.display();
        this.viewport = viewport;
        this.details = details;
        this.reset = reset;
        this.layer = layer;
        this.area = Rect.corners(viewport.x(), viewport.y(), Math.max(viewport.endX(), details.endX()),
            Math.max(Math.max(viewport.endY(), details.endY()), Math.max(reset.endY(), layer.endY())));
        this.states = createStates();
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

    private float fittedZoom() {
        var wd = display.width() + display.depth();
        var width = wd * WIDTH_SCALE;
        var height = display.height() * HEIGHT_SCALE1 + wd * HEIGHT_SCALE2;
        return Math.min(viewport.width() / width, viewport.height() / height) * .9f;
    }

    private boolean visible(int y) {
        return view.selectedLayer == 0 || y == view.selectedLayer - 1;
    }

    private void reset() {
        view.yaw = DEFAULT_YAW;
        view.pitch = DEFAULT_PITCH;
        view.zoom = 1f;
        view.selectedLayer = 0;
    }

    private void changeLayer(boolean forward) {
        var height = display.height();
        if (forward) {
            view.selectedLayer = view.selectedLayer == height ? 0 : view.selectedLayer + 1;
        } else {
            view.selectedLayer = view.selectedLayer == 0 ? height : view.selectedLayer - 1;
        }
    }

    private void transform(PoseStack poseStack) {
        poseStack.translate(viewport.x() + viewport.width() / 2f, viewport.y() + viewport.height() / 2f, 100f);
        var scale = fittedZoom() * view.zoom;
        poseStack.scale(scale, -scale, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(view.pitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(view.yaw));
        poseStack.translate(-display.width() / 2f, -display.height() / 2f, -display.depth() / 2f);
    }

    private static void renderState(PoseStack poseStack, GuiGraphics graphics, BlockState state, int x, int y, int z) {
        poseStack.pushPose();
        poseStack.translate(x, y, z);
        RenderUtil.renderBlockInGui(poseStack, graphics.bufferSource(), state,
            LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public ScreenPosition getPosition() {
        return new ScreenPosition(area.x(), area.y());
    }

    @Override
    public ScreenRectangle getArea() {
        return new ScreenRectangle(area.x(), area.y(), area.width(), area.height());
    }

    @Override
    public void drawWidget(GuiGraphics graphics, double mouseX, double mouseY) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
        transform(poseStack);
        var transform = new Matrix4f(poseStack.last().pose());
        graphics.enableScissor(viewport.x(), viewport.y(), viewport.endX(), viewport.endY());
        RenderSystem.enableDepthTest();
        Lighting.setupFor3DItems();
        for (var y = 0; y < display.height(); y++) {
            if (!visible(y)) {
                continue;
            }
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var state = display.getIngredient(x, y, z).map(states::get).orElse(null);
                    if (state != null) {
                        renderState(poseStack, graphics, state, x, y, z);
                    }
                }
            }
        }
        var controller = display.controllerPosition();
        if (visible(controller.getY())) {
            renderState(poseStack, graphics, controllerState(set), controller.getX(), controller.getY(),
                controller.getZ());
        }
        graphics.flush();
        graphics.disableScissor();
        Lighting.setupForFlatItems();
        RenderSystem.disableDepthTest();
        poseStack.popPose();
        hovered = viewport.in(mouseX, mouseY) ? pick(transform, mouseX, mouseY) : null;
        drawControls(graphics);
        var font = Minecraft.getInstance().font;
        for (var i = 0; i < display.getDetailLines().size(); i++) {
            graphics.drawString(font, display.getDetailLines().get(i), details.x(), details.y() + i * 10, 0x404040);
        }
    }

    private void drawControls(GuiGraphics graphics) {
        var font = Minecraft.getInstance().font;
        graphics.fill(reset.x(), reset.y(), reset.endX(), reset.endY(), 0x80404040);
        graphics.fill(layer.x(), layer.y(), layer.endX(), layer.endY(), 0x80404040);
        graphics.drawCenteredString(font, Component.translatable("tinactory.jei.multiblock.reset"),
            reset.x() + reset.width() / 2, reset.y() + 3, 0xffffff);
        var label = view.selectedLayer == 0 ? Component.translatable("tinactory.jei.multiblock.layer.all") :
            Component.translatable("tinactory.jei.multiblock.layer", view.selectedLayer);
        graphics.drawCenteredString(font, label, layer.x() + layer.width() / 2, layer.y() + 3, 0xffffff);
    }

    @Nullable
    private BlockState pick(Matrix4f transform, double mouseX, double mouseY) {
        var inverse = transform.invert(new Matrix4f());
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
                    var state = display.getIngredient(x, y, z).map(states::get).orElse(null);
                    var hit = state == null ? Float.POSITIVE_INFINITY : hit(near, dx, dy, dz, x, y, z);
                    if (hit < nearest) {
                        nearest = hit;
                        result = state;
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

    private static float hit(Vector4f origin, float dx, float dy, float dz, int x, int y, int z) {
        var min = Math.max(Math.max(axis(origin.x(), dx, x), axis(origin.y(), dy, y)),
            axis(origin.z(), dz, z));
        var max = Math.min(Math.min(axisMax(origin.x(), dx, x + 1), axisMax(origin.y(), dy, y + 1)),
            axisMax(origin.z(), dz, z + 1));
        return min <= max && max >= 0 ? Math.max(min, 0) : Float.POSITIVE_INFINITY;
    }

    private static float axis(float origin, float direction, int bound) {
        return direction == 0 ? (origin < bound ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY) :
            (bound - origin) / direction;
    }

    private static float axisMax(float origin, float direction, int bound) {
        return direction == 0 ? (origin > bound ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY) :
            (bound - origin) / direction;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (reset.in(mouseX, mouseY)) {
            tooltip.add(Component.translatable("tinactory.jei.multiblock.reset"));
        } else if (layer.in(mouseX, mouseY)) {
            tooltip.add(Component.translatable("tinactory.jei.multiblock.layer.tooltip"));
        } else if (hovered != null) {
            var stack = new ItemStack(hovered.getBlock());
            tooltip.addAll(stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player,
                TooltipFlag.NORMAL));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && reset.in(mouseX, mouseY)) {
            reset();
            return true;
        }
        if (layer.in(mouseX, mouseY) && (button == 0 || button == 1)) {
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
        view.yaw += (float) dragX;
        view.pitch = Math.clamp(view.pitch + (float) dragY, MIN_PITCH, MAX_PITCH);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (!viewport.in(mouseX, mouseY)) {
            return false;
        }
        view.zoom = Math.clamp(view.zoom * (deltaY > 0 ? 1.1f : .9f), .5f, 3f);
        return true;
    }
}
