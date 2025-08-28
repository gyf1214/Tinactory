package org.shsts.tinactory.content.electric;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Circuits {
    public static final Set<Circuit> CIRCUITS = new HashSet<>();
    public static final Map<String, CircuitComponent> COMPONENTS = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> BOARDS = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> CIRCUIT_BOARDS = new HashMap<>();

    public static Voltage getVoltage(CircuitTier tier, CircuitLevel level) {
        return Voltage.fromRank(tier.baseVoltage.rank + level.voltageOffset);
    }

    public static Circuit circuit(CircuitTier tier, CircuitLevel level, String id) {
        var item = REGISTRATE.item("circuit/" + id).register();
        var ret = new Circuit(tier, level, item);
        CIRCUITS.add(ret);
        return ret;
    }

    public static CircuitComponent circuitComponent(String name) {
        var ret = new CircuitComponent(name);
        COMPONENTS.put(name, ret);
        return ret;
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
