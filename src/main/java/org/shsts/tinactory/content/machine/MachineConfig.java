package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

import static org.shsts.tinactory.core.util.GeneralUtil.optionalCastor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineConfig implements INBTSerializable<CompoundTag> {
    private boolean autoDumpItem = false;
    private boolean autoDumpFluid = false;
    @Nullable
    private ProcessingRecipe<?> targetRecipe = null;
    @Nullable
    private ResourceLocation targetRecipeLoc = null;

    public boolean isAutoDumpItem() {
        return autoDumpItem;
    }

    public boolean isAutoDumpFluid() {
        return autoDumpFluid;
    }

    @Nullable
    public ProcessingRecipe<?> getTargetRecipe() {
        return targetRecipe;
    }

    @Nullable
    public ResourceLocation getTargetRecipeLoc() {
        return targetRecipe != null ? targetRecipe.getId() : null;
    }

    @Nullable
    private ProcessingRecipe<?> recipeByKey(Level world, @Nullable ResourceLocation loc) {
        return Optional.ofNullable(loc)
                .flatMap(world.getRecipeManager()::byKey)
                .flatMap(optionalCastor(ProcessingRecipe.class))
                .orElse(null);
    }

    public void onLoad(Level world) {
        targetRecipe = recipeByKey(world, targetRecipeLoc);
        targetRecipeLoc = null;
    }

    public void apply(Level world, SetMachinePacket packet) {
        if (packet.getAutoDumpItem() != null) {
            autoDumpItem = packet.getAutoDumpItem();
        }
        if (packet.getAutoDumpFluid() != null) {
            autoDumpFluid = packet.getAutoDumpFluid();
        }
        if (packet.isResetTargetRecipe()) {
            targetRecipe = null;
        } else if (packet.getTargetRecipeLoc() != null) {
            targetRecipe = recipeByKey(world, packet.getTargetRecipeLoc());
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putBoolean("autoDumpItem", autoDumpItem);
        tag.putBoolean("autoDumpFluid", autoDumpFluid);
        if (targetRecipe != null) {
            tag.putString("targetRecipe", targetRecipe.getId().toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        autoDumpItem = tag.getBoolean("autoDumpItem");
        autoDumpFluid = tag.getBoolean("autoDumpFluid");
        if (tag.contains("targetRecipe", Tag.TAG_STRING)) {
            targetRecipeLoc = new ResourceLocation(tag.getString("targetRecipe"));
        } else {
            targetRecipeLoc = null;
        }
    }
}
