package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BingXing.MODID);

    public static final Supplier<BlockEntityType<ParallelHatchBlockEntity>> PARALLEL_HATCH =
            BLOCK_ENTITIES.register("parallel_hatch",
                    () -> BlockEntityType.Builder.of(ParallelHatchBlockEntity::new,
                            ModBlocks.BASIC_PARALLEL_HATCH.get(),
                            ModBlocks.ADVANCED_PARALLEL_HATCH.get(),
                            ModBlocks.ELITE_PARALLEL_HATCH.get(),
                            ModBlocks.ULTIMATE_PARALLEL_HATCH.get(),
                            ModBlocks.CREATIVE_PARALLEL_HATCH.get()
                    ).build(null));
}