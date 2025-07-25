package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.util.I18n.tr;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class SwitchButton extends Button {
    protected final Texture texture;
    protected final int enableTexY;
    protected final int disableTexY;
    protected final String enableLang;
    protected final String disableLang;

    public SwitchButton(IMenu menu, Texture texture, int enableTexY, int disableTexY,
        String enableLang, String disableLang) {
        super(menu);
        this.texture = texture;
        this.enableTexY = enableTexY;
        this.disableTexY = disableTexY;
        this.enableLang = enableLang;
        this.disableLang = disableLang;
    }

    protected abstract boolean getValue();

    protected abstract void setValue(boolean val);

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var texRect = getValue() ? enableTexY : disableTexY;
        RenderUtil.blit(poseStack, texture, getBlitOffset(), rect, 0, texRect);
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var langKey = getValue() ? enableLang : disableLang;
        return Optional.of(List.of(tr("tinactory.tooltip." + langKey)));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        setValue(!getValue());
    }
}
