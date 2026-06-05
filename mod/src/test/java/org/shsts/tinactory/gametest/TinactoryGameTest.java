package org.shsts.tinactory.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.BeforeBatch;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.AllItems;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.gametest.dependency.DependencyChecker;
import org.shsts.tinactory.integration.network.CableBlock;
import org.shsts.tinactory.integration.network.MachineBlock;
import org.shsts.tinactory.integration.network.WorldNetworkManagers;

import static org.shsts.tinactory.AllCapabilities.MACHINE;

@GameTestHolder(TinactoryKeys.ID)
public final class TinactoryGameTest {
    private static final String TEAM_NAME = "gametest";

    @BeforeBatch(batch = "defaultBatch")
    public static void setupGameTestTeam(ServerLevel world) {
        var scoreboard = world.getScoreboard();
        if (scoreboard.getPlayerTeam(TEAM_NAME) == null) {
            scoreboard.addPlayerTeam(TEAM_NAME);
        }
    }

    @GameTest
    public static void testSucceed(GameTestHelper helper) {
        helper.succeed();
    }

    @GameTest
    public static void testDependency(GameTestHelper helper) {
        if (DependencyChecker.runRuntimeCheck(helper.getLevel())) {
            helper.succeed();
        } else {
            helper.fail("Tinactory dependency checker failed");
        }
    }

    @GameTest(timeoutTicks = 40)
    public static void testMockPlayerUseCreatesMachineNetwork(GameTestHelper helper) {
        var machinePos = new BlockPos(1, 1, 1);
        helper.setBlock(machinePos, machineState(Direction.EAST));
        useWithTeamMockPlayer(helper, machinePos);
        helper.runAfterDelay(12, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteMachinePos = helper.absolutePos(machinePos);
            if (manager.getNetworkAtPos(absoluteMachinePos).isEmpty()) {
                helper.fail("Mock player use did not create a ticking machine network", machinePos);
            }
            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 40)
    public static void testNetworkConnectivity(GameTestHelper helper) {
        var machinePos = new BlockPos(1, 1, 1);
        var cablePos = machinePos.east();
        var subnetPos = cablePos.east();

        var cableState = componentBlock("cable").defaultBlockState()
            .setValue(CableBlock.EAST, true)
            .setValue(CableBlock.WEST, true);
        var subnetState = componentBlock("electric_buffer").defaultBlockState()
            .setValue(MachineBlock.IO_FACING, Direction.WEST);

        helper.setBlock(machinePos, machineState(Direction.EAST));
        helper.setBlock(cablePos, cableState);
        helper.setBlock(subnetPos, subnetState);
        useWithTeamMockPlayer(helper, machinePos);

        helper.runAfterDelay(12, () -> {
            var manager = WorldNetworkManagers.get(helper.getLevel());
            var absoluteMachinePos = helper.absolutePos(machinePos);
            var absoluteCablePos = helper.absolutePos(cablePos);
            var absoluteSubnetPos = helper.absolutePos(subnetPos);
            var maybeGraph = manager.getNetworkAtPos(absoluteMachinePos);
            if (maybeGraph.isEmpty()) {
                helper.fail("Machine was not discovered by the network", machinePos);
            }
            var graph = maybeGraph.orElseThrow();
            if (manager.getNetworkAtPos(absoluteCablePos).orElse(null) != graph) {
                helper.fail("Cable was not connected to the machine network", cablePos);
            }
            if (manager.getNetworkAtPos(absoluteSubnetPos).orElse(null) != graph) {
                helper.fail("Electric buffer was not connected to the machine network", subnetPos);
            }
            var network = MACHINE.tryGet(helper.getBlockEntity(machinePos))
                .flatMap(machine -> machine.network())
                .orElseThrow();
            if (!network.getSubnet(absoluteCablePos).equals(absoluteMachinePos)) {
                helper.fail("Cable did not inherit the machine subnet", cablePos);
            }
            if (!network.getSubnet(absoluteSubnetPos).equals(absoluteSubnetPos)) {
                helper.fail("Electric buffer was not recognized as a subnet", subnetPos);
            }
            helper.succeed();
        });
    }

    private static BlockState machineState(Direction ioFacing) {
        return AllBlockEntities.getMachine("electric_furnace")
            .block(Voltage.LV)
            .defaultBlockState()
            .setValue(MachineBlock.IO_FACING, ioFacing);
    }

    private static Block componentBlock(String name) {
        return (Block) AllItems.getComponent(name).get(Voltage.LV).get();
    }

    private static void useWithTeamMockPlayer(GameTestHelper helper, BlockPos pos) {
        var player = helper.makeMockPlayer();
        var scoreboard = helper.getLevel().getScoreboard();
        var team = scoreboard.getPlayerTeam(TEAM_NAME);
        assert team != null;
        scoreboard.addPlayerToTeam(player.getScoreboardName(), team);

        var absolutePos = helper.absolutePos(pos);
        var state = helper.getLevel().getBlockState(absolutePos);
        state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(
            Vec3.atCenterOf(absolutePos), Direction.NORTH, absolutePos, true));
    }
}
