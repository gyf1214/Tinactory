package org.shsts.tinactory.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.tech.TechManager;

import static org.shsts.tinactory.content.AllCommands.PLAYER_NO_TEAM;
import static org.shsts.tinactory.content.AllCommands.TECH_NOT_FOUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TestCommands {
    private static int setTechProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var progress = LongArgumentType.getLong(ctx, "progress");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        team.setTechProgress(tech, progress);
        var msg = "Set tech %s process of %s to %d".formatted(tech.getLoc(), team.getName(), progress);
        player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void onRegisterCommand(RegisterCommandsEvent event) {
        var builder = Commands.literal("setTechProgress")
            .then(Commands.argument("tech", ResourceLocationArgument.id())
                .then(Commands.argument("progress", LongArgumentType.longArg(0))
                    .executes(TestCommands::setTechProgress)));

        var dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal(Tinactory.ID)
            .then(Commands.literal("admin").then(builder)));
    }
}
