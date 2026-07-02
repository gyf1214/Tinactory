package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.shsts.tinycorelib.api.item.ICapabilityItem;
import org.shsts.tinycorelib.api.registrate.entry.IItemCapability;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.PATTERN_CELL_ITEM;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternCell extends Item implements ICapabilityItem {
    private final long bytesLimit;

    public MEPatternCell(Properties properties, long bytesLimit) {
        super(properties.stacksTo(1));
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEPatternCell> factory(long bytesLimit) {
        return properties -> new MEPatternCell(properties, bytesLimit);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        @Nullable Level world,
        List<Component> tooltip,
        TooltipFlag isAdvanced) {
        PATTERN_CELL_ITEM.tryGet(stack).ifPresent(cell -> {
            addTooltip(tooltip, "mePatternCell",
                NUMBER_FORMAT.format(cell.patterns().size()));
            addTooltip(tooltip, "meStorageCell",
                NUMBER_FORMAT.format(cell.bytesUsed()),
                NUMBER_FORMAT.format(bytesLimit));
        });
    }

    @Override
    @Nullable
    public <T> T getCapability(ItemStack stack, IItemCapability<T> capability) {
        if (PATTERN_CELL_ITEM.is(capability)) {
            return capability.cast(new PatternCellPort(stack, bytesLimit, CONFIG.bytesPerPattern.get()));
        }
        return null;
    }
}
