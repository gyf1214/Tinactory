package org.shsts.tinactory.api.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachine {
    UUID uuid();

    Optional<ITeamProfile> owner();

    boolean canPlayerInteract(Player player);

    IMachineConfig config();

    /**
     * Called only on server.
     */
    void setConfig(ISetMachineConfigPacket packet, boolean invokeUpdate);

    /**
     * Called only on server.
     */
    default void setConfig(ISetMachineConfigPacket packet) {
        setConfig(packet, true);
    }

    Component title();

    ItemStack icon();

    BlockEntity blockEntity();

    Optional<IProcessor> processor();

    Optional<IContainer> container();

    Optional<IElectricMachine> electric();

    Optional<INetwork> network();

    /**
     * Called when connect to the network.
     */
    void onConnectToNetwork(INetwork network);

    /**
     * Called when disconnect from the network.
     */
    void onDisconnectFromNetwork();

    void buildSchedulings(INetworkComponent.SchedulingBuilder builder);
}
