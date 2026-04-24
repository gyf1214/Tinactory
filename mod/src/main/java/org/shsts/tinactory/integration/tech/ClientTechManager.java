package org.shsts.tinactory.integration.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.scores.PlayerTeam;
import org.shsts.tinactory.api.tech.IClientTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinycorelib.api.network.IPacket;
import org.slf4j.Logger;

import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClientTechManager extends TechManager implements IClientTechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final class ClientTeamProfile extends TeamProfile {
        private final PlayerTeam playerTeam;

        private ClientTeamProfile(ClientTechManager techManager, PlayerTeam playerTeam) {
            super(techManager, playerTeam.getName());
            this.playerTeam = playerTeam;
        }
    }

    @Nullable
    private ClientTeamProfile localTeam = null;

    @Nullable
    private ClientTeamProfile getLocalTeam() {
        var team = (PlayerTeam) ClientUtil.getPlayer().getTeam();
        var curTeam = localTeam == null ? null : localTeam.playerTeam;
        if (team != curTeam) {
            localTeam = team == null ? null : new ClientTeamProfile(this, team);
            LOGGER.debug("reset local client team to {}", localTeam);
        }
        return localTeam;
    }

    @Override
    public Optional<ITeamProfile> localTeamProfile() {
        return Optional.ofNullable(getLocalTeam());
    }

    @Override
    public void unload() {
        super.unload();
        localTeam = null;
    }

    public void handleTechInit(TechInitPacket packet) {
        technologies.clear();
        var techs = packet.getTechs();
        techs.forEach(tech -> technologies.put(tech.getLoc(), tech));
        techs.forEach(tech -> tech.resolve(this));
        LOGGER.debug("reload {} techs", technologies.size());
    }

    public void handleTechUpdate(TechUpdatePacket packet) {
        var team = getLocalTeam();
        if (team == null) {
            return;
        }
        for (var progress : packet.getProgress().entrySet()) {
            var oldProgress = team.getTechProgress(progress.getKey());
            team.applyProgressUpdate(progress.getKey(), progress.getValue());
            techByKey(progress.getKey())
                .filter(tech -> oldProgress < tech.getMaxProgress() &&
                    progress.getValue() >= tech.getMaxProgress())
                .ifPresent(team::onTechComplete);
        }

        LOGGER.debug("update {} techs for team {}", packet.getProgress().size(), team.getName());
        if (packet.isUpdateTarget()) {
            var targetTech = packet.getTargetTech().flatMap(this::techByKey).orElse(null);
            team.applyTargetTechUpdate(targetTech);
            LOGGER.debug("update targetTech = {} for team {}", targetTech, team.getName());
        }
        invokeChange(team);
    }

    @Override
    public void broadcastUpdate(ITeamProfile team, IPacket packet) {}
}
