package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Technology implements ITechnology {
    @Nullable
    private ResourceLocation loc = null;
    private final List<ResourceLocation> dependIds;
    private final List<ITechnology> depends = new ArrayList<>();
    private final Map<String, Integer> modifiers;
    private final long maxProgress;
    @Nullable
    private final Item displayItem;
    @Nullable
    private final ResourceLocation displayTexture;
    private final int rank;

    public Technology(List<ResourceLocation> dependIds, long maxProgress, Map<String, Integer> modifiers,
        Optional<Item> displayItem, Optional<ResourceLocation> displayTexture, int rank) {
        this.dependIds = dependIds;
        this.modifiers = modifiers;
        this.maxProgress = maxProgress;
        this.displayItem = displayItem.orElse(null);
        this.displayTexture = displayTexture.orElse(null);
        this.rank = rank;
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
    public List<ITechnology> getDepends() {
        return depends;
    }

    @Override
    public Map<String, Integer> getModifiers() {
        return modifiers;
    }

    @Override
    public long getMaxProgress() {
        return maxProgress;
    }

    @Override
    public ItemStack getDisplayItem() {
        return displayItem != null ? new ItemStack(displayItem) : ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return displayTexture != null ? displayTexture : modLoc("void");
    }

    /**
     * Only compares rank, does not imply equal.
     */
    @Override
    public int compareTo(ITechnology o) {
        return Integer.compare(rank, ((Technology) o).rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
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
        ResourceLocation.CODEC.listOf().optionalFieldOf("depends", Collections.emptyList())
            .forGetter(tech -> tech.dependIds),
        Codec.LONG.fieldOf("max_progress").forGetter(tech -> tech.maxProgress),
        Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("modifiers").forGetter(tech -> tech.modifiers),
        ForgeRegistries.ITEMS.getCodec().optionalFieldOf("display_item")
            .forGetter(tech -> Optional.ofNullable(tech.displayItem)),
        ResourceLocation.CODEC.optionalFieldOf("display_texture")
            .forGetter(tech -> Optional.ofNullable(tech.displayTexture)),
        Codec.INT.fieldOf("rank").forGetter(tech -> tech.rank)
    ).apply(instance, Technology::new));
}
