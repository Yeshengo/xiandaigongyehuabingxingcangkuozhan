package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BingXing.MODID);

    public static final DeferredItem<BlockItem> BASIC_PARALLEL_HATCH = ITEMS.registerSimpleBlockItem(ModBlocks.BASIC_PARALLEL_HATCH);
    public static final DeferredItem<BlockItem> ADVANCED_PARALLEL_HATCH = ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_PARALLEL_HATCH);
    public static final DeferredItem<BlockItem> ELITE_PARALLEL_HATCH = ITEMS.registerSimpleBlockItem(ModBlocks.ELITE_PARALLEL_HATCH);
    public static final DeferredItem<BlockItem> ULTIMATE_PARALLEL_HATCH = ITEMS.registerSimpleBlockItem(ModBlocks.ULTIMATE_PARALLEL_HATCH);
    public static final DeferredItem<BlockItem> CREATIVE_PARALLEL_HATCH = ITEMS.registerSimpleBlockItem(ModBlocks.CREATIVE_PARALLEL_HATCH);
}