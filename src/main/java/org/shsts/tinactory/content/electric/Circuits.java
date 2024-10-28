package org.shsts.tinactory.content.electric;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Circuits {
    private record CircuitKey(CircuitTier tier, CircuitLevel level) {}

    private static final Map<CircuitKey, RegistryEntry<Item>> CIRCUITS = new HashMap<>();

    private record ComponentKey(String component, CircuitComponentTier tier) {}

    private static final Map<ComponentKey, RegistryEntry<Item>> COMPONENTS = new HashMap<>();

    private static final Map<CircuitTier, RegistryEntry<Item>> BOARDS = new HashMap<>();
    private static final Map<CircuitTier, RegistryEntry<Item>> CIRCUIT_BOARDS = new HashMap<>();

    public static class CircuitComponent {
        private final String component;

        private CircuitComponent(String component) {
            this.component = component;
        }

        public RegistryEntry<Item> item(CircuitComponentTier tier) {
            return COMPONENTS.get(new ComponentKey(component, tier));
        }

        public Item getItem(CircuitComponentTier tier) {
            return item(tier).get();
        }

        public TagKey<Item> tag(CircuitComponentTier tier) {
            return AllTags.circuitComponent(component, tier);
        }
    }

    public static class Circuit {
        private final CircuitKey key;
        private final RegistryEntry<Item> item;

        private Circuit(CircuitKey key, RegistryEntry<Item> item) {
            this.key = key;
            this.item = item;
        }

        public RegistryEntry<Item> item() {
            return item;
        }

        public Item getItem() {
            return item.get();
        }

        public CircuitTier tier() {
            return key.tier;
        }

        public CircuitLevel level() {
            return key.level;
        }

        public RegistryEntry<Item> circuitBoard() {
            return CIRCUIT_BOARDS.get(key.tier);
        }
    }

    public static Voltage getVoltage(CircuitTier tier, CircuitLevel level) {
        return Voltage.fromRank(tier.baseVoltage.rank + level.voltageOffset);
    }

    @FunctionalInterface
    public interface ForEachConsumer {
        void accept(CircuitTier tier, CircuitLevel level, RegistryEntry<Item> item);
    }

    public static void forEach(ForEachConsumer cons) {
        for (var entry : CIRCUITS.entrySet()) {
            cons.accept(entry.getKey().tier(), entry.getKey().level(), entry.getValue());
        }
    }

    @FunctionalInterface
    public interface ForEachComponentConsumer {
        void accept(String component, CircuitComponentTier tier, RegistryEntry<Item> item);
    }

    public static void forEachComponent(ForEachComponentConsumer cons) {
        for (var entry : COMPONENTS.entrySet()) {
            cons.accept(entry.getKey().component(), entry.getKey().tier(), entry.getValue());
        }
    }

    public static Circuit circuit(CircuitTier tier, CircuitLevel level, String id) {
        var item = REGISTRATE.item("circuit/" + id, Item::new).register();
        var key = new CircuitKey(tier, level);
        assert !CIRCUITS.containsKey(key);
        CIRCUITS.put(key, item);
        return new Circuit(key, item);
    }

    public static CircuitComponent component(String name) {
        for (var tier : CircuitComponentTier.values()) {
            var item = REGISTRATE.item(tier.getName(name), Item::new).register();
            var key = new ComponentKey(name, tier);
            assert !COMPONENTS.containsKey(key);
            COMPONENTS.put(key, item);
        }
        return new CircuitComponent(name);
    }

    public static void addBoards() {
        for (var tier : CircuitTier.values()) {
            var board = REGISTRATE.item("board/" + tier.board, Item::new).register();
            BOARDS.put(tier, board);
        }
        for (var tier : CircuitTier.values()) {
            var circuitBoard = REGISTRATE.item("circuit_board/" + tier.circuitBoard, Item::new).register();
            CIRCUIT_BOARDS.put(tier, circuitBoard);
        }
    }

    public static RegistryEntry<Item> board(CircuitTier tier) {
        return BOARDS.get(tier);
    }

    public static RegistryEntry<Item> circuitBoard(CircuitTier tier) {
        return CIRCUIT_BOARDS.get(tier);
    }
}
