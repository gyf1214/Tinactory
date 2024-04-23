package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.shsts.tinactory.api.tech.ITechnology;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Technology extends ForgeRegistryEntry<ITechnology> implements ITechnology {
    private final List<ResourceLocation> dependIds;
    private final Set<Technology> depends = new HashSet<>();
    public final long maxProgress;

    public Technology(List<ResourceLocation> dependIds, long maxProgress) {
        this.dependIds = dependIds;
        this.maxProgress = maxProgress;
    }

    public void resolve() {
        depends.clear();
        dependIds.stream()
                .flatMap(loc -> TechManager.techByKey(loc).stream())
                .forEach(depends::add);
    }

    @Override
    public Collection<Technology> getDepends() {
        return depends;
    }

    @Override
    public long getMaxProgress() {
        return maxProgress;
    }

    public static final Codec<Technology> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("depends", List.of()).forGetter(tech -> tech.dependIds),
            Codec.LONG.fieldOf("max_progress").forGetter(tech -> tech.maxProgress)
    ).apply(instance, Technology::new));
}
