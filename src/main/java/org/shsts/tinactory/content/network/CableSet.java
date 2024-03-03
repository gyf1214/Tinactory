package org.shsts.tinactory.content.network;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CableSet {
    private record CableSetting(MaterialSet material, double resistance) {}

    private final Map<Voltage, RegistryEntry<CableBlock>> cables;

    private CableSet(Map<Voltage, CableSetting> settings) {
        this.cables = settings.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> this.createBlock(e.getKey(), e.getValue())));
    }

    private RegistryEntry<CableBlock> createBlock(Voltage voltage, CableSetting setting) {
        var id = "network/cable/" + voltage.id;
        var insulationColor = voltage == Voltage.ULV ? 0xFF896727 : 0xFF36302E;
        return REGISTRATE.block(id, properties -> new CableBlock(properties, voltage, setting.resistance))
                .transform(ModelGen.cable())
                .tint(insulationColor, setting.material.color)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();
    }

    public CableBlock getBlock(Voltage voltage) {
        return this.cables.get(voltage).get();
    }

    public static class Builder {
        private final Map<Voltage, CableSetting> cableSettings = new HashMap<>();

        private Builder() {}

        public Builder add(Voltage voltage, MaterialSet material, double resistance) {
            this.cableSettings.put(voltage, new CableSetting(material, resistance));
            return this;
        }

        public CableSet build() {
            return new CableSet(this.cableSettings);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
