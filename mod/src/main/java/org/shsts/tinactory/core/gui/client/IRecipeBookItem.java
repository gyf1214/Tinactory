package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinycorelib.api.core.ILoc;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeBookItem extends ILoc {
    boolean isMarker();

    void select(Layout layout, BiConsumer<Layout.SlotInfo, IProcessingObject> ingredientCons);

    Optional<List<Component>> buttonToolTip();

    void render(PoseStack poseStack, Rect rect, int z);
}
