package org.shsts.tinactory.datagen;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.datagen.builder.TechBuilder;
import org.shsts.tinactory.datagen.content.Technologies;
import org.shsts.tinactory.datagen.content.Veins;
import org.shsts.tinactory.datagen.handler.DataHandler;
import org.shsts.tinactory.datagen.handler.LanguageHandler;
import org.shsts.tinactory.datagen.handler.RecipeHandler;
import org.shsts.tinactory.datagen.handler.TechHandler;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class DataGen implements IRecipeDataConsumer {
    public final String modid;
    public final TechHandler techHandler;
    public final RecipeHandler recipeHandler;
    public final LanguageHandler languageHandler;

    private final List<DataHandler<?>> dataHandlers;

    public DataGen(String modid) {
        this.modid = modid;
        this.dataHandlers = new ArrayList<>();

        this.techHandler = handler(new TechHandler(this));
        this.recipeHandler = handler(new RecipeHandler(this));
        this.languageHandler = handler(new LanguageHandler(this));
    }

    public TechBuilder<DataGen> tech(String id) {
        return new TechBuilder<>(this, this, id);
    }

    public <P> TechBuilder<P> tech(P parent, ResourceLocation loc) {
        return new TechBuilder<>(this, parent, loc);
    }

    public void register(IEventBus modEventBus) {
        for (var handler : dataHandlers) {
            modEventBus.addListener(handler::onGatherData);
        }
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

    private <T extends DataHandler<?>> T handler(T dataHandler) {
        dataHandlers.add(dataHandler);
        return dataHandler;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        for (var handler : dataHandlers) {
            handler.clear();
        }
    }

    public static final Registrate REGISTRATE = new Registrate(Tinactory.ID);
    public static final DataGen DATA_GEN = new DataGen(Tinactory.ID);

    public static void init(IEventBus modEventBus) {
        Technologies.init();
        Veins.init();

        REGISTRATE.register(modEventBus);
        DATA_GEN.register(modEventBus);
    }
}
