package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.GhostRecipe;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.core.ILoc;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeBookItem extends ILoc, IRectRenderable {
    boolean isMarker();

    void select(Layout layout, GhostRecipe ghostRecipe);

    Optional<List<Component>> buttonToolTip();
}
