package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.integration.gui.client.Button;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

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

    public SwitchButton(MenuBase menu, Texture texture, int disableTexY, int enableTexY,
        String disableLang, String enableLang) {
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
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var texRect = getValue() ? enableTexY : disableTexY;
        RenderUtil.blit(graphics, texture, rect, 0, texRect);
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
