package org.shsts.tinactory.datagen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockModelProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.core.recipe.NullRecipe;
import org.shsts.tinactory.datagen.builder.BlockDataBuilder;
import org.shsts.tinactory.datagen.builder.ItemDataBuilder;
import org.shsts.tinactory.datagen.builder.TechBuilder;
import org.shsts.tinactory.datagen.content.Components;
import org.shsts.tinactory.datagen.content.Machines;
import org.shsts.tinactory.datagen.content.Markers;
import org.shsts.tinactory.datagen.content.Materials;
import org.shsts.tinactory.datagen.content.Models;
import org.shsts.tinactory.datagen.content.Technologies;
import org.shsts.tinactory.datagen.content.Veins;
import org.shsts.tinactory.datagen.context.DataContext;
import org.shsts.tinactory.datagen.context.TrackedContext;
import org.shsts.tinactory.datagen.handler.BlockStateHandler;
import org.shsts.tinactory.datagen.handler.DataHandler;
import org.shsts.tinactory.datagen.handler.ItemModelHandler;
import org.shsts.tinactory.datagen.handler.LanguageHandler;
import org.shsts.tinactory.datagen.handler.LootTableHandler;
import org.shsts.tinactory.datagen.handler.RecipeHandler;
import org.shsts.tinactory.datagen.handler.TagsHandler;
import org.shsts.tinactory.datagen.handler.TechHandler;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.tracking.TrackedType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class DataGen implements IRecipeDataConsumer {
    public final String modid;

    public final TrackedContext<Block> blockTrackedCtx;
    public final TrackedContext<Item> itemTrackedCtx;
    public final TrackedContext<String> langTrackedCtx;

    public final BlockStateHandler blockStateHandler;
    public final ItemModelHandler itemModelHandler;
    public final LootTableHandler lootTableHandler;
    public final RecipeHandler recipeHandler;
    public final TechHandler techHandler;
    public final LanguageHandler languageHandler;

    private final Registrate registrate;
    private final List<DataHandler<?>> dataHandlers;
    private final Map<ResourceKey<? extends Registry<?>>, TagsHandler<?>> tagsHandlers;
    private final Set<TrackedContext<?>> trackedContexts;

    @SuppressWarnings("deprecation")
    public DataGen(Registrate registrate) {
        this.modid = registrate.modid;
        this.registrate = registrate;
        this.dataHandlers = new ArrayList<>();
        this.tagsHandlers = new HashMap<>();
        this.trackedContexts = new HashSet<>();

        this.blockTrackedCtx = trackedCtx(TrackedType.BLOCK);
        this.itemTrackedCtx = trackedCtx(TrackedType.ITEM);
        this.langTrackedCtx = trackedCtx(TrackedType.LANG);

        createTagsHandler(Registry.BLOCK);
        createTagsHandler(Registry.ITEM);
        this.blockStateHandler = handler(new BlockStateHandler(this));
        this.itemModelHandler = handler(new ItemModelHandler(this));
        this.lootTableHandler = handler(new LootTableHandler(this));
        this.recipeHandler = handler(new RecipeHandler(this));
        this.techHandler = handler(new TechHandler(this));
        this.languageHandler = handler(new LanguageHandler(this, langTrackedCtx));
    }

    public <U extends Block> BlockDataBuilder<U, DataGen>
    block(ResourceLocation loc, Supplier<U> block) {
        return new BlockDataBuilder<>(this, this, loc, block);
    }

    public <U extends Block> BlockDataBuilder<U, DataGen>
    block(RegistryEntry<U> entry) {
        return new BlockDataBuilder<>(this, this, entry.loc, entry);
    }

    public <U extends Item> ItemDataBuilder<U, DataGen>
    item(ResourceLocation loc, Supplier<U> item) {
        return new ItemDataBuilder<>(this, this, loc, item);
    }

    public <U extends Item> ItemDataBuilder<U, DataGen>
    item(RegistryEntry<U> item) {
        return new ItemDataBuilder<>(this, this, item.loc, item);
    }

    public DataGen blockModel(Consumer<DataContext<BlockModelProvider>> cons) {
        blockStateHandler.addBlockModelCallback(cons);
        return this;
    }

    public DataGen itemModel(Consumer<DataContext<ItemModelProvider>> cons) {
        itemModelHandler.addModelCallback(cons);
        return this;
    }

    @SafeVarargs
    public final <T> DataGen tag(Supplier<? extends T> object, TagKey<T>... tags) {
        assert tags.length > 0;
        tagsHandler(tags[0].registry()).addTags(object, tags);
        return this;
    }

    public <T> DataGen tag(TagKey<T> object, TagKey<T> tag) {
        tagsHandler(tag.registry()).addTag(object, tag);
        return this;
    }

    public DataGen replaceVanillaRecipe(Supplier<RecipeBuilder> recipe) {
        recipeHandler.registerRecipe(cons -> recipe.get().save(cons));
        return this;
    }

    public DataGen vanillaRecipe(Supplier<RecipeBuilder> recipe) {
        return vanillaRecipe(recipe, "");
    }

    public DataGen vanillaRecipe(Supplier<RecipeBuilder> recipe, String suffix) {
        recipeHandler.registerRecipe(cons -> {
            var builder = recipe.get();
            var loc = builder.getResult().getRegistryName();
            assert loc != null;
            var prefix = builder instanceof SimpleCookingRecipeBuilder ? "smelt" : "craft";
            var recipeLoc = new ResourceLocation(modid, prefix + "/" + loc.getPath() + suffix);
            builder.save(cons, recipeLoc);
        });
        return this;
    }

    public DataGen nullRecipe(ResourceLocation loc) {
        recipeHandler.registerRecipe(() -> new NullRecipe(loc));
        return this;
    }

    public DataGen nullRecipe(String loc) {
        return nullRecipe(new ResourceLocation(loc));
    }

    public DataGen nullRecipe(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return nullRecipe(loc);
    }

    public TechBuilder<DataGen> tech(String id) {
        return new TechBuilder<>(this, this, id);
    }

    public <P> TechBuilder<P> tech(P parent, String id) {
        return new TechBuilder<>(this, parent, modLoc(id));
    }

    public void trackLang(String key) {
        langTrackedCtx.trackExtra(key, key);
    }

    public void trackLang(ResourceLocation key) {
        trackLang(key.getNamespace() + '.' + key.getPath().replace('/', '.'));
    }

    public void register(IEventBus modEventBus) {
        modEventBus.addListener(this::onGatherData);
        modEventBus.addListener(this::onCommonSetup);
    }

    @Override
    public String getModId() {
        return modid;
    }

    @Override
    public void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe) {
        recipeHandler.registerRecipe(recipe);
    }

    @Override
    public void registerSmartRecipe(ResourceLocation loc, Supplier<SmartRecipe<?>> recipe) {
        recipeHandler.registerSmartRecipe(recipe);
    }

    private <T extends DataHandler<?>> T handler(T dataHandler) {
        dataHandlers.add(dataHandler);
        return dataHandler;
    }

    private <T> void createTagsHandler(Registry<T> registry) {
        var ret = new TagsHandler<>(this, registry);
        tagsHandlers.put(registry.key(), ret);
        handler(ret);
    }

    @SuppressWarnings("unchecked")
    private <T> TagsHandler<T> tagsHandler(ResourceKey<? extends Registry<T>> key) {
        assert tagsHandlers.containsKey(key);
        return (TagsHandler<T>) tagsHandlers.get(key);
    }

    private <V> TrackedContext<V> trackedCtx(TrackedType<V> type) {
        var ret = new TrackedContext<>(registrate, type);
        trackedContexts.add(ret);
        return ret;
    }

    private void onGatherData(GatherDataEvent event) {
        init();
        for (var handler : dataHandlers) {
            handler.onGatherData(event);
        }
        event.getGenerator().addProvider(new DataProvider() {
            @Override
            public void run(HashCache cache) {
                for (var trackedCtx : trackedContexts) {
                    trackedCtx.postValidate();
                }
            }

            @Override
            public String getName() {
                return "Validation: " + modid;
            }
        });
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        for (var handler : dataHandlers) {
            handler.clear();
        }
    }

    public static final DataGen DATA_GEN = new DataGen(Tinactory.REGISTRATE);

    public static void init() {
        Models.init();
        Technologies.init();
        Materials.init();
        Components.init();
        Machines.init();
        Veins.init();
        Markers.init();
    }
}
