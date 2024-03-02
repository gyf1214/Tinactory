package org.shsts.tinactory.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
    private static int testCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        player.sendMessage(new TextComponent("hello world"), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int getTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var msg = TechManager.teamByPlayer(player)
                .map(team -> "%s is team %s".formatted(player, team))
                .orElse("%s has no team".formatted(player));
        player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int createTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        TechManager.newTeam(player, ctx.getArgument("name", String.class));
        player.sendMessage(new TextComponent("create team success"), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int invalidatePlayer(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        TechManager.invalidatePlayer(player);
        player.sendMessage(new TextComponent("invalidate player success"), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(TinactoryTest.ID)
                .then(Commands.literal("test").executes(AllCommands::testCommand))
                .then(Commands.literal("getTeam").executes(AllCommands::getTeam))
                .then(Commands.literal("createTeam")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(AllCommands::createTeam)))
                .then(Commands.literal("invalidatePlayer").executes(AllCommands::invalidatePlayer));
        dispatcher.register(builder);
    }
}
