package org.shsts.tinactory.content;

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
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import java.util.Random;

import static org.shsts.tinactory.content.AllWorldGens.PLAYER_START_FEATURE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {
    public static final SimpleCommandExceptionType PLAYER_HAS_TEAM = new SimpleCommandExceptionType(
        I18n.tr("tinactory.chat.exception.hasTeam"));
    public static final SimpleCommandExceptionType PLAYER_NO_TEAM = new SimpleCommandExceptionType(
        I18n.tr("tinactory.chat.exception.noTeam"));
    public static final DynamicCommandExceptionType TEAM_ALREADY_EXISTS = new DynamicCommandExceptionType(
        t -> I18n.tr("tinactory.chat.exception.teamExists", t));
    public static final DynamicCommandExceptionType TECH_NOT_FOUND = new DynamicCommandExceptionType(
        t -> I18n.tr("tinactory.chat.exception.noTech", t));

    private static int createTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var name = StringArgumentType.getString(ctx, "name");
        var manager = TechManager.server();

        if (manager.teamByPlayer(player).isPresent()) {
            throw PLAYER_HAS_TEAM.create();
        }
        if (manager.teamByName(name).isPresent()) {
            throw TEAM_ALREADY_EXISTS.create(name);
        }
        manager.newTeam(player, name);
        player.sendMessage(I18n.tr("tinactory.chat.createTeam.success",
            name, player.getDisplayName()), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int addPlayerToTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var player2 = EntityArgument.getPlayer(ctx, "player");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);

        if (manager.teamByPlayer(player2).isPresent()) {
            throw PLAYER_HAS_TEAM.create();
        }

        manager.addPlayerToTeam(player2, team);
        player.sendMessage(I18n.tr("tinactory.chat.addPlayerToTeam.success",
            player2.getDisplayName(), team.getName()), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int leaveTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);

        manager.leaveTeam(player);
        player.sendMessage(I18n.tr("tinactory.chat.leaveTeam.success",
            player.getDisplayName(), team.getName()), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTargetTech(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        team.setTargetTech(tech);
        player.sendMessage(I18n.tr("tinactory.chat.setTargetTech.success", team.getName(),
            I18n.tr(tech.getDescriptionId())), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetTargetTech(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var manager = TechManager.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);

        team.resetTargetTech();
        player.sendMessage(I18n.tr("tinactory.chat.resetTargetTech.success", team.getName()),
            Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    private static int createSpawn(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
        var world = ctx.getSource().getLevel();

        PLAYER_START_FEATURE.get().place(FeatureConfiguration.NONE, world,
            world.getChunkSource().getGenerator(), new Random(), pos);
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
        var msg = "Set tech %s process of %s to %d".formatted(tech.getLoc(), team.getName(), progress);
        player.sendMessage(new TextComponent(msg), Util.NIL_UUID);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(Tinactory.ID)
            .then(Commands.literal("createTeam")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(AllCommands::createTeam)))
            .then(Commands.literal("addPlayerToTeam")
                .then(Commands.argument("player", EntityArgument.player())
                    .executes(AllCommands::addPlayerToTeam)))
            .then(Commands.literal("leaveTeam").executes(AllCommands::leaveTeam))
            .then(Commands.literal("setTargetTech")
                .then(Commands.argument("tech", ResourceLocationArgument.id())
                    .executes(AllCommands::setTargetTech))
                .executes(AllCommands::resetTargetTech))
            .then(Commands.literal("admin").requires(p -> p.hasPermission(2))
                .then(Commands.literal("createSpawn")
                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(AllCommands::createSpawn)))
                .then(Commands.literal("setTechProgress")
                    .then(Commands.argument("tech", ResourceLocationArgument.id())
                        .then(Commands.argument("progress", LongArgumentType.longArg(0))
                            .executes(AllCommands::setTechProgress)))));

        dispatcher.register(builder);
    }
}
