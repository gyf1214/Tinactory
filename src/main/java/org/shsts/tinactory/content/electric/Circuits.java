package org.shsts.tinactory.content.electric;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Circuits {
    private static final Map<String, Circuit> CIRCUIT = new HashMap<>();
    private static final Map<String, CircuitComponent> CIRCUIT_COMPONENT = new HashMap<>();
    public static final List<IEntry<Item>> BOULE_LIST = new ArrayList<>();
    public static final Map<String, IEntry<Item>> BOULE = new HashMap<>();
    public static final List<IEntry<Item>> WAFER_RAW_LIST = new ArrayList<>();
    public static final Map<String, IEntry<Item>> WAFER_RAW = new HashMap<>();
    public static final Map<String, IEntry<Item>> WAFER = new HashMap<>();
    public static final Map<String, IEntry<Item>> CHIP = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> BOARD = new HashMap<>();
    private static final Map<CircuitTier, IEntry<Item>> CIRCUIT_BOARD = new HashMap<>();

    public static Voltage getVoltage(CircuitTier tier, CircuitLevel level) {
        return Voltage.fromRank(tier.baseVoltage.rank + level.voltageOffset);
    }

    public static void newCircuit(CircuitTier tier, CircuitLevel level, String id) {
        var voltage = getVoltage(tier, level);
        var item = REGISTRATE.item("circuit/" + id, (properties) -> (Item) new Item(properties) {
            @Override
            public void appendHoverText(ItemStack stack, @Nullable Level world,
                List<Component> tooltip, TooltipFlag isAdvanced) {
                addTooltip(tooltip, "circuit", voltage.displayName());
            }
        }).register();
        CIRCUIT.put(id, new Circuit(tier, level, item));
    }

    public static Circuit getCircuit(String name) {
        return CIRCUIT.get(name);
    }

    public static Collection<Circuit> allCircuits() {
        return CIRCUIT.values();
    }

    public static void newCircuitComponent(String name) {
        CIRCUIT_COMPONENT.put(name, new CircuitComponent(name));
    }

    public static CircuitComponent getCircuitComponent(String name) {
        return CIRCUIT_COMPONENT.get(name);
    }

    public static Collection<CircuitComponent> allCircuitComponents() {
        return CIRCUIT_COMPONENT.values();
    }

    public static void newWafer(String name) {
        var boule = REGISTRATE.item("boule/" + name).register();
        BOULE_LIST.add(boule);
        BOULE.put(name, boule);
        var wafer = REGISTRATE.item("wafer_raw/" + name).register();
        WAFER_RAW_LIST.add(wafer);
        WAFER_RAW.put(name, wafer);
    }

    public static void newChip(String name) {
        WAFER.put(name, REGISTRATE.item("wafer/" + name).register());
        CHIP.put(name, REGISTRATE.item("chip/" + name).register());
    }

    public static void buildBoards() {
        for (var tier : CircuitTier.values()) {
            var board = REGISTRATE.item("board/" + tier.board).register();
            BOARD.put(tier, board);
        }
        for (var tier : CircuitTier.values()) {
            var circuitBoard = REGISTRATE.item("circuit_board/" + tier.circuitBoard).register();
            CIRCUIT_BOARD.put(tier, circuitBoard);
        }
    }

    public static IEntry<Item> board(CircuitTier tier) {
        return BOARD.get(tier);
    }

    public static IEntry<Item> circuitBoard(CircuitTier tier) {
        return CIRCUIT_BOARD.get(tier);
    }
}
