package org.shsts.tinactory.content.recipe;

import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.recipe.IRecipeSerializer;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CleanRecipe extends ProcessingRecipe {
    private static final Logger LOGGER = LogUtils.getLogger();

    public final double minCleanness;
    public final double maxCleanness;

    protected CleanRecipe(Builder builder) {
        super(builder);
        this.minCleanness = builder.minCleanness;
        this.maxCleanness = builder.maxCleanness;
    }

    protected double getCleanness(IMachine machine, Level world, BlockPos pos) {
        return Cleanroom.getCleanness(world, pos);
    }

    private boolean checkCleanness(IMachine machine, Random random) {
        var blockEntity = machine.blockEntity();
        var world = blockEntity.getLevel();
        assert world != null;
        var pos = blockEntity.getBlockPos();

        var cleanness = getCleanness(machine, world, pos);

        LOGGER.debug("check cleanness pos={}:{}, cleanness={}",
            world.dimension().location(), pos, cleanness);

        if (cleanness >= maxCleanness) {
            LOGGER.debug("recipe rate=1");
            return true;
        }
        if (cleanness <= minCleanness) {
            LOGGER.debug("recipe rate=0");
            return false;
        }
        var rate = (cleanness - minCleanness) / (maxCleanness - minCleanness);
        LOGGER.debug("recipe rate={}", rate);
        return random.nextDouble() < rate;
    }

    @Override
    public void insertOutputs(IMachine machine, Random random) {
        if (checkCleanness(machine, random)) {
            super.insertOutputs(machine, random);
        }
    }

    public static class Builder extends BuilderBase<CleanRecipe, Builder> {
        private double minCleanness = 0d;
        private double maxCleanness = 0d;

        public Builder(IRecipeType<Builder> parent, ResourceLocation loc) {
            super(parent, loc);
        }

        public Builder requireCleanness(double min, double max) {
            minCleanness = min;
            maxCleanness = max;
            return self();
        }

        @Override
        protected CleanRecipe createObject() {
            return new CleanRecipe(this);
        }
    }

    protected static class Serializer extends ProcessingRecipe.Serializer<CleanRecipe, Builder> {
        @Override
        protected Builder buildFromJson(IRecipeType<Builder> type, ResourceLocation loc, JsonObject jo) {
            return super.buildFromJson(type, loc, jo)
                .requireCleanness(GsonHelper.getAsDouble(jo, "min_cleanness", 0d),
                    GsonHelper.getAsDouble(jo, "max_cleanness", 0d));
        }

        @Override
        public void toJson(JsonObject jo, CleanRecipe recipe) {
            super.toJson(jo, recipe);
            jo.addProperty("min_cleanness", recipe.minCleanness);
            jo.addProperty("max_cleanness", recipe.maxCleanness);
        }
    }

    public static IRecipeSerializer<CleanRecipe, Builder> SERIALIZER = new Serializer();
}
