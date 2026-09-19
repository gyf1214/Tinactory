package org.shsts.tinactory.content.logistics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.Tag;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.core.machine.MachineConfigType;
import org.shsts.tinactory.core.util.CodecHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record LogisticWorkerConfig(boolean valid,
    @Nullable LogisticComponent.PortKey from,
    @Nullable LogisticComponent.PortKey to,
    FilterEntry filter) {
    public static final Codec<LogisticWorkerConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.fieldOf("valid").forGetter(LogisticWorkerConfig::valid),
            portKeyCodec("from").forGetter(LogisticWorkerConfig::optionalFrom),
            portKeyCodec("to").forGetter(LogisticWorkerConfig::optionalTo),
            FilterEntry.MAP_CODEC.forGetter(LogisticWorkerConfig::filter)
        ).apply(instance, LogisticWorkerConfig::new));

    private static MapCodec<Optional<LogisticComponent.PortKey>> portKeyCodec(String prefix) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            UUIDUtil.CODEC.optionalFieldOf(prefix + "Machine")
                .forGetter(key -> key.map(LogisticComponent.PortKey::machineId)),
            Codec.INT.optionalFieldOf(prefix + "PortIndex")
                .forGetter(key -> key.map(LogisticComponent.PortKey::portIndex))
        ).apply(instance, (machine, port) -> (machine.isPresent() && port.isPresent()) ?
            Optional.of(new LogisticComponent.PortKey(machine.get(), port.get())) :
            Optional.empty()));
    }

    public static final LogisticWorkerConfig EMPTY = new LogisticWorkerConfig(false,
        (LogisticComponent.PortKey) null, null, FilterEntry.EMPTY);

    public LogisticWorkerConfig(boolean valid, Optional<LogisticComponent.PortKey> from,
        Optional<LogisticComponent.PortKey> to, FilterEntry filter) {
        this(valid, from.orElse(null), to.orElse(null), filter);
    }

    public Optional<LogisticComponent.PortKey> optionalFrom() {
        return Optional.ofNullable(from);
    }

    public Optional<LogisticComponent.PortKey> optionalTo() {
        return Optional.ofNullable(to);
    }

    public FilterEntry.Type filterType() {
        return filter.type();
    }

    public LogisticWorkerConfig setValid(boolean val) {
        return new LogisticWorkerConfig(val, from, to, filter);
    }

    public LogisticWorkerConfig setFrom(UUID machineId, int portIndex) {
        var key = new LogisticComponent.PortKey(machineId, portIndex);
        return new LogisticWorkerConfig(valid, key, to, filter);
    }

    public LogisticWorkerConfig resetFrom() {
        return new LogisticWorkerConfig(valid, null, to, filter);
    }

    public LogisticWorkerConfig setTo(UUID machineId, int portIndex) {
        var key = new LogisticComponent.PortKey(machineId, portIndex);
        return new LogisticWorkerConfig(valid, from, key, filter);
    }

    public LogisticWorkerConfig resetTo() {
        return new LogisticWorkerConfig(valid, from, null, filter);
    }

    public LogisticWorkerConfig setFilter(FilterEntry val) {
        return new LogisticWorkerConfig(valid, from, to, val);
    }

    public static IMachineConfigType<List<LogisticWorkerConfig>> configType() {
        return new MachineConfigType<>(CODEC.listOf(), null) {
            private record ConfigEntry(int index, LogisticWorkerConfig config) {}

            @Override
            public Optional<List<LogisticWorkerConfig>> fixLegacyConfig(HolderLookup.Provider provider,
                Map<String, Tag> unknownTags) {
                var list = new ArrayList<ConfigEntry>();
                var it = unknownTags.entrySet().iterator();
                while (it.hasNext()) {
                    var entry = it.next();
                    var key = entry.getKey();
                    if (key.startsWith("workerConfig_")) {
                        var i = -1;
                        try {
                            i = Integer.parseInt(key.substring("workerConfig_".length()));
                        } catch (NumberFormatException ignored) {}
                        if (i >= 0) {
                            var val = CodecHelper.parseTag(provider, CODEC, entry.getValue());
                            list.add(new ConfigEntry(i, val));
                            it.remove();
                        }
                    }
                }
                if (list.isEmpty()) {
                    return Optional.empty();
                }
                var list1 = list.stream()
                    .sorted(Comparator.comparing(ConfigEntry::index))
                    .map(ConfigEntry::config)
                    .toList();
                return Optional.of(list1);
            }
        };
    }
}
