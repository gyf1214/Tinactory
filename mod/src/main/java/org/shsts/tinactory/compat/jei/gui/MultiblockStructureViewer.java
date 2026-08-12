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
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
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
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.shsts.tinactory.api.multiblock.IBlockIngredient;
import org.shsts.tinactory.api.multiblock.IMultiblockDisplay;
import org.shsts.tinactory.compat.jei.ingredient.ItemIngredientRenderer;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.network.PrimitiveBlock;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

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
    private static final float ZOOM_MIN = 1f;
    private static final float ZOOM_MAX = 64f;
    private final MultiblockSet set;
    private final IMultiblockDisplay display;
    private final Rect viewport;
    private final Rect layerButton;
    private final Rect resetButton;
    private final ScreenPosition pos;
    private final ScreenRectangle area;
    private final Map<IBlockIngredient, BlockState> blockStates;
    @Nullable
    private Hover hovered = null;
    private boolean dragging = false;
    private float yaw = DEFAULT_YAW;
    private float pitch = DEFAULT_PITCH;
    private float zoom;
    private int selectedLayer = 0;

    private record Hover(BlockState state, @Nullable IBlockIngredient ingredient) {}

    private static float fittedZoom(IMultiblockDisplay display, Rect viewport) {
        var wd = display.width() + display.depth();
        var width = wd * WIDTH_SCALE;
        var height = display.height() * HEIGHT_SCALE1 + wd * HEIGHT_SCALE2;
        var zoom = Math.min(viewport.width() / width, viewport.height() / height) * .9f;
        return Math.clamp(zoom, ZOOM_MIN, ZOOM_MAX);
    }

    public MultiblockStructureViewer(MultiblockSet set, Rect viewport, Rect buttons) {
        this.set = set;
        this.display = set.display();
        this.viewport = viewport;

        this.layerButton = buttons.resize(buttons.width(), BUTTON_HEIGHT);
        this.resetButton = buttons.offset(0, BUTTON_HEIGHT + SPACING).resize(buttons.width(), BUTTON_HEIGHT);

        var endX = Math.max(viewport.endX(), buttons.endX());
        var endY = Math.max(viewport.endY(), buttons.endY());
        this.pos = new ScreenPosition(0, 0);
        this.area = new ScreenRectangle(0, 0, endX, endY);

        this.blockStates = createBlockStates();
        this.zoom = fittedZoom(display, viewport);
    }

    @Override
    public ScreenPosition getPosition() {
        return pos;
    }

    @Override
    public ScreenRectangle getArea() {
        return area;
    }

    private Map<IBlockIngredient, BlockState> createBlockStates() {
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
        zoom = fittedZoom(display, viewport);
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
        var layerLabel = selectedLayer == 0 ? tr("layer.all") : tr("layer", selectedLayer);
        drawButton(graphics, layerButton, layerLabel, mouseX, mouseY);
        drawButton(graphics, resetButton, tr("reset"), mouseX, mouseY);
    }

    private void transformView(PoseStack poseStack) {
        poseStack.translate(viewport.x() + viewport.width() / 2f, viewport.y() + viewport.height() / 2f, 100f);
        poseStack.scale(zoom, -zoom, zoom);
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

    private void renderStructure(GuiGraphics graphics, int guiX, int guiY,
        Consumer<PoseStack.Pose> poseCons) {
        var poseStack = graphics.pose();
        poseStack.pushPose();
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
        var guiPose = graphics.pose().last().pose();
        var guiX = guiPose.m30();
        var guiY = guiPose.m31();
        var inverse = new Matrix4f();
        var inverseNormal = new Matrix3f();
        renderStructure(graphics, Math.round(guiX), Math.round(guiY), pose -> {
            pose.pose().invert(inverse);
            pose.normal().invert(inverseNormal);
        });
        hovered = viewport.in(mouseX, mouseY) ?
            pick(inverse, inverseNormal, guiX + mouseX, guiY + mouseY) : null;
        drawControls(graphics, mouseX, mouseY);
    }

    private static float hitWall(float origin, float direction, int bound) {
        if (MathUtil.compare(direction) == 0) {
            return switch (MathUtil.compare(origin, bound)) {
                case 0 -> 0f;
                case 1 -> Float.POSITIVE_INFINITY;
                case -1 -> Float.NEGATIVE_INFINITY;
                default -> throw new IllegalStateException();
            };
        }
        return (bound - origin) / direction;
    }

    private static Tuple<Float, Float> hitSlab(float origin, float direction, int bound1, int bound2) {
        var t1 = hitWall(origin, direction, bound1);
        var t2 = hitWall(origin, direction, bound2);
        return new Tuple<>(Math.min(t1, t2), Math.max(t1, t2));
    }

    private static float hitBlock(Vector4f origin, Vector3f dir, int x, int y, int z) {
        var tx = hitSlab(origin.x(), dir.x(), x, x + 1);
        var ty = hitSlab(origin.y(), dir.y(), y, y + 1);
        var tz = hitSlab(origin.z(), dir.z(), z, z + 1);
        var minT = Math.max(Math.max(tx.getA(), ty.getA()), tz.getA());
        var maxT = Math.min(Math.min(tx.getB(), ty.getB()), tz.getB());
        return MathUtil.compare(minT, maxT) <= 0 ? minT : Float.POSITIVE_INFINITY;
    }

    @Nullable
    private Hover pick(Matrix4f inverse, Matrix3f inverseNormal, double mouseX, double mouseY) {
        var pos = inverse.transform(new Vector4f((float) mouseX, (float) mouseY, 200f, 1f));
        var dir = inverseNormal.transform(new Vector3f(0f, 0f, -1f));

        var nearest = Float.POSITIVE_INFINITY;
        Hover result = null;
        for (var y = 0; y < display.height(); y++) {
            if (!visible(y)) {
                continue;
            }
            for (var z = 0; z < display.depth(); z++) {
                for (var x = 0; x < display.width(); x++) {
                    var ingredient = display.getIngredient(x, y, z).orElse(null);
                    var blockState = ingredient == null ? null : blockStates.get(ingredient);
                    var hit = blockState == null ? Float.POSITIVE_INFINITY : hitBlock(pos, dir, x, y, z);
                    if (hit < nearest) {
                        nearest = hit;
                        result = new Hover(blockState, ingredient);
                    }
                }
            }
        }
        var controller = display.controllerPosition();
        if (visible(controller.getY())) {
            var hit = hitBlock(pos, dir, controller.getX(), controller.getY(), controller.getZ());
            if (hit < nearest) {
                result = new Hover(controllerState(set), null);
            }
        }
        return result;
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
        if (hovered != null) {
            var stack = new ItemStack(hovered.state().getBlock());
            tooltip.addAll(stack.getTooltipLines(Item.TooltipContext.EMPTY, Minecraft.getInstance().player,
                TooltipFlag.NORMAL));
            var ingredient = hovered.ingredient();
            if (ingredient != null) {
                var alternatives = ingredient.expand(ClientUtil.registryAccess()).stream().map(ItemStack::new).toList();
                tooltip.add(tr("alternatives"));
                tooltip.add(new TagContentTooltipComponent<>(new ItemIngredientRenderer(), alternatives));
            }
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
        zoom = Math.clamp(zoom * (deltaY > 0 ? 1.1f : .9f), ZOOM_MIN, ZOOM_MAX);
        return true;
    }
}
