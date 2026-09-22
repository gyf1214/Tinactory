package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.util.I18n;

import java.util.List;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeBookItemBase extends IRecipeBookItem {
    @Override
    default boolean matchSearch(String query) {
        if (query.isEmpty()) {
            return true;
        }
        var tooltips = buttonToolTip().orElse(List.of()).stream()
            .map(I18n::flattenComponent)
            .filter($ -> !$.isEmpty());
        return Stream.concat(Stream.of(loc().toString()), tooltips)
            .anyMatch(text -> I18n.matchText(query, text));
    }
}
