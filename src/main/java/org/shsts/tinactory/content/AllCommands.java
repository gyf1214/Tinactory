package org.shsts.tinactory.content;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TranslatableComponent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.tech.TechManager;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {
    private static final SimpleCommandExceptionType PLAYER_HAS_TEAM = new SimpleCommandExceptionType(
            new TranslatableComponent("tinactory.chat.exception.hasTeam"));
    private static final SimpleCommandExceptionType PLAYER_NO_TEAM = new SimpleCommandExceptionType(
            new TranslatableComponent("tinactory.chat.exception.noTeam"));
    private static final DynamicCommandExceptionType TEAM_ALREADY_EXISTS = new DynamicCommandExceptionType(
            t -> new TranslatableComponent("tinactory.chat.exception.teamExists", t));

    private static int createTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var name = ctx.getArgument("name", String.class);
        if (TechManager.INSTANCE.teamByPlayer(player).isPresent()) {
            throw PLAYER_HAS_TEAM.create();
        }
        if (TechManager.INSTANCE.teamByName(name).isPresent()) {
            throw TEAM_ALREADY_EXISTS.create(name);
        }
        TechManager.INSTANCE.newTeam(player, name);
        player.sendMessage(new TranslatableComponent("tinactory.chat.createTeam.success",
                name, player.getDisplayName()), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int leaveTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var team = TechManager.INSTANCE.teamByPlayer(player);
        if (team.isEmpty()) {
            throw PLAYER_NO_TEAM.create();
        }
        var teamName = team.get().getName();
        TechManager.INSTANCE.leaveTeam(player);
        player.sendMessage(new TranslatableComponent("tinactory.chat.leaveTeam.success",
                player.getDisplayName(), teamName), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(Tinactory.ID)
                .then(Commands.literal("createTeam")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(AllCommands::createTeam)))
                .then(Commands.literal("leaveTeam").executes(AllCommands::leaveTeam));

        dispatcher.register(builder);
    }
}
