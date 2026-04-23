package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TagIngredient extends ItemsIngredient {
    public static final String CODEC_NAME = "tag_ingredient";

    private final TagKey<Item> tag;

    public TagIngredient(TagKey<Item> tag, int amount) {
        super(Ingredient.of(tag), amount);
        this.tag = tag;
    }

    public TagKey<Item> tag() {
        return tag;
    }

    @Override
    public String codecName() {
        return CODEC_NAME;
    }

    public static Codec<TagIngredient> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
            TagKey.codec(Registry.ITEM_REGISTRY).fieldOf("tag").forGetter(TagIngredient::tag),
            Codec.INT.fieldOf("amount").forGetter($ -> $.amount)
        ).apply(instance, TagIngredient::new));
    }
}
