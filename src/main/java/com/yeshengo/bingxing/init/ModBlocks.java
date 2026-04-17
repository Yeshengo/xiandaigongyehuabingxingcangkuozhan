package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.block.ParallelHatchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(BingXing.MODID);

    public static final DeferredBlock<Block> BASIC_PARALLEL_HATCH = register("basic_parallel_hatch");
    public static final DeferredBlock<Block> ADVANCED_PARALLEL_HATCH = register("advanced_parallel_hatch");
    public static final DeferredBlock<Block> ELITE_PARALLEL_HATCH = register("elite_parallel_hatch");
    public static final DeferredBlock<Block> ULTIMATE_PARALLEL_HATCH = register("ultimate_parallel_hatch");
    public static final DeferredBlock<Block> CREATIVE_PARALLEL_HATCH = register("creative_parallel_hatch");

    private static DeferredBlock<Block> register(String name) {
        return BLOCKS.register(name, () -> new ParallelHatchBlock(BlockBehaviour.Properties.of().noOcclusion()));
    }
}