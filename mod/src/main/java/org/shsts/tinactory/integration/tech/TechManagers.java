package org.shsts.tinactory.integration.tech;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.tech.TechInitPacket;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.tech.TechUpdatePacket;
import org.shsts.tinactory.core.tech.TinactorySavedData;

import java.util.Optional;

import static org.shsts.tinactory.Tinactory.CHANNEL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TechManagers {
    private static final String SAVED_DATA_NAME = "tinactory_saved_data";
    @Nullable
    private static ServerTechManager server = null;
    @Nullable
    private static ClientTechManager client = null;
    @Nullable
    private static TinactorySavedData savedData = null;

    private TechManagers() {}

    public static ServerTechManager server() {
        assert server != null;
        return server;
    }

    public static ClientTechManager client() {
        assert client != null;
        return client;
    }

    public static TinactorySavedData savedData() {
        assert savedData != null;
        return savedData;
    }

    public static TechManager get(Level world) {
        return world.isClientSide ? client() : server();
    }

    public static Optional<ITeamProfile> localTeam() {
        return client().localTeamProfile();
    }

    public static void init() {
        server = new ServerTechManager();
        CHANNEL.registerClientPacket(TechInitPacket.class, TechInitPacket::new,
            (packet, ctx) -> client().handleTechInit(packet));
        CHANNEL.registerClientPacket(TechUpdatePacket.class, TechUpdatePacket::new,
            (packet, ctx) -> client().handleTechUpdate(packet));
    }

    public static void initClient() {
        client = new ClientTechManager();
    }

    public static void loadSavedData(ServerLevel world) {
        savedData = world.getDataStorage().computeIfAbsent(
            tag -> TinactorySavedData.fromTag(tag, server()),
            () -> new TinactorySavedData(server()),
            SAVED_DATA_NAME);
    }

    public static void unloadSavedData() {
        savedData = null;
    }
}
