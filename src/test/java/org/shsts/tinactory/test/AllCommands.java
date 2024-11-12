package org.shsts.tinactory.test;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {
    private static final DynamicCommandExceptionType TEAM_NOT_EXISTS = new DynamicCommandExceptionType(
        t -> I18n.raw("team %s does not exist", t));
    private static final SimpleCommandExceptionType PLAYER_NO_TEAM = new SimpleCommandExceptionType(
        I18n.raw("player has no team"));
    private static final DynamicCommandExceptionType TECH_NOT_FOUND = new DynamicCommandExceptionType(
        t -> I18n.raw("tech %s not found", t));

    private static int testCommand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        player.sendMessage(I18n.raw("hello world"), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int getTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var msg = TechManager.server().teamByPlayer(player)
            .map(team -> I18n.raw("%s is team %s", player, team))
            .orElse(I18n.raw("%s has no team", player));
        player.sendMessage(msg, Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var name = ctx.getArgument("name", String.class);
        var manager = TechManager.server();
        var team = manager.teamByName(name).orElseThrow(() -> TEAM_NOT_EXISTS.create(name));

        manager.removeTeam(team.getPlayerTeam());

        ctx.getSource().sendSuccess(new TextComponent("remove %s successfully".formatted(name)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTechProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var progress = LongArgumentType.getLong(ctx, "progress");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        team.setTechProgress(tech, progress);

        ctx.getSource().sendSuccess(I18n.raw("team %s: set tech %s progress=%d",
            team.getName(), tech.getLoc(), progress), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int getTechProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        var progress = team.getTechProgress(tech);

        player.sendMessage(I18n.raw("team %s: tech %s progress=%d", team.getName(), tech.getLoc(), progress),
            Util.NIL_UUID);
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
                        .executes(AllCommands::setTechProgress))))
            .then(Commands.literal("getTechProgress")
                .then(Commands.argument("tech", ResourceLocationArgument.id())
                    .executes(AllCommands::getTechProgress)));
        dispatcher.register(builder);
    }
}
