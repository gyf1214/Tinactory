package org.shsts.tinactory.content.network;

import com.google.common.collect.ImmutableMap;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.material.MaterialSet;
import org.shsts.tinactory.core.network.IConnector;
import org.shsts.tinactory.core.network.NetworkManager;
import org.shsts.tinactory.core.tool.IWrenchable;
import org.shsts.tinactory.core.tool.UsableToolItem;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    public static final int SMALL_WIRE_RADIUS = 1;
    public static final int WIRE_RADIUS = 2;
    public static final int PIPE_RADIUS = 4;
    public static final int INSULATION_COLOR = 0xFF36302E;

    private final int radius;
    public final Voltage voltage;
    private final double resistance;
    public final MaterialSet material;
    private final Map<BlockState, VoxelShape> shapes;

    public CableBlock(Properties properties, int radius, Voltage voltage, double resistance, MaterialSet mat) {
        super(properties.requiresCorrectToolForDrops());
        this.radius = radius;
        this.voltage = voltage;
        this.resistance = resistance;
        this.shapes = makeShapes();
        this.material = mat;

        registerDefaultState(stateDefinition.any()
            .setValue(NORTH, false)
            .setValue(EAST, false)
            .setValue(SOUTH, false)
            .setValue(WEST, false)
            .setValue(UP, false)
            .setValue(DOWN, false));
    }

    public static Function<Properties, CableBlock> cable(Voltage voltage, double resistance,
        MaterialSet mat, boolean bare) {
        var radius = bare ? WIRE_RADIUS : RADIUS;
        return prop -> new CableBlock(prop, radius, voltage, resistance, mat);
    }

    public static <U extends Block, P> Transformer<IBlockBuilder<U, P>> tint(
        int color, boolean bare) {
        if (bare) {
            return $ -> $.tint(color);
        } else {
            return $ -> $.tint(CableBlock.INSULATION_COLOR, color);
        }
    }

    private Map<BlockState, VoxelShape> makeShapes() {
        double st = 8d - (double) radius;
        double ed = 8d + (double) radius;

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

    private VoxelShape getRealShape(BlockState state) {
        return shapes.get(state);
    }

    private boolean shouldRenderOverlay(CollisionContext ctx) {
        return ctx instanceof EntityCollisionContext collision &&
            collision.getEntity() instanceof LocalPlayer player &&
            player.getMainHandItem().getItem() instanceof UsableToolItem &&
            canWrenchWith(player.getMainHandItem());
    }

    private VoxelShape getRenderShape(BlockState state, CollisionContext ctx) {
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

    private BlockState setConnectedState(Level world, BlockPos pos, BlockState state,
        Direction dir, boolean connected) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        if (state.getValue(property) == connected) {
            return state;
        }
        var newState = state.setValue(property, connected);
        NetworkManager.tryGet(world).ifPresent(manager -> manager.invalidatePosDir(pos, dir));
        return newState;
    }

    private void setConnected(Level world, BlockPos pos, BlockState state,
        Direction dir, boolean connected) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        if (state.getValue(property) == connected) {
            return;
        }
        var newState = state.setValue(property, connected);
        world.setBlockAndUpdate(pos, newState);

        NetworkManager.tryGet(world).ifPresent(manager -> manager.invalidatePosDir(pos, dir));
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool,
        Direction dir, boolean sneaky) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        if (state.getValue(property)) {
            setConnected(world, pos, state, dir, false);
        } else if (allowConnectWith(world, pos, state, dir) &&
            IConnector.allowConnectWith(world, pos.relative(dir), dir.getOpposite())) {
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
    public boolean allowConnectWith(Level world, BlockPos pos, BlockState state,
        Direction dir, BlockState state1) {
        var block1 = state1.getBlock();
        return block1 instanceof IConnector &&
            (!(block1 instanceof CableBlock cableBlock) || cableBlock.voltage == voltage);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var world = ctx.getLevel();
        var pos = ctx.getClickedPos();
        var state = defaultBlockState();
        var dir = ctx.getClickedFace().getOpposite();
        var pos1 = pos.relative(dir);
        var dir1 = ctx.getClickedFace();
        if (autoConnectWith(world, pos, state, dir) &&
            IConnector.autoConnectWith(world, pos1, dir1, state)) {
            return state.setValue(PROPERTY_BY_DIRECTION.get(dir), true);
        }
        return state;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state,
        @Nullable LivingEntity placer, ItemStack stack) {
        NetworkManager.tryGet(world).ifPresent(manager -> {
            manager.invalidatePos(pos);
            for (var dir : Direction.values()) {
                if (isConnected(world, pos, state, dir)) {
                    manager.invalidatePos(pos.relative(dir));
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState state1,
        LevelAccessor levelAccessor, BlockPos pos, BlockPos pos1) {
        var world = (Level) levelAccessor;
        var dir1 = dir.getOpposite();

        var old = isConnected(world, pos, state, dir);
        if (!old && autoConnectWith(world, pos, state, dir, state1) &&
            IConnector.isConnectedInWorld(world, pos1, state1, dir1)) {
            return setConnectedState(world, pos, state, dir, true);
        } else if (old && (!allowConnectWith(world, pos, state, dir, state1) ||
            !IConnector.isConnectedInWorld(world, pos1, state1, dir1))) {
            return setConnectedState(world, pos, state, dir, false);
        }
        return state;
    }

    private void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGet(world).ifPresent(manager -> {
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
