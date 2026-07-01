package net.assassinreport.betterenchanting.block.custom;

import com.mojang.serialization.MapCodec;
import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

public class NewChiseledBookshelfBlock extends BaseEntityBlock implements EntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 16, 16);
    public static final BooleanProperty SLOT_0_OCCUPIED = BooleanProperty.create("slot_0_occupied");
    public static final BooleanProperty SLOT_1_OCCUPIED = BooleanProperty.create("slot_1_occupied");
    public static final BooleanProperty SLOT_2_OCCUPIED = BooleanProperty.create("slot_2_occupied");
    public static final BooleanProperty SLOT_3_OCCUPIED = BooleanProperty.create("slot_3_occupied");
    public static final BooleanProperty SLOT_4_OCCUPIED = BooleanProperty.create("slot_4_occupied");
    public static final BooleanProperty SLOT_5_OCCUPIED = BooleanProperty.create("slot_5_occupied");

    public static final MapCodec<NewChiseledBookshelfBlock> CODEC = simpleCodec(NewChiseledBookshelfBlock::new);

    @NullMarked
    @Override
    protected MapCodec<? extends NewChiseledBookshelfBlock> codec() {
        return CODEC;
    }

    public NewChiseledBookshelfBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(SLOT_0_OCCUPIED, false)
                .setValue(SLOT_1_OCCUPIED, false)
                .setValue(SLOT_2_OCCUPIED, false)
                .setValue(SLOT_3_OCCUPIED, false)
                .setValue(SLOT_4_OCCUPIED, false)
                .setValue(SLOT_5_OCCUPIED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(SLOT_0_OCCUPIED, false)
                .setValue(SLOT_1_OCCUPIED, false)
                .setValue(SLOT_2_OCCUPIED, false)
                .setValue(SLOT_3_OCCUPIED, false)
                .setValue(SLOT_4_OCCUPIED, false)
                .setValue(SLOT_5_OCCUPIED, false);
    }

    @NullMarked
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SLOT_0_OCCUPIED);
        builder.add(SLOT_1_OCCUPIED);
        builder.add(SLOT_2_OCCUPIED);
        builder.add(SLOT_3_OCCUPIED);
        builder.add(SLOT_4_OCCUPIED);
        builder.add(SLOT_5_OCCUPIED);
    }

    @NullMarked
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @NullMarked
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NewChiseledBookshelfBlockEntity(pos, state);
    }

    @NullMarked
    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean movedByPiston) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NewChiseledBookshelfBlockEntity bookshelfEntity) {
            Containers.dropContents(world, pos, bookshelfEntity);
            world.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, movedByPiston);
    }

    @NullMarked
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world instanceof ServerLevel) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof MenuProvider factory) {
                player.openMenu(factory);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @NullMarked
    protected boolean hasAnalogOutputSignal(final BlockState state) {
        return true;
    }

    @NullMarked
    protected int getAnalogOutputSignal(final BlockState state, final Level level, final BlockPos pos, final Direction direction) {
        if (level.isClientSide()) {
            return 0;
        } else {
            BlockEntity var6 = level.getBlockEntity(pos);
            if (var6 instanceof NewChiseledBookshelfBlockEntity) {
                NewChiseledBookshelfBlockEntity blockEntity = (NewChiseledBookshelfBlockEntity)var6;
                return blockEntity.getLastInteractedSlot() + 1;
            } else {
                return 0;
            }
        }
    }
}