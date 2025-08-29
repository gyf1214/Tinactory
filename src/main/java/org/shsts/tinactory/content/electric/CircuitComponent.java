package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CircuitComponent {
    public final String name;
    private final Map<CircuitComponentTier, IEntry<Item>> items;

    public CircuitComponent(String name) {
        this.name = name;
        this.items = new HashMap<>();
        for (var tier : CircuitComponentTier.values()) {
            var item = REGISTRATE.item(tier.getName(name)).register();
            items.put(tier, item);
        }
    }

    public ResourceLocation loc(CircuitComponentTier tier) {
        return entry(tier).loc();
    }

    public IEntry<Item> entry(CircuitComponentTier tier) {
        return items.get(tier);
    }

    public Item item(CircuitComponentTier tier) {
        return entry(tier).get();
    }

    public TagKey<Item> tag(CircuitComponentTier tier) {
        return AllTags.circuitComponent(name, tier);
    }
}
