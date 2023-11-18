package org.shsts.tinactory.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class Technology extends ForgeRegistryEntry<Technology> {
    private final List<ResourceLocation> dependIds;
    private final List<Technology> depends = new ArrayList<>();

    public Technology(List<ResourceLocation> dependIds) {
        this.dependIds = dependIds;
    }

    public void resolve() {
        this.depends.clear();
        this.dependIds.stream()
                .flatMap(loc -> TechManager.techByKey(loc).stream())
                .forEach(this.depends::add);
    }

    public static final Codec<Technology> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("depends", List.of()).forGetter(tech -> tech.dependIds)
    ).apply(instance, Technology::new));
}
