package org.shsts.tinactory.datagen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.builder.Builder;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.datagen.provider.TechProvider;
import org.shsts.tinycorelib.api.core.ILoc;
import org.shsts.tinycorelib.datagen.api.IDataHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllItems.getComponent;
import static org.shsts.tinactory.content.AllRecipes.RESEARCH_BENCH;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechBuilder<P> extends Builder<JsonObject, P, TechBuilder<P>> implements ILoc {
    public static final int RANK_PER_VOLTAGE = 1000;

    private final ResourceLocation loc;
    private final List<ResourceLocation> depends = new ArrayList<>();
    private long maxProgress = 0;
    private int rank = 0;
    private final Map<String, Integer> modifiers = new HashMap<>();
    @Nullable
    private Supplier<ResourceLocation> displayItem = null;
    @Nullable
    private ResourceLocation displayTexture = null;
    @Nullable
    private Voltage researchVoltage;

    public TechBuilder(P parent, ResourceLocation loc) {
        super(parent);
        this.loc = loc;
    }

    public static <P> TechBuilder<P> factory(IDataHandler<TechProvider> handler,
        P parent, ResourceLocation loc) {
        var builder = new TechBuilder<>(parent, loc);
        return builder.onBuild(() -> builder.onRegister(handler));
    }

    public TechBuilder<P> depends(ResourceLocation... loc) {
        depends.addAll(List.of(loc));
        return this;
    }

    public TechBuilder<P> maxProgress(long maxProgress) {
        this.maxProgress = maxProgress;
        return this;
    }

    public TechBuilder<P> displayItem(ResourceLocation loc) {
        displayItem = () -> loc;
        return this;
    }

    public TechBuilder<P> displayItem(ItemLike item) {
        return displayItem(() -> item);
    }

    public TechBuilder<P> displayItem(Supplier<? extends ItemLike> item) {
        displayItem = () -> item.get().asItem().getRegistryName();
        return this;
    }

    public TechBuilder<P> displayTexture(ResourceLocation val) {
        displayTexture = val;
        return this;
    }

    public TechBuilder<P> researchVoltage(Voltage val) {
        researchVoltage = val;
        return this;
    }

    public TechBuilder<P> modifier(String key, int val) {
        modifiers.put(key, val);
        return this;
    }

    public TechBuilder<P> rank(int val) {
        rank = val;
        return this;
    }

    @Override
    protected JsonObject createObject() {
        assert maxProgress > 0;
        var jo = new JsonObject();
        jo.addProperty("max_progress", maxProgress);
        var rank1 = researchVoltage != null ? RANK_PER_VOLTAGE * researchVoltage.rank : 0;
        jo.addProperty("rank", rank + rank1);
        var ja = new JsonArray();
        depends.forEach(d -> ja.add(d.toString()));
        jo.add("depends", ja);

        if (displayItem != null) {
            jo.addProperty("display_item", displayItem.get().toString());
        } else if (displayTexture != null) {
            jo.addProperty("display_texture", displayTexture.toString());
        }

        var jo1 = new JsonObject();
        for (var entry : modifiers.entrySet()) {
            jo1.addProperty(entry.getKey(), entry.getValue());
        }
        jo.add("modifiers", jo1);
        return jo;
    }

    public void validate(ExistingFileHelper existingFileHelper) {
        for (var loc : depends) {
            if (!existingFileHelper.exists(loc, TechProvider.RESOURCE_TYPE)) {
                throw new IllegalStateException("Technology at %s does not exist".formatted(loc));
            }
        }
    }

    @Override
    public ResourceLocation loc() {
        return loc;
    }

    private void onRegister(IDataHandler<TechProvider> handler) {
        var dataGen = handler.dataGen();
        handler.addCallback(p -> p.addTech(this));
        var description = ITechnology.getDescriptionId(loc);
        var details = ITechnology.getDetailsId(loc);
        dataGen.trackLang(description);
        dataGen.trackLang(details);

        if (researchVoltage != null) {
            var input = getComponent("research_equipment").get(researchVoltage).get();
            RESEARCH_BENCH.recipe(DATA_GEN, loc)
                .target(loc)
                .input(new ProcessingIngredients.ItemIngredient(new ItemStack(input, 1)))
                .voltage(researchVoltage.value)
                .power((long) (0.25 * researchVoltage.value))
                .workTicks(200)
                .build();
        }
    }

    public ResourceLocation register() {
        build();
        return loc;
    }
}
