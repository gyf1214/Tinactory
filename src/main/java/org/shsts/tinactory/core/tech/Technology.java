package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Technology implements ITechnology {
    @Nullable
    private ResourceLocation loc = null;
    private final List<ResourceLocation> dependIds;
    private final Set<ITechnology> depends = new HashSet<>();
    public final long maxProgress;

    public Technology(List<ResourceLocation> dependIds, long maxProgress) {
        this.dependIds = dependIds;
        this.maxProgress = maxProgress;
    }

    @Override
    public ResourceLocation getLoc() {
        assert loc != null;
        return loc;
    }

    public void setLoc(ResourceLocation loc) {
        this.loc = loc;
    }

    public void resolve(ITechManager manager) {
        depends.clear();
        dependIds.stream()
                .flatMap(loc -> manager.techByKey(loc).stream())
                .forEach(depends::add);
    }

    @Override
    public Collection<ITechnology> getDepends() {
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
