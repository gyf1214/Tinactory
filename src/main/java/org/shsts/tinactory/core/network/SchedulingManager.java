package org.shsts.tinactory.core.network;

import com.google.common.collect.ImmutableList;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;
import org.shsts.tinactory.api.network.IScheduling;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SchedulingManager {
    private static class SortInfo {
        public int degree = 0;
        public final List<IScheduling> edges = new ArrayList<>();
    }

    @Nullable
    private static List<IScheduling> sortedSchedulings = null;

    public static void onBake(IForgeRegistry<IScheduling> registry, RegistryManager stage) {
        var nodes = new HashMap<IScheduling, SortInfo>();
        for (var scheduling : registry) {
            nodes.put(scheduling, new SortInfo());
        }

        var builder = ImmutableList.<IScheduling>builder();
        Queue<IScheduling> queue = new ArrayDeque<>();
        for (var entry : nodes.entrySet()) {
            entry.getKey().addConditions((s1, s2) -> {
                var v1 = nodes.get(s1);
                var v2 = nodes.get(s2);
                v2.degree++;
                v1.edges.add(s2);
            });
        }

        for (var entry : nodes.entrySet()) {
            if (entry.getValue().degree == 0) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            var s1 = queue.remove();
            builder.add(s1);
            var v1 = nodes.get(s1);
            for (var s2 : v1.edges) {
                var v2 = nodes.get(s2);
                v2.degree--;
                if (v2.degree <= 0) {
                    queue.add(s2);
                }
            }
        }

        sortedSchedulings = builder.build();
        if (sortedSchedulings.size() != nodes.size()) {
            throw new RuntimeException("Cannot sort schedulings");
        }
    }

    public static List<IScheduling> getSortedSchedulings() {
        assert sortedSchedulings != null;
        return sortedSchedulings;
    }
}
