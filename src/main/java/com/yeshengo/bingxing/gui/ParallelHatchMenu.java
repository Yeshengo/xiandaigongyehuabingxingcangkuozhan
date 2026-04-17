package com.yeshengo.bingxing.gui;

import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import com.yeshengo.bingxing.init.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ParallelHatchMenu extends AbstractContainerMenu {

    private final BlockPos pos;
    private final ContainerLevelAccess access;
    private int maxLevel = 8;
    private final DataSlot parallelLevelSlot = new DataSlot() {
        @Override
        public int get() {
            return getParallelLevelFromBE();
        }

        @Override
        public void set(int value) {
            setParallelLevelToBE(value);
        }
    };

    public ParallelHatchMenu(int containerId, Inventory playerInventory, BlockPos pos) {
        super(ModMenuTypes.PARALLEL_HATCH.get(), containerId);
        this.pos = pos;
        this.access = ContainerLevelAccess.create(playerInventory.player.level(), pos);

        // 初始化最大等级
        access.execute((level, blockPos) -> {
            BlockEntity be = level.getBlockEntity(blockPos);
            if (be instanceof ParallelHatchBlockEntity hatch) {
                maxLevel = hatch.getMaxParallel();
            }
        });

        addDataSlot(parallelLevelSlot);
    }

    private int getParallelLevelFromBE() {
        BlockEntity be = access.evaluate((level, blockPos) -> level.getBlockEntity(blockPos)).orElse(null);
        return be instanceof ParallelHatchBlockEntity hatch ? hatch.getParallelLevel() : 1;
    }

    private void setParallelLevelToBE(int level) {
        access.execute((levelAccess, blockPos) -> {
            BlockEntity be = levelAccess.getBlockEntity(blockPos);
            if (be instanceof ParallelHatchBlockEntity hatch) {
                hatch.setParallelLevel(level);
            }
        });
    }

    public int getParallelLevel() {
        return parallelLevelSlot.get();
    }

    public void setParallelLevel(int level) {
        parallelLevelSlot.set(level);
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean stillValid(Player player) {
        // 修正：基于当前菜单的位置进行距离检查
        return access.evaluate((level, pos) -> {
            // 检查方块是否仍然是并行仓（防止方块被破坏后菜单还开着）
            if (!(level.getBlockState(pos).getBlock() instanceof com.yeshengo.bingxing.block.ParallelHatchBlock)) {
                return false;
            }
            // 检查玩家距离
            return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
        }).orElse(false);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}