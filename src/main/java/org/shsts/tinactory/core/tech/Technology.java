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
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.IRenderable;
import org.shsts.tinactory.core.gui.client.Renderables;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private final ItemStack displayItem;
    @Nullable
    private final Texture displayTexture;
    private final int rank;

    public Technology(List<ResourceLocation> dependIds, long maxProgress, Map<String, Integer> modifiers,
        Optional<Item> displayItem, Optional<ResourceLocation> displayTexture, int rank) {
        this.dependIds = dependIds;
        this.modifiers = modifiers;
        this.maxProgress = maxProgress;
        this.displayItem = displayItem.map(ItemStack::new).orElse(null);
        this.displayTexture = displayTexture.map($ -> new Texture($, 16, 16)).orElse(null);
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
    public DistLazy<IRenderable> getDisplay() {
        return () -> () -> {
            if (displayItem != null) {
                return Renderables.item(displayItem);
            } else if (displayTexture != null) {
                return Renderables.texture(displayTexture);
            } else {
                return Renderables.VOID;
            }
        };
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
            .forGetter(tech -> Optional.ofNullable(tech.displayItem).map(ItemStack::getItem)),
        ResourceLocation.CODEC.optionalFieldOf("display_texture")
            .forGetter(tech -> Optional.ofNullable(tech.displayTexture).map(Texture::loc)),
        Codec.INT.fieldOf("rank").forGetter(tech -> tech.rank)
    ).apply(instance, Technology::new));
}
