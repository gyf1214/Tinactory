package org.shsts.tinactory.content.material;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.core.common.MetaConsumer;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllItems.COMPONENTS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ComponentMeta extends MetaConsumer {
    public ComponentMeta() {
        super("Component");
    }

    private void buildComponents(ResourceLocation loc, JsonObject jo) {
        var tint = GsonHelper.getAsInt(jo, "voltageTint", -1);
        var components = new HashMap<Voltage, IEntry<Item>>();
        var name = loc.getPath();
        var voltages = Voltage.parseJson(jo, "items");
        for (var v : voltages) {
            var id = "component/" + v.id + "/" + name;
            var builder = REGISTRATE.item(id);
            if (tint >= 0) {
                builder.tint(() -> () -> ($, i) -> i == tint ? v.color : 0xFFFFFFFF);
            }
            var entry = builder.register();
            components.put(v, entry);
        }
        COMPONENTS.put(name, components);
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var type = GsonHelper.getAsString(jo, "type", "default");
        switch (type) {
            case "default" -> buildComponents(loc, jo);
        }
    }
}
