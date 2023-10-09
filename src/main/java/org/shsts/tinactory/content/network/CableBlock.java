package org.shsts.tinactory.content.network;

import com.google.common.collect.ImmutableMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
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
import org.shsts.tinactory.content.tool.IWrenchable;
import org.shsts.tinactory.content.tool.WrenchItem;
import org.shsts.tinactory.network.IConnector;
import org.shsts.tinactory.network.NetworkManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CableBlock extends Block implements IWrenchable, IConnector {
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;
    public static final BooleanProperty UP = PipeBlock.UP;
    public static final BooleanProperty DOWN = PipeBlock.DOWN;

    protected final Map<BlockState, VoxelShape> shapes;

    public final CableSetting setting;

    private CableBlock(Properties properties, CableSetting setting) {
        super(properties);
        this.setting = setting;

        this.shapes = this.makeShapes(setting.radius);

        var defaultState = this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(UP, false)
                .setValue(DOWN, false);
        this.registerDefaultState(defaultState);
    }

    public static Function<Properties, CableBlock> factory(CableSetting setting) {
        return $ -> new CableBlock($, setting);
    }

    private Map<BlockState, VoxelShape> makeShapes(int radius) {
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
        for (var state : this.getStateDefinition().getPossibleStates()) {
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
        return this.shapes.get(state);
    }

    protected boolean shouldRenderOverlay(CollisionContext ctx) {
        return ctx instanceof EntityCollisionContext collision &&
                collision.getEntity() instanceof LocalPlayer player &&
                player.getMainHandItem().getItem() instanceof WrenchItem &&
                this.canWrenchWith(player.getMainHandItem());
    }

    protected VoxelShape getRenderShape(BlockState state, CollisionContext ctx) {
        return this.shouldRenderOverlay(ctx) ? Shapes.block() : this.getRealShape(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return this.getRealShape(state);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
        return this.getRenderShape(state, ctx);
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter world, BlockPos pos) {
        return this.getRealShape(state);
    }

    @Override
    public boolean canWrenchWith(ItemStack item) {
        return true;
    }

    protected void setConnected(Level world, BlockPos pos, BlockState state, Direction dir, boolean connected) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        var newState = state.setValue(property, connected);
        world.setBlockAndUpdate(pos, newState);

        NetworkManager.tryGetInstance(world).ifPresent(manager -> manager.invalidatePosDir(pos, dir));
    }

    @Override
    public void onWrenchWith(Level world, BlockPos pos, BlockState state, ItemStack tool, Direction dir, boolean sneaky) {
        var property = PROPERTY_BY_DIRECTION.get(dir);
        this.setConnected(world, pos, state, dir, !state.getValue(property));
    }

    @Override
    public boolean isConnected(Level world, BlockPos pos, BlockState state, Direction dir) {
        return state.getValue(PROPERTY_BY_DIRECTION.get(dir));
    }

    protected void onDestroy(Level world, BlockPos pos, BlockState state) {
        NetworkManager.tryGetInstance(world).ifPresent(manager -> {
            manager.invalidatePos(pos);
            for (var entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (state.getValue(entry.getValue())) {
                    manager.invalidatePosDir(pos, entry.getKey());
                }
            }
        });
    }

    @Override
    public void onBlockExploded(BlockState state, Level world, BlockPos pos, Explosion explosion) {
        super.onBlockExploded(state, world, pos, explosion);
        this.onDestroy(world, pos, state);
    }

    @Override
    public void destroy(LevelAccessor world, BlockPos pos, BlockState state) {
        super.destroy(world, pos, state);
        this.onDestroy((Level) world, pos, state);
    }
}
