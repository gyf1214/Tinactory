package org.shsts.tinactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.tech.TechManagers;

import static org.shsts.tinactory.AllWorldGens.PLAYER_START_FEATURE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllCommands {
    public static final SimpleCommandExceptionType PLAYER_NO_TEAM = new SimpleCommandExceptionType(
        I18n.tr("tinactory.chat.exception.noTeam"));
    public static final DynamicCommandExceptionType TECH_NOT_FOUND = new DynamicCommandExceptionType(
        t -> I18n.tr("tinactory.chat.exception.noTech", t.toString()));

    private static int syncTeam(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        TechManagers.server().syncTeam(ctx.getSource().getPlayerOrException());
        return Command.SINGLE_SUCCESS;
    }

    private static int setTargetTech(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var manager = TechManagers.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        team.setTargetTech(tech);
        player.sendSystemMessage(I18n.tr("tinactory.chat.setTargetTech.success",
            I18n.tr(ITechnology.getDescriptionId(techName))));
        return Command.SINGLE_SUCCESS;
    }

    private static int resetTargetTech(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var manager = TechManagers.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);

        team.resetTargetTech();
        player.sendSystemMessage(I18n.tr("tinactory.chat.resetTargetTech.success"));
        return Command.SINGLE_SUCCESS;
    }

    private static int createSpawn(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var pos = BlockPosArgument.getSpawnablePos(ctx, "pos");
        var world = ctx.getSource().getLevel();

        PLAYER_START_FEATURE.get().place(FeatureConfiguration.NONE, world,
            world.getChunkSource().getGenerator(), world.random, pos);
        return Command.SINGLE_SUCCESS;
    }

    private static int setTechProgress(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var player = ctx.getSource().getPlayerOrException();
        var techName = ResourceLocationArgument.getId(ctx, "tech");
        var progress = LongArgumentType.getLong(ctx, "progress");
        var manager = TechManagers.server();
        var team = manager.teamByPlayer(player).orElseThrow(PLAYER_NO_TEAM::create);
        var tech = manager.techByKey(techName).orElseThrow(() -> TECH_NOT_FOUND.create(techName));

        team.setTechProgress(tech, progress);
        var msg = "Set tech %s process of %s to %d".formatted(techName, team.getName(), progress);
        player.sendSystemMessage(I18n.raw(msg));
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var builder = Commands.literal(TinactoryKeys.ID)
            .then(Commands.literal("syncTeam").executes(AllCommands::syncTeam))
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
