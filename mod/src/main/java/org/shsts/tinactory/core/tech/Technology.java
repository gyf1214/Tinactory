package org.shsts.tinactory.core.tech;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.ItemIdRenderDescriptor;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.TextureRenderDescriptor;
import org.shsts.tinactory.core.util.I18n;

import java.util.ArrayList;
import java.util.Comparator;
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
    private final ResourceLocation displayItem;
    @Nullable
    private final ResourceLocation displayTexture;
    private final IRenderDescriptor display;
    private final int rank;

    public Technology(List<ResourceLocation> dependIds, long maxProgress, Map<String, Integer> modifiers,
        Optional<ResourceLocation> displayItem, Optional<ResourceLocation> displayTexture, int rank) {
        this.dependIds = dependIds;
        this.modifiers = modifiers;
        this.maxProgress = maxProgress;
        this.displayItem = displayItem.orElse(null);
        this.displayTexture = displayTexture.orElse(null);
        this.display = this.displayItem != null ? new ItemIdRenderDescriptor(this.displayItem) :
            this.displayTexture != null ? new TextureRenderDescriptor(new Texture(this.displayTexture, 16, 16)) :
                EmptyRenderDescriptor.INSTANCE;
        this.rank = rank;
    }

    @Override
    public ResourceLocation loc() {
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
    public IRenderDescriptor getDisplay() {
        return display;
    }

    @Override
    public Component getDescription() {
        return I18n.tr(getDescriptionId(loc()));
    }

    @Override
    public Component getDetails() {
        return I18n.tr(getDetailsId(loc()));
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
        return "Technology[" + loc + "]";
    }

    public static String getDescriptionId(ResourceLocation loc) {
        return loc.getNamespace() + ".technology." + loc.getPath().replace('/', '.');
    }

    public static String getDetailsId(ResourceLocation loc) {
        return getDescriptionId(loc) + ".details";
    }

    public static final Codec<Technology> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.listOf().fieldOf("depends").forGetter(tech -> tech.dependIds),
        Codec.LONG.fieldOf("max_progress").forGetter(tech -> tech.maxProgress),
        Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("modifiers").forGetter(tech -> tech.modifiers),
        ResourceLocation.CODEC.optionalFieldOf("display_item")
            .forGetter(tech -> Optional.ofNullable(tech.displayItem)),
        ResourceLocation.CODEC.optionalFieldOf("display_texture")
            .forGetter(tech -> Optional.ofNullable(tech.displayTexture)),
        Codec.INT.fieldOf("rank").forGetter(tech -> tech.rank)
    ).apply(instance, Technology::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Technology> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static final Comparator<ITechnology> DISPLAY_ORDER =
        Comparator.comparingInt($ -> ((Technology) $).rank);
}
