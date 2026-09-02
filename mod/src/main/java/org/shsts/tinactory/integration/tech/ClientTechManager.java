package org.shsts.tinactory.integration.tech;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.tech.IClientTechManager;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.network.IPacket;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClientTechManager extends TechManager implements IClientTechManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final class ClientTeamProfile extends TeamProfile {
        private ClientTeamProfile(ClientTechManager techManager, String profileId) {
            super(techManager, profileId, I18n.raw(profileId));
        }
    }

    @Nullable
    private ClientTeamProfile localTeam = null;
    private boolean techInitialized = false;
    private final Set<Runnable> initCallbacks = new HashSet<>();

    @Override
    public Optional<ITeamProfile> localTeamProfile() {
        return Optional.ofNullable(localTeam);
    }

    @Override
    public boolean techInitialized() {
        return techInitialized;
    }

    @Override
    public void onTechInit(Runnable callback) {
        initCallbacks.add(callback);
    }

    @Override
    public void removeTechInitListener(Runnable callback) {
        initCallbacks.remove(callback);
    }

    @Override
    public void unload() {
        super.unload();
        localTeam = null;
        techInitialized = false;
    }

    public void handleTechInit(TechInitPacket packet) {
        unload();
        for (var entry : packet.entries()) {
            putTech(entry.loc(), entry.technology());
        }
        for (var entry : packet.entries()) {
            entry.technology().resolve(this);
        }
        techInitialized = true;
        LOGGER.debug("reload {} techs", technologies.size());
        for (var cb : initCallbacks) {
            cb.run();
        }
    }

    public void handleTechUpdate(TechUpdatePacket packet) {
        if (packet.getUpdateType() == TechUpdatePacket.UpdateType.CLEAR) {
            localTeam = null;
            LOGGER.debug("clear local client team");
            return;
        }
        var profileId = packet.getProfileId().orElse(null);
        if (profileId == null) {
            LOGGER.warn("ignore tech update without a profile ID");
            return;
        }
        var team = localTeam;
        if (packet.getUpdateType() == TechUpdatePacket.UpdateType.FULL) {
            team = new ClientTeamProfile(this, profileId);
            localTeam = team;
        } else if (team == null || !team.getName().equals(profileId)) {
            LOGGER.debug("ignore tech update for non-current profile {}", profileId);
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
