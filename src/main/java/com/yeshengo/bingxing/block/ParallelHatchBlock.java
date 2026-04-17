package com.yeshengo.bingxing.block;

import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import com.mojang.serialization.MapCodec;
import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import com.yeshengo.bingxing.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ParallelHatchBlock extends BaseEntityBlock {

    public ParallelHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(ParallelHatchBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ParallelHatchBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.PARALLEL_HATCH.get(),
                (l, p, s, be) -> ((ParallelHatchBlockEntity) be).tick());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ParallelHatchBlockEntity hatch) {
                serverPlayer.openMenu(hatch, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player, InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ParallelHatchBlockEntity hatch) {
                serverPlayer.openMenu(hatch, pos);
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    public static int getColor(BlockState state, @Nullable BlockAndTintGetter world, BlockPos pos, int tintIndex) {
        if (world instanceof Level level) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ParallelHatchBlockEntity hatch) {
                ResourceLocation casingId = hatch.getCasingId();
                if (casingId != null) {
                    String path = casingId.getPath();
                    // 根据 MI 常见外壳名称映射颜色
                    if (path.contains("bronze") || path.contains("bricked_bronze")) return 0xB87333; // 铜色
                    if (path.contains("steel") || path.contains("bricked_steel")) return 0x808080;   // 钢色
                    if (path.contains("stainless") || path.contains("clean")) return 0xC0C0C0;      // 不锈钢
                    if (path.contains("titanium")) return 0xBFBFBF;                                  // 钛
                    if (path.contains("iridium")) return 0xE0E0E0;                                   // 铱
                    if (path.contains("heatproof")) return 0x8B4513;                                 // 耐热
                    if (path.contains("frostproof")) return 0xADD8E6;                                // 耐寒
                    if (path.contains("nuclear")) return 0x556B2F;                                   // 核
                    // 可根据需要继续添加
                }
            }
        }
        return 0xFFFFFF; // 默认白色
    }
}