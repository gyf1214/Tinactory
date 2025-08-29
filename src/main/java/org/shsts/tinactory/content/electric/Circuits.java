package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Circuits {
    public static final Map<String, Circuit> CIRCUITS = new HashMap<>();
    public static final Map<String, CircuitComponent> CIRCUIT_COMPONENTS = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> BOARDS = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> CIRCUIT_BOARDS = new HashMap<>();

    public static Voltage getVoltage(CircuitTier tier, CircuitLevel level) {
        return Voltage.fromRank(tier.baseVoltage.rank + level.voltageOffset);
    }

    public static void newCircuit(CircuitTier tier, CircuitLevel level, String id) {
        var item = REGISTRATE.item("circuit/" + id).register();
        CIRCUITS.put(id, new Circuit(tier, level, item));
    }

    public static Circuit getCircuit(String name) {
        return CIRCUITS.get(name);
    }

    public static void newCircuitComponent(String name) {
        CIRCUIT_COMPONENTS.put(name, new CircuitComponent(name));
    }

    public static CircuitComponent getCircuitComponent(String name) {
        return CIRCUIT_COMPONENTS.get(name);
    }

    public static void buildBoards() {
        for (var tier : CircuitTier.values()) {
            var board = REGISTRATE.item("board/" + tier.board).register();
            BOARDS.put(tier, board);
        }
        for (var tier : CircuitTier.values()) {
            var circuitBoard = REGISTRATE.item("circuit_board/" + tier.circuitBoard).register();
            CIRCUIT_BOARDS.put(tier, circuitBoard);
        }
    }

    public static IEntry<Item> board(CircuitTier tier) {
        return BOARDS.get(tier);
    }

    public static IEntry<Item> circuitBoard(CircuitTier tier) {
        return CIRCUIT_BOARDS.get(tier);
    }
}
