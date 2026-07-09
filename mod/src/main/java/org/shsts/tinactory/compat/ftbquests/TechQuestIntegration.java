package org.shsts.tinactory.compat.ftbquests;

import dev.architectury.event.EventResult;
import dev.ftb.mods.ftbquests.events.CustomTaskEvent;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.CustomTask;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechManager;
import org.shsts.tinactory.integration.tech.TechHelper;
import org.shsts.tinactory.integration.tech.TechManagers;
import org.shsts.tinactory.integration.util.ServerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.LOGGER;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechQuestIntegration {
    private static final String MARKER_TAG = "tinactory_technology";
    private static final String TECHNOLOGY_TAG_PREFIX = "tinactory_technology_";

    @Nullable
    private ServerQuestFile activeQuestFile = null;
    private final Map<ResourceLocation, List<TechnologyTaskBinding>> tasksByTechnology = new HashMap<>();

    public TechQuestIntegration() {
        CustomTaskEvent.EVENT.register(this::onCustomTask);
        TechManagers.server().onProgressChange(this::onTinactoryTechProgressChanged);
    }

    private static String technologyTag(ResourceLocation technology) {
        return TECHNOLOGY_TAG_PREFIX + normalizeTechnologyId(technology);
    }

    private static String normalizeTechnologyId(ResourceLocation technology) {
        return technology.getNamespace() + "_" + technology.getPath().replace('/', '_');
    }

    private static Optional<ResourceLocation> findTechnology(Set<String> tags, ITechManager manager) {
        ResourceLocation match = null;
        for (var technology : manager.allTechs()) {
            var loc = manager.key(technology).orElseThrow();
            if (tags.contains(technologyTag(loc))) {
                if (match != null) {
                    return Optional.empty();
                }
                match = loc;
            }
        }
        return Optional.ofNullable(match);
    }

    private void refreshActiveQuestFile() {
        if (activeQuestFile != ServerQuestFile.INSTANCE) {
            activeQuestFile = ServerQuestFile.INSTANCE;
            tasksByTechnology.clear();
        }
    }

    private EventResult onCustomTask(CustomTaskEvent event) {
        var task = event.getTask();
        refreshActiveQuestFile();
        var tags = task.getTags();
        if (!tags.contains(MARKER_TAG)) {
            return EventResult.pass();
        }
        var technology = findTechnology(tags, TechManagers.server());
        if (technology.isEmpty()) {
            LOGGER.warn("Unable to wire FTB custom task {}: missing, unknown, or ambiguous Tinactory technology tag",
                task.getCodeString());
            return EventResult.pass();
        }
        var technologyLoc = technology.get();
        var tech = TechManagers.server().techByKey(technologyLoc).orElseThrow();
        task.setMaxProgress(tech.getMaxProgress());
        task.setCheckTimer(20);
        task.setEnableButton(false);
        task.setCheck((data, player) -> checkTechnologyTask(data, player, technologyLoc));
        tasksByTechnology.computeIfAbsent(technologyLoc, unused -> new ArrayList<>())
            .add(new TechnologyTaskBinding(technologyLoc, task));
        return EventResult.pass();
    }

    private void checkTechnologyTask(CustomTask.Data data, ServerPlayer player, ResourceLocation technology) {
        TechManagers.server().teamByPlayer(player)
            .ifPresent(team -> setTaskProgressIfChanged(
                data.teamData(), data.task(), team.getTechProgress(technology)));
    }

    private void onTinactoryTechProgressChanged(ITeamProfile team) {
        if (ServerQuestFile.INSTANCE == null) {
            refreshActiveQuestFile();
            return;
        }
        refreshActiveQuestFile();
        var player = findOnlineTinactoryTeamMember(team);
        if (player.isEmpty()) {
            return;
        }
        var teamData = ServerQuestFile.INSTANCE.getTeamData(player.get());
        if (teamData.isEmpty()) {
            return;
        }
        for (var bindings : tasksByTechnology.values()) {
            for (var binding : bindings) {
                setTaskProgressIfChanged(teamData.get(), binding.task(),
                    team.getTechProgress(binding.technology()));
            }
        }
    }

    private Optional<ServerPlayer> findOnlineTinactoryTeamMember(ITeamProfile team) {
        return TechHelper.scoreboardTeam(team.getName())
            .stream()
            .flatMap(playerTeam -> playerTeam.getPlayers().stream())
            .map(playerName -> ServerUtil.getPlayerList().getPlayerByName(playerName))
            .filter(Objects::nonNull)
            .findFirst();
    }

    private void setTaskProgressIfChanged(TeamData teamData, CustomTask task, long progress) {
        if (teamData.getProgress(task) != progress) {
            teamData.setProgress(task, progress);
        }
    }

    private record TechnologyTaskBinding(ResourceLocation technology, CustomTask task) {}
}
