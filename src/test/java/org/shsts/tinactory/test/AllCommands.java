package org.shsts.tinactory.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {

    private static final DynamicCommandExceptionType TEAM_NOT_EXISTS = new DynamicCommandExceptionType(
            t -> new TextComponent("team %s does not exist"));

    private static int testCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        player.sendMessage(new TextComponent("hello world"), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int getTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var msg = TechManager.server().teamByPlayer(player)
                .map(team -> "%s is team %s".formatted(player, team))
                .orElse("%s has no team".formatted(player));
        player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var name = ctx.getArgument("name", String.class);
        var team = TechManager.server().teamByName(name);
        if (team.isEmpty()) {
            throw TEAM_NOT_EXISTS.create(name);
        }
        TechManager.server().removeTeam(team.get().getPlayerTeam());
        ctx.getSource().sendSuccess(new TextComponent("remove %s successfully".formatted(name)), true);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(TinactoryTest.ID)
                .then(Commands.literal("test").executes(AllCommands::testCommand))
                .then(Commands.literal("getTeam").executes(AllCommands::getTeam))
                .then(Commands.literal("removeTeam")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(AllCommands::removeTeam)));
        dispatcher.register(builder);
    }
}
