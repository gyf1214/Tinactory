package org.shsts.tinactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.material.MapColor;
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

    private static int scanOres(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        var world = ctx.getSource().getLevel();
        var pos = ctx.getSource().getPosition();
        var player = ctx.getSource().getPlayerOrException();
        var x = (int) Math.round(pos.x());
        var z = (int) Math.round(pos.z());
        var scale = (byte) IntegerArgumentType.getInteger(ctx, "scale");

        var stack = MapItem.create(world, x, z, scale, true, false);
        var data = MapItem.getSavedData(stack, world);
        assert data != null;
        var scale1 = 1 << data.scale;
        var minX = data.centerX - 64 * scale1;
        var minZ = data.centerZ - 64 * scale1;

        var minY = world.getMinBuildHeight();
        for (var i = 0; i < 128; i++) {
            for (var j = 0; j < 128; j++) {
                var x1 = minX + j * scale1;
                var z1 = minZ + i * scale1;

                MapColor color;
                if (world.hasChunk(SectionPos.blockToSectionCoord(x1), SectionPos.blockToSectionCoord(z1))) {
                    var maxY = world.getHeight(Heightmap.Types.WORLD_SURFACE, x1, z1);
                    var hasOre = false;
                    for (var y = minY; y < maxY; y++) {
                        var block = world.getBlockState(new BlockPos(x1, y, z1));
                        if (block.is(AllTags.ORE_BLOCK)) {
                            hasOre = true;
                            break;
                        }
                    }
                    color = hasOre ? MapColor.COLOR_ORANGE : MapColor.STONE;
                } else {
                    color = MapColor.NONE;
                }

                data.setColor(j, i, color.getPackedId(MapColor.Brightness.NORMAL));
            }
        }
        MapItem.lockMap(world, stack);

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }

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
                            .executes(AllCommands::setTechProgress))))
                .then(Commands.literal("scanOres")
                    .then(Commands.argument("scale", IntegerArgumentType.integer(0, 3))
                        .executes(AllCommands::scanOres))));

        dispatcher.register(builder);
    }
}
