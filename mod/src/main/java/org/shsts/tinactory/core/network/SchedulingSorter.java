package org.shsts.tinactory.core.network;

import com.google.common.collect.ImmutableList;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.network.IScheduling;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Queue;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class SchedulingSorter {
    private static final class SortInfo {
        private int degree = 0;
        private final List<IScheduling> edges = new ArrayList<>();
    }

    private SchedulingSorter() {}

    public static List<IScheduling> sort(Iterable<IScheduling> schedulings) {
        var nodes = new LinkedHashMap<IScheduling, SortInfo>();
        for (var scheduling : schedulings) {
            nodes.put(scheduling, new SortInfo());
        }

        for (var scheduling : nodes.keySet()) {
            scheduling.addConditions((left, right) -> {
                var leftInfo = nodes.get(left);
                var rightInfo = nodes.get(right);
                if (leftInfo == null || rightInfo == null) {
                    return;
                }
                rightInfo.degree++;
                leftInfo.edges.add(right);
            });
        }

        Queue<IScheduling> queue = new ArrayDeque<>();
        for (var entry : nodes.entrySet()) {
            if (entry.getValue().degree == 0) {
                queue.add(entry.getKey());
            }
        }

        var builder = ImmutableList.<IScheduling>builder();
        while (!queue.isEmpty()) {
            var scheduling = queue.remove();
            builder.add(scheduling);
            var info = nodes.get(scheduling);
            for (var next : info.edges) {
                var nextInfo = nodes.get(next);
                nextInfo.degree--;
                if (nextInfo.degree == 0) {
                    queue.add(next);
                }
            }
        }

        var sorted = builder.build();
        if (sorted.size() != nodes.size()) {
            throw new RuntimeException("Cannot sort schedulings");
        }
        return sorted;
    }
}
