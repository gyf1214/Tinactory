package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeBookItemBase extends IRecipeBookItem {
    @Override
    default boolean matchSearch(String query) {
        var tooltips = buttonToolTip().orElse(List.of()).stream()
            .map($ -> ChatFormatting.stripFormatting($.getString()).trim().toLowerCase(Locale.ROOT))
            .filter($ -> !$.isEmpty());
        return Stream.concat(Stream.of(loc().toString().toLowerCase(Locale.ROOT)), tooltips)
            .anyMatch($ -> $.contains(query.toLowerCase(Locale.ROOT)));
    }
}
