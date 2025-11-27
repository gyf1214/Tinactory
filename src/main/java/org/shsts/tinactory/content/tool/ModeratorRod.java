package org.shsts.tinactory.content.tool;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.multiblock.INuclearCell;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ModeratorRod extends Item implements INuclearItem {
    public record Properties(double fastAbsorb, double slowAbsorb,
        double slowEmission, double heatEmission) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsDouble(jo, "fastAbsorb"),
                GsonHelper.getAsDouble(jo, "slowAbsorb"),
                GsonHelper.getAsDouble(jo, "slowEmission"),
                GsonHelper.getAsDouble(jo, "heatEmission"));
        }
    }

    private final Properties properties;

    public ModeratorRod(Item.Properties properties, Properties prop) {
        super(properties);
        this.properties = prop;
    }

    @Override
    public ItemStack tickCell(ItemStack stack, INuclearCell cell) {
        var fast = cell.getFastNeutron();
        var slow = cell.getSlowNeutron();

        var fastAbsorb = fast * properties.fastAbsorb;
        var slowAbsorb = slow * properties.slowAbsorb;
        var slowEmission = fastAbsorb * properties.slowEmission;

        cell.incFastNeutron(-fastAbsorb);
        cell.incSlowNeutron(slowEmission - slowAbsorb);
        cell.incHeat(fastAbsorb * properties.heatEmission);

        return stack;
    }
}
