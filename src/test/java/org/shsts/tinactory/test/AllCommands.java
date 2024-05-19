package org.shsts.tinactory.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {
    private static final DynamicCommandExceptionType TEAM_NOT_EXISTS = new DynamicCommandExceptionType(
            t -> new TextComponent("team %s does not exist".formatted(t)));
    private static final SimpleCommandExceptionType PLAYER_NO_TEAM = new SimpleCommandExceptionType(
            new TextComponent("player has no team"));
    private static final DynamicCommandExceptionType TECH_NOT_FOUND = new DynamicCommandExceptionType(
            t -> new TextComponent("tech %s not found".formatted(t)));

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
        var manager = TechManager.server();
        var team = manager.teamByName(name);

        if (team.isEmpty()) {
            throw TEAM_NOT_EXISTS.create(name);
        }
        manager.removeTeam(team.get().getPlayerTeam());

        ctx.getSource().sendSuccess(new TextComponent("remove %s successfully".formatted(name)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTechProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var progress = LongArgumentType.getLong(ctx, "progress");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player);
        var tech = manager.techByKey(techName);

        if (team.isEmpty()) {
            throw PLAYER_NO_TEAM.create();
        }
        if (tech.isEmpty()) {
            throw TECH_NOT_FOUND.create(techName);
        }
        team.get().setTechProgress(tech.get(), progress);

        ctx.getSource().sendSuccess(new TextComponent("team %s: set tech %s progress=%d"
                .formatted(team.get().getName(), tech.get().getLoc(), progress)), true);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(TinactoryTest.ID)
                .then(Commands.literal("test").executes(AllCommands::testCommand))
                .then(Commands.literal("getTeam").executes(AllCommands::getTeam))
                .then(Commands.literal("removeTeam")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(AllCommands::removeTeam)))
                .then(Commands.literal("setTechProgress")
                        .then(Commands.argument("tech", ResourceLocationArgument.id())
                                .then(Commands.argument("progress", LongArgumentType.longArg(0))
                                        .executes(AllCommands::setTechProgress))));
        dispatcher.register(builder);
    }
}
