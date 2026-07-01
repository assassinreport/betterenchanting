package net.assassinreport.betterenchanting.block.custom;

import com.mojang.serialization.MapCodec;
import net.assassinreport.betterenchanting.block.entity.ModBlockEntities;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

public class NewEnchantingBlock extends BaseEntityBlock implements EntityBlock {

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public NewEnchantingBlock(Properties properties) {
        super(properties);
    }

    public static final MapCodec<NewEnchantingBlock> CODEC = simpleCodec(NewEnchantingBlock::new);

    @NullMarked
    @Override
    protected MapCodec<? extends NewEnchantingBlock> codec() {
        return CODEC;
    }

    @NullMarked
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return SHAPE;
    }

    @NullMarked
    @Override
    public RenderShape getRenderShape(BlockState state) {
            return RenderShape.MODEL;
    }

    @NullMarked
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NewEnchantingTableBlockEntity(pos, state);
    }

    @NullMarked
    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof NewEnchantingTableBlockEntity) {
            Containers.dropContents(world, pos, (NewEnchantingTableBlockEntity) blockEntity);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull Level world,
            @NotNull BlockState state,
            @NotNull BlockEntityType<T> type) {

        return createTickerHelper(type, ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY,
                NewEnchantingTableBlockEntity::tick);
    }
}