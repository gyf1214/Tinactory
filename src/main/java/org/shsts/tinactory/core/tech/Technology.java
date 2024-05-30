package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Technology implements ITechnology {
    @Nullable
    private ResourceLocation loc = null;
    private final List<ResourceLocation> dependIds;
    private final Set<ITechnology> depends = new HashSet<>();
    public final long maxProgress;
    public final Item displayItem;

    public Technology(List<ResourceLocation> dependIds, long maxProgress, Item displayItem) {
        this.dependIds = dependIds;
        this.maxProgress = maxProgress;
        this.displayItem = displayItem;
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

    // TODO
    @Override
    public Component getName() {
        return new TextComponent(getLoc().toString());
    }

    @Override
    public Collection<ITechnology> getDepends() {
        return depends;
    }

    @Override
    public long getMaxProgress() {
        return maxProgress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Technology that = (Technology) o;
        return Objects.equals(loc, that.loc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loc);
    }

    @Override
    public String toString() {
        return "Technology{%s}".formatted(loc);
    }

    public static final Codec<Technology> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("depends", List.of()).forGetter(tech -> tech.dependIds),
            Codec.LONG.fieldOf("max_progress").forGetter(tech -> tech.maxProgress),
            ForgeRegistries.ITEMS.getCodec().fieldOf("display_item").forGetter(tech -> tech.displayItem)
    ).apply(instance, Technology::new));
}
