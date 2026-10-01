package org.shsts.tinactory.compat.ftbquests;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.integration.tech.TechManagers;

import static org.shsts.tinactory.Tinactory.LOGGER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechTask extends Task {
    private static final long UNRESOLVED_MAX_PROGRESS = 1L;

    private String technologyValue = "";
    @Nullable
    private ResourceLocation technologyId;
    private long maxProgress = UNRESOLVED_MAX_PROGRESS;
    private boolean warnedUnresolved;

    public TechTask(long id, Quest quest) {
        super(id, quest);
    }

    @Override
    public TaskType getType() {
        return TechTaskRegistration.type();
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider provider) {
        super.readData(tag, provider);
        setTechnology(tag.getString("technology"));
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        tag.putString("technology", technologyValue);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        setTechnology(buffer.readUtf());
        maxProgress = Math.max(UNRESOLVED_MAX_PROGRESS, buffer.readVarLong());
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeUtf(technologyValue);
        buffer.writeVarLong(resolveMaxProgress());
    }

    @Override
    public void fillConfigGroup(ConfigGroup group) {
        super.fillConfigGroup(group);
        group.addString("technology", technologyValue, this::setTechnology, "");
    }

    @Override
    public long getMaxProgress() {
        return resolveMaxProgress();
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return 20;
    }

    @Override
    public void submitTask(TeamData teamData, ServerPlayer player, ItemStack itemStack) {
        if (technologyId == null || !checkTaskSequence(teamData) || teamData.isCompleted(this)) {
            return;
        }
        var max = resolveMaxProgress();
        TechManagers.server().teamByPlayer(player).ifPresent(profile -> {
            var progress = Math.clamp(profile.getTechProgress(technologyId), 0L, max);
            if (teamData.getProgress(this) != progress) {
                teamData.setProgress(this, progress);
            }
        });
    }

    @Override
    public void onButtonClicked(Button button, boolean clicked) {
        if (technologyId != null) {
            TechTaskJeiLookup.open(technologyId);
        }
    }

    private void setTechnology(String value) {
        technologyValue = value;
        technologyId = ResourceLocation.tryParse(value);
        maxProgress = UNRESOLVED_MAX_PROGRESS;
        warnedUnresolved = false;
        if (technologyId == null && getQuestFile().isServerSide()) {
            warnUnresolved();
        }
    }

    private long resolveMaxProgress() {
        if (technologyId == null || !getQuestFile().isServerSide()) {
            return maxProgress;
        }
        var technology = TechManagers.server().techByKey(technologyId);
        if (technology.isEmpty()) {
            warnUnresolved();
            return maxProgress;
        }
        maxProgress = Math.max(UNRESOLVED_MAX_PROGRESS, technology.get().getMaxProgress());
        return maxProgress;
    }

    private void warnUnresolved() {
        if (!warnedUnresolved) {
            LOGGER.warn("Unable to resolve Tinactory technology '{}' for FTB task {}", technologyValue,
                getCodeString());
            warnedUnresolved = true;
        }
    }
}
