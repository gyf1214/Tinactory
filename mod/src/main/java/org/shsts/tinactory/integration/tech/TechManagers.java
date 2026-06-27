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
import org.shsts.tinycorelib.api.network.IPacketType;
import org.shsts.tinycorelib.api.network.PacketDirection;

import java.util.Optional;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

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
    public static final IPacketType<TechInitPacket> TECH_INIT;
    public static final IPacketType<TechUpdatePacket> TECH_UPDATE;

    static {
        TECH_INIT = REGISTRATE.packet("tech_init", TechInitPacket::new)
            .direction(PacketDirection.CLIENTBOUND)
            .handler((packet, ctx) -> client().handleTechInit(packet))
            .register();
        TECH_UPDATE = REGISTRATE.packet("tech_update", TechUpdatePacket::new)
            .direction(PacketDirection.CLIENTBOUND)
            .handler((packet, ctx) -> client().handleTechUpdate(packet))
            .register();
    }

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
