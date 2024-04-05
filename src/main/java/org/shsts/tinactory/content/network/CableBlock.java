package org.shsts.tinactory.content.network;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.shsts.tinactory.api.electric.IElectricBlock;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.content.tool.UsableToolItem;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CableBlock extends Block implements IWrenchable, IConnector, IElectricBlock {
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;
    public static final int RADIUS = 3;

    public final Voltage voltage;
    public final double resistance;
    protected final Map<BlockState, VoxelShape> shapes;

    public CableBlock(Properties properties, Voltage voltage, double resistance) {
        super(properties.strength(2.0f).requiresCorrectToolForDrops());
        this.voltage = voltage;
        this.resistance = resistance;
        this.shapes = this.makeShapes();

        var defaultState = stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false);
        this.registerDefaultState(defaultState);
    }

    private Map<BlockState, VoxelShape> makeShapes() {
        double st = 8d - (double) RADIUS;
        double ed = 8d + (double) RADIUS;

        var baseShape = Block.box(st, st, st, ed, ed, ed);
        var dirShapes = new HashMap<Direction, VoxelShape>();
        for (var dir : Direction.values()) {
            var stepX = 8d + (double) dir.getStepX() * 8d;
            var stepY = 8d + (double) dir.getStepY() * 8d;
            var stepZ = 8d + (double) dir.getStepZ() * 8d;
            dirShapes.put(dir, Block.box(
                    Math.min(st, stepX), Math.min(st, stepY), Math.min(st, stepZ),
                    Math.max(ed, stepX), Math.max(ed, stepY), Math.max(ed, stepZ)));
        }

        var builder = ImmutableMap.<BlockState, VoxelShape>builder();
        for (var state : getStateDefinition().getPossibleStates()) {
            var shape = baseShape;
            for (var dir : Direction.values()) {
                if (state.getValue(PROPERTY_BY_DIRECTION.get(dir))) {
                    shape = Shapes.or(shape, dirShapes.get(dir));
                }
            }
            builder.put(state, shape);
        }

        return builder.build();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN);
    }

    protected VoxelShape getRealShape(BlockState state) {
        return shapes.get(state);
    }

    protected boolean shouldRenderOverlay(CollisionContext ctx) {
        return ctx instanceof EntityCollisionContext collision &&
                collision.getEntity() instanceof LocalPlayer player &&
                player.getMainHandItem().getItem() instanceof UsableToolItem &&
                canWrenchWith(player.getMainHandItem());
    }

    protected VoxelShape getRenderShape(BlockState state, CollisionContext ctx) {
        return shouldRenderOverlay(ctx) ? Shapes.block() : getRealShape(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return getRealShape(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return getRenderShape(state, ctx);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return getRealShape(state);
    }

    @Override
    public boolean canWrenchWith(ItemStack item) {
        return item.is(AllTags.TOOL_WIRE_CUTTER);
    }

    protected BlockState setConnected(Level world, BlockPos pos, BlockState state,
                                      Direction dir, boolean connected) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        if (state.getValue(property) == connected) {
            return state;
        }
        var newState = state.setValue(property, connected);
        world.setBlockAndUpdate(pos, newState);

        NetworkManager.tryGetInstance(world).ifPresent(manager -> manager.invalidatePosDir(pos, dir));
        return newState;
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
                             Direction dir, boolean sneaky) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        if (state.getValue(property)) {
            setConnected(world, pos, state, dir, false);
        } else if (IConnector.allowConnect(world, pos, state, dir)) {
            setConnected(world, pos, state, dir, true);
        }
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        return state.getValue(PROPERTY_BY_DIRECTION.get(dir));
    }

    @Override
    public long getVoltage(BlockState state) {
        return voltage.value;
    }

    @Override
    public double getResistance(BlockState state) {
        return resistance;
    }

    @Override
    public boolean allowConnectFrom(Level world, BlockPos pos, BlockState state,
                                    Direction dir, BlockState state1) {
        return state1.getBlock() instanceof IElectricBlock block1 &&
                (voltage == Voltage.PRIMITIVE || voltage.value == block1.getVoltage(state1));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var state = defaultBlockState();
        var dir = ctx.getClickedFace().getOpposite();
        if (IConnector.autoConnectOnPlace(ctx, state)) {
            return state.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
                            @Nullable LivingEntity placer, ItemStack stack) {
        NetworkManager.tryGetInstance(world).ifPresent(manager -> {
            for (var dir : Direction.values()) {
                if (isConnected(world, pos, state, dir)) {
                    manager.invalidatePosDir(pos, dir);
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState state1,
                                  LevelAccessor levelAccessor, BlockPos pos, BlockPos pos1) {
        var world = (Level) levelAccessor;
        var connected = IConnector.autoConnectFromNeighbor(world, pos1, state1, dir.getOpposite(), state);
        return setConnected(world, pos, state, dir, connected);
    }

    protected void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGetInstance(world).ifPresent(manager -> {
            manager.invalidatePos(pos);
            for (var entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (state.getValue(entry.getValue())) {
                    manager.invalidatePos(pos.relative(entry.getKey()));
                }
            }
        });
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
        onDestroy(world, pos, state);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
        onDestroy((Level) world, pos, state);
    }
}
