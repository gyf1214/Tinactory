package org.shsts.tinactory.content.electric;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.core.common.MetaConsumer;

import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CircuitMeta extends MetaConsumer {
    public CircuitMeta() {
        super("Circuit");
    }

    private void buildCircuit(JsonObject jo) {
        var tier = CircuitTier.fromName(GsonHelper.getAsString(jo, "tier"));
        var level = CircuitLevel.fromName(GsonHelper.getAsString(jo, "baseLevel"));
        var ja = GsonHelper.getAsJsonArray(jo, "items");
        for (var je : ja) {
            var id = GsonHelper.convertToString(je, "items");
            Circuits.newCircuit(tier, level, id);
            if (level != CircuitLevel.MAINFRAME) {
                level = level.next();
            }
        }
    }

    private void buildList(JsonObject jo, Consumer<String> cons) {
        var ja = GsonHelper.getAsJsonArray(jo, "items");
        for (var je : ja) {
            var id = GsonHelper.convertToString(je, "items");
            cons.accept(id);
        }
    }

    @Override
    protected void doAcceptMeta(ResourceLocation loc, JsonObject jo) {
        var type = GsonHelper.getAsString(jo, "type", "circuit");
        switch (type) {
            case "circuit" -> buildCircuit(jo);
            case "component" -> buildList(jo, Circuits::newCircuitComponent);
            case "wafer" -> buildList(jo, Circuits::newWafer);
            case "chip" -> buildList(jo, Circuits::newChip);
        }
    }
}
