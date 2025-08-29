package org.shsts.tinactory.content.electric;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.MetaConsumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CircuitMeta extends MetaConsumer {
    public CircuitMeta() {
        super("Circuit");
    }

    private void buildCircuit(JsonObject jo) {
        var tier = CircuitTier.fromName(GsonHelper.getAsString(jo, "tier"));
        var level = CircuitLevel.fromName(GsonHelper.getAsString(jo, "baseLevel"));
        var ja = GsonHelper.getAsJsonArray(jo, "circuits");
        for (var je : ja) {
            var id = GsonHelper.convertToString(je, "circuits");
            Circuits.newCircuit(tier, level, id);
            if (level != CircuitLevel.MAINFRAME) {
                level = level.next();
            }
        }
    }

    private void buildComponent(JsonObject jo) {
        var ja = GsonHelper.getAsJsonArray(jo, "components");
        for (var je : ja) {
            var id = GsonHelper.convertToString(je, "circuits");
            Circuits.newCircuitComponent(id);
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var type = GsonHelper.getAsString(jo, "type", "circuit");
        if (type.equals("circuit")) {
            buildCircuit(jo);
        } else if (type.equals("component")) {
            buildComponent(jo);
        }
    }
}
