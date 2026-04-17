package com.yeshengo.bingxing.mixin;

import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.blockentities.multiblocks.AbstractCraftingMultiblockBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.recipe.MachineRecipe;
import aztech.modern_industrialization.machines.recipe.condition.MachineProcessCondition;
import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

@Mixin(value = CrafterComponent.class, remap = false)
public abstract class CrafterComponentMixin {

    @Shadow private long usedEnergy;
    @Shadow private long recipeEnergy;
    @Shadow private RecipeHolder<MachineRecipe> activeRecipe;
    @Shadow private MachineProcessCondition.Context conditionContext;

    /**
     * 在配方完成时，额外产出 N-1 份产品（同时消耗 N-1 份原料）
     */
    @Inject(method = "tickRecipe", at = @At(value = "INVOKE", target = "Laztech/modern_industrialization/machines/components/CrafterComponent;clearLocks()V", shift = At.Shift.BEFORE))
    private void onRecipeFinished(CallbackInfoReturnable<Boolean> cir) {
        if (activeRecipe == null || usedEnergy != recipeEnergy) return;

        try {
            MachineBlockEntity machine = getMachineFromContext();
            if (machine instanceof AbstractCraftingMultiblockBlockEntity multiblock) {
                int parallelLevel = getTotalParallelLevel(multiblock); // 最多返回 1 个并行仓的等级
                if (parallelLevel <= 1) return;

                MachineRecipe recipe = activeRecipe.value();
                BingXing.LOGGER.info("Parallel processing: {} batches", parallelLevel);

                Method takeItemInputs = CrafterComponent.class.getDeclaredMethod("takeItemInputs", MachineRecipe.class, boolean.class);
                Method takeFluidInputs = CrafterComponent.class.getDeclaredMethod("takeFluidInputs", MachineRecipe.class, boolean.class);
                Method putItemOutputs = CrafterComponent.class.getDeclaredMethod("putItemOutputs", MachineRecipe.class, boolean.class, boolean.class);
                Method putFluidOutputs = CrafterComponent.class.getDeclaredMethod("putFluidOutputs", MachineRecipe.class, boolean.class, boolean.class);
                takeItemInputs.setAccessible(true);
                takeFluidInputs.setAccessible(true);
                putItemOutputs.setAccessible(true);
                putFluidOutputs.setAccessible(true);

                for (int i = 1; i < parallelLevel; i++) {
                    boolean itemsOk = (boolean) takeItemInputs.invoke(this, recipe, false);
                    boolean fluidsOk = (boolean) takeFluidInputs.invoke(this, recipe, false);
                    if (!itemsOk || !fluidsOk) {
                        BingXing.LOGGER.warn("Parallel batch {} failed: itemsOk={}, fluidsOk={}", i, itemsOk, fluidsOk);
                        break;
                    }
                    putItemOutputs.invoke(this, recipe, false, false);
                    putFluidOutputs.invoke(this, recipe, false, false);
                    BingXing.LOGGER.debug("Parallel batch {} completed", i);
                }
            }
        } catch (Exception e) {
            BingXing.LOGGER.error("Failed to process parallel batches", e);
        }
    }

    private MachineBlockEntity getMachineFromContext() {
        try {
            Method getBlockEntity = conditionContext.getClass().getMethod("getBlockEntity");
            return (MachineBlockEntity) getBlockEntity.invoke(conditionContext);
        } catch (Exception e) {
            BingXing.LOGGER.error("Failed to get MachineBlockEntity from conditionContext", e);
            return null;
        }
    }

    /**
     * 限制：只取第一个并行仓的等级，忽略其他并行仓（实现一台机器只能有一个并行仓）
     */
    private int getTotalParallelLevel(AbstractCraftingMultiblockBlockEntity machine) {
        try {
            Field shapeMatcherField = MultiblockMachineBlockEntity.class.getDeclaredField("shapeMatcher");
            shapeMatcherField.setAccessible(true);
            ShapeMatcher shapeMatcher = (ShapeMatcher) shapeMatcherField.get(machine);
            if (shapeMatcher == null) return 1;

            Field simpleMembersField = ShapeMatcher.class.getDeclaredField("simpleMembers");
            simpleMembersField.setAccessible(true);
            Map<BlockPos, ?> simpleMembers = (Map<BlockPos, ?>) simpleMembersField.get(shapeMatcher);
            if (simpleMembers == null) return 1;

            Level level = machine.getLevel();
            for (BlockPos pos : simpleMembers.keySet()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof ParallelHatchBlockEntity hatch) {
                    return hatch.getParallelLevel(); // 只取第一个，忽略其他
                }
            }
            return 1;
        } catch (Exception e) {
            BingXing.LOGGER.warn("Failed to get parallel level", e);
            return 1;
        }
    }
}