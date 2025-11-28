package org.shsts.tinactory.content.tool;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.content.multiblock.INuclearCell;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.List;

import static org.shsts.tinactory.content.AllRegistries.ITEMS;
import static org.shsts.tinactory.core.util.ClientUtil.DOUBLE_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NuclearRod extends Item implements INuclearItem {
    private static final double REACTION_SCALE = 2048d;

    public record Properties(double fastRate, double slowRate, double constantRate,
        double fastEmission, double slowEmission, double heatEmission, double heatFast,
        double maxReactions, ResourceLocation depletedItem) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsDouble(jo, "fastRate"),
                GsonHelper.getAsDouble(jo, "slowRate"),
                GsonHelper.getAsDouble(jo, "constantRate"),
                GsonHelper.getAsDouble(jo, "fastEmission"),
                GsonHelper.getAsDouble(jo, "slowEmission"),
                GsonHelper.getAsDouble(jo, "heatEmission"),
                GsonHelper.getAsDouble(jo, "heatFast"),
                GsonHelper.getAsDouble(jo, "maxReactions"),
                new ResourceLocation(GsonHelper.getAsString(jo, "depletedItem")));
        }
    }

    private final Properties properties;
    private final long maxReactions;
    @Nullable
    private Item depletedItem = null;

    public NuclearRod(Item.Properties properties, Properties prop) {
        super(properties.stacksTo(1));
        this.properties = prop;
        this.maxReactions = (long) Math.floor(prop.maxReactions * REACTION_SCALE);
    }

    private long getReactions(ItemStack stack) {
        if (stack.getTag() == null) {
            return 0L;
        }
        return MathUtil.clamp(stack.getTag().getLong("reactions"), 0, maxReactions);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return maxReactions > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13f * (double) (maxReactions - getReactions(stack)) / (double) maxReactions);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        if (maxReactions > 0) {
            var remaining = (double) (maxReactions - getReactions(stack)) / REACTION_SCALE;
            addTooltip(tooltip, "fuelRod", DOUBLE_FORMAT.format(remaining));
        }
    }

    private void react(ItemStack stack, long reacts) {
        var tag = stack.getTag();
        if (tag == null) {
            var tag1 = new CompoundTag();
            tag1.putLong("reactions", reacts);
            stack.setTag(tag1);
        } else {
            tag.putLong("reactions", reacts + tag.getLong("reactions"));
        }
    }

    private ItemStack getDepleted() {
        if (depletedItem == null) {
            depletedItem = ITEMS.getEntry(properties.depletedItem).get();
        }
        return new ItemStack(depletedItem);
    }

    @Override
    public ItemStack tickCell(ItemStack stack, INuclearCell cell) {
        var fast = cell.getFastNeutron();
        var slow = cell.getSlowNeutron();
        var fastReaction = fast * properties.fastRate;
        var slowReaction = slow * properties.slowRate;

        var react0 = fastReaction + slowReaction + properties.constantRate;
        double react2;
        ItemStack ret;
        if (maxReactions > 0) {
            var react1 = Math.max(1, (long) Math.ceil(react0 * REACTION_SCALE));
            var remaining = maxReactions - getReactions(stack);
            if (react1 >= remaining) {
                react2 = remaining / REACTION_SCALE;
                ret = getDepleted();
            } else {
                react2 = react0;
                react(stack, react1);
                ret = stack;
            }
        } else {
            react2 = react0;
            ret = stack;
        }

        var fastEmission = react2 * properties.fastEmission;
        var slowEmission = react2 * properties.slowEmission;
        var heat = react2 * properties.heatEmission + fastReaction * properties.heatFast;
        cell.incFastNeutron(fastEmission - fastReaction);
        cell.incSlowNeutron(slowEmission - slowReaction);
        cell.incHeat(heat);

        return ret;
    }
}
