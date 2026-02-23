package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.shsts.tinactory.core.autocraft.api.MachineConstraintRegistry;
import org.shsts.tinactory.core.autocraft.integration.PatternCellStorage;
import org.shsts.tinactory.core.autocraft.integration.PatternNbtCodec;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.common.CapabilityItem;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternCell extends CapabilityItem {
    public static final int BYTES_PER_PATTERN = PatternCellStorage.BYTES_PER_PATTERN;

    private final int bytesLimit;

    public MEPatternCell(Properties properties, int bytesLimit) {
        super(properties.stacksTo(1));
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEPatternCell> patternCell(int bytesLimit) {
        return properties -> new MEPatternCell(properties, bytesLimit);
    }

    public int bytesCapacity() {
        return bytesLimit;
    }

    public int bytesUsed(ItemStack stack, PatternNbtCodec codec) {
        return PatternCellStorage.bytesUsed(stack.getOrCreateTag(), codec);
    }

    public int patternCount(ItemStack stack, PatternNbtCodec codec) {
        return listPatterns(stack, codec).size();
    }

    public List<CraftPattern> listPatterns(ItemStack stack, PatternNbtCodec codec) {
        return PatternCellStorage.listPatterns(stack.getOrCreateTag(), codec);
    }

    public boolean insertPattern(ItemStack stack, CraftPattern pattern, PatternNbtCodec codec) {
        if (!(stack.getItem() instanceof MEPatternCell)) {
            return false;
        }
        return PatternCellStorage.insertPattern(stack.getOrCreateTag(), bytesLimit, pattern, codec);
    }

    @Override
    public void appendHoverText(
        ItemStack stack,
        @Nullable Level world,
        List<Component> tooltip,
        TooltipFlag isAdvanced) {
        var count = PatternCellStorage.listPatterns(stack.getOrCreateTag(), new PatternNbtCodec(
            new MachineConstraintRegistry())).size();
        var used = count * BYTES_PER_PATTERN;
        addTooltip(tooltip, "mePatternCell",
            NUMBER_FORMAT.format(count),
            NUMBER_FORMAT.format(used),
            NUMBER_FORMAT.format(bytesLimit));
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {}
}
