package org.shsts.tinactory.content;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.shsts.tinycorelib.api.blockentity.IReturnEvent;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllEvents {
    public record OnUseArg(Player player, InteractionHand hand, BlockHitResult hitResult) {}

    public static final IEntry<IReturnEvent<AllEvents.OnUseArg, InteractionResult>> SERVER_USE;

    private AllEvents() {}

    static {
        SERVER_USE = REGISTRATE.returnEvent("server_use", InteractionResult.PASS);
    }

    public static void init() {}
}
