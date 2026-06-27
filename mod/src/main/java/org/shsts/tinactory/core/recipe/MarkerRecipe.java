package org.shsts.tinactory.core.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingDisplay;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingResult;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.TextureRenderDescriptor;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.core.ILoc;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MarkerRecipe extends ProcessingRecipe {
    private final ResourceLocation baseTypeId;
    private final String prefix;
    private final boolean requireMultiblock;
    @Nullable
    private final IProcessingIngredient displayIngredient;
    @Nullable
    private final Texture displayTex;

    public final List<Input> markerOutputs;

    public MarkerRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        ResourceLocation baseTypeId, String prefix, boolean requireMultiblock,
        Optional<IProcessingIngredient> displayIngredient, Optional<ResourceLocation> displayTex,
        List<Input> markerOutputs) {
        super(inputs, outputs, workTicks, voltage, power);
        this.baseTypeId = baseTypeId;
        this.prefix = prefix;
        this.requireMultiblock = requireMultiblock;
        this.displayIngredient = displayIngredient.orElse(null);
        this.displayTex = displayTex.map($ -> new Texture($, 16, 16)).orElse(null);
        this.markerOutputs = List.copyOf(markerOutputs);
    }

    public Optional<IProcessingIngredient> displayIngredient() {
        return Optional.ofNullable(displayIngredient);
    }

    public Optional<Texture> displayTexture() {
        return Optional.ofNullable(displayTex);
    }

    @Override
    public IRenderDescriptor display() {
        if (displayTex != null) {
            return new TextureRenderDescriptor(displayTex);
        }
        if (displayIngredient instanceof IProcessingDisplay display) {
            return display.display();
        }
        return super.display();
    }

    @Override
    public Optional<List<Component>> tooltip() {
        return Optional.of(List.of(I18n.tr(ProcessingRecipe.getDescriptionId(loc()))));
    }

    @Override
    public boolean matches(IMachine machine, int parallel) {
        return false;
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) &&
            (!requireMultiblock || machine.isMultiblock());
    }

    public boolean matchesType(IRecipeType<?> type) {
        return baseTypeId.equals(type.loc());
    }

    public boolean matchesType(ResourceLocation type) {
        return baseTypeId.equals(type);
    }

    public boolean matches(ILoc recipe) {
        if (prefix.isEmpty()) {
            return true;
        } else {
            var id = recipe.id();
            return id.equals(prefix) || id.startsWith(prefix + "/");
        }
    }

    public static MapCodec<MarkerRecipe> codec(Codec<IProcessingIngredient> ingredientCodec,
        Codec<IProcessingResult> resultCodec) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ProcessingRecipe.inputCodec(ingredientCodec).listOf().optionalFieldOf("inputs", List.of())
                .forGetter($ -> $.inputs),
            ProcessingRecipe.outputCodec(resultCodec).listOf().optionalFieldOf("outputs", List.of())
                .forGetter($ -> $.outputs),
            Codec.LONG.optionalFieldOf("work_ticks", 1L).forGetter($ -> $.workTicks),
            Codec.LONG.optionalFieldOf("voltage", 0L).forGetter($ -> $.voltage),
            Codec.LONG.optionalFieldOf("power", 1L).forGetter($ -> $.power),
            ResourceLocation.CODEC.fieldOf("base_type").forGetter($ -> $.baseTypeId),
            Codec.STRING.optionalFieldOf("prefix", "").forGetter($ -> $.prefix),
            Codec.BOOL.optionalFieldOf("require_multiblock", false).forGetter($ -> $.requireMultiblock),
            ingredientCodec.optionalFieldOf("display").forGetter($ -> Optional.ofNullable($.displayIngredient)),
            ResourceLocation.CODEC.optionalFieldOf("display_texture")
                .forGetter($ -> $.displayTex == null ? Optional.empty() : Optional.of($.displayTex.loc())),
            ProcessingRecipe.inputCodec(ingredientCodec).listOf().optionalFieldOf("marker_outputs", List.of())
                .forGetter($ -> $.markerOutputs)
        ).apply(instance, MarkerRecipe::new));
    }
}
