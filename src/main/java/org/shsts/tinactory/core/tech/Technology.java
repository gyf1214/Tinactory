package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Technology extends ForgeRegistryEntry<Technology> {
    private final List<ResourceLocation> dependIds;
    public final Set<Technology> depends = new HashSet<>();
    public final long maxProgress;

    public Technology(List<ResourceLocation> dependIds, long maxProgress) {
        this.dependIds = dependIds;
        this.maxProgress = maxProgress;
    }

    public void resolve() {
        this.depends.clear();
        this.dependIds.stream()
                .flatMap(loc -> TechManager.techByKey(loc).stream())
                .forEach(this.depends::add);
    }

    public static final Codec<Technology> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("depends", List.of()).forGetter(tech -> tech.dependIds),
            Codec.LONG.fieldOf("max_progress").forGetter(tech -> tech.maxProgress)
    ).apply(instance, Technology::new));
}
