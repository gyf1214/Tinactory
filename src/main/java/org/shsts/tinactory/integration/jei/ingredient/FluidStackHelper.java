package org.shsts.tinactory.integration.jei.ingredient;

import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.ISubtypeManager;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidStackHelper implements IIngredientHelper<FluidStackWrapper> {
    private final ISubtypeManager subtypeManager;
    private final IColorHelper colorHelper;

    public FluidStackHelper(ISubtypeManager subtypeManager, IColorHelper colorHelper) {
        this.subtypeManager = subtypeManager;
        this.colorHelper = colorHelper;
    }

    @Override
    public IIngredientType<FluidStackWrapper> getIngredientType() {
        return FluidStackType.INSTANCE;
    }

    @Override
    public String getDisplayName(FluidStackWrapper ingredient) {
        return ingredient.stack().getDisplayName().toString();
    }

    @Override
    public String getUniqueId(FluidStackWrapper ingredient, UidContext context) {
        var fluid = ingredient.stack().getFluid();
        var registryName = fluid.getRegistryName();
        var result = new StringBuilder()
                .append("fluid:")
                .append(registryName);

        var subtypeInfo = subtypeManager.getSubtypeInfo(FluidStackType.INSTANCE, ingredient, context);
        if (!subtypeInfo.isEmpty()) {
            result.append(":");
            result.append(subtypeInfo);
        }

        return result.toString();
    }

    @Override
    public String getWildcardId(FluidStackWrapper ingredient) {
        var loc = ingredient.stack().getFluid().getRegistryName();
        assert loc != null;
        return "fluid:" + loc;
    }

    @Override
    public ResourceLocation getResourceLocation(FluidStackWrapper ingredient) {
        var loc = ingredient.stack().getFluid().getRegistryName();
        assert loc != null;
        return loc;
    }

    @SuppressWarnings("removal")
    @Override
    public String getModId(FluidStackWrapper ingredient) {
        return getResourceLocation(ingredient).getNamespace();
    }

    @SuppressWarnings("removal")
    @Override
    public String getResourceId(FluidStackWrapper ingredient) {
        return getResourceLocation(ingredient).getPath();
    }

    @Override
    public Iterable<Integer> getColors(FluidStackWrapper ingredient) {
        var stack = ingredient.stack();
        var attributes = stack.getFluid().getAttributes();
        var fluidStill = attributes.getStillTexture(stack);
        if (fluidStill != null) {
            var minecraft = Minecraft.getInstance();
            var sprite = minecraft.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(fluidStill);
            var color = attributes.getColor(stack);
            return colorHelper.getColors(sprite, color, 1);
        }
        return Collections.emptyList();
    }

    @Override
    public FluidStackWrapper copyIngredient(FluidStackWrapper ingredient) {
        return new FluidStackWrapper(ingredient.stack().copy());
    }

    @Override
    public ItemStack getCheatItemStack(FluidStackWrapper ingredient) {
        var bucket = ingredient.stack().getFluid().getBucket();
        return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
    }

    @Override
    public FluidStackWrapper normalizeIngredient(FluidStackWrapper ingredient) {
        var copy = copyIngredient(ingredient);
        copy.stack().setAmount(FluidAttributes.BUCKET_VOLUME);
        return copy;
    }


    @Override
    public Collection<ResourceLocation> getTags(FluidStackWrapper ingredient) {
        return ForgeRegistries.FLUIDS.getResourceKey(ingredient.stack().getFluid())
                .flatMap(ForgeRegistries.FLUIDS::getHolder)
                .map(Holder::tags)
                .orElse(Stream.of())
                .map(TagKey::location)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<ResourceLocation> getTagEquivalent(Collection<FluidStackWrapper> ingredients) {
        var fluids = ingredients.stream()
                .map(i -> i.stack().getFluid())
                .collect(Collectors.toSet());

        var tags = ForgeRegistries.FLUIDS.tags();
        assert tags != null;
        return tags.stream().filter(iTag -> iTag.size() == fluids.size() &&
                        fluids.stream().allMatch(iTag::contains))
                .findAny().map(iTag -> iTag.getKey().location());
    }

    @Override
    public String getErrorInfo(@Nullable FluidStackWrapper ingredient) {
        return Objects.toString(ingredient);
    }
}
