package com.yeshengo.bingxing.mixin;

import aztech.modern_industrialization.machines.multiblocks.MultiblockMachineBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.ShapeMatcher;
import aztech.modern_industrialization.machines.multiblocks.SimpleMember;
import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;
import java.util.Map;

@Mixin(value = ShapeMatcher.class, remap = false)
public abstract class ShapeMatcherMixin {

    @Shadow
    private Map<BlockPos, SimpleMember> simpleMembers;

    @Inject(method = "matches", at = @At("HEAD"), cancellable = true)
    private void onMatches(BlockPos pos, Level world, CallbackInfoReturnable<Boolean> cir) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ParallelHatchBlockEntity hatch)) return;

        BingXing.LOGGER.info("ShapeMatcherMixin: found parallel hatch at {}", pos);

        if (simpleMembers.containsKey(pos)) {
            BingXing.LOGGER.info("ShapeMatcherMixin: hatch already in members, ensuring controller");
            // 直接调用 hatch 的查找控制器方法
            hatch.findAndSetControllerNow(world);
            cir.setReturnValue(true);
            return;
        }

        // 限制一台多方块只能有一个并行仓
        for (BlockPos memberPos : simpleMembers.keySet()) {
            if (world.getBlockEntity(memberPos) instanceof ParallelHatchBlockEntity) {
                BingXing.LOGGER.info("ShapeMatcherMixin: another parallel hatch already present, rejecting");
                cir.setReturnValue(false);
                return;
            }
        }

        // 添加为简单成员
        SimpleMember member = SimpleMember.forBlock(() -> be.getBlockState().getBlock());
        simpleMembers.put(pos, member);
        BingXing.LOGGER.info("ShapeMatcherMixin: added hatch to members at {}", pos);

        // 立即查找并设置控制器
        hatch.findAndSetControllerNow(world);

        cir.setReturnValue(true);
    }
}