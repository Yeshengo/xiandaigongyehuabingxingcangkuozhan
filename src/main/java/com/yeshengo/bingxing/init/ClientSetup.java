package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.block.ParallelHatchBlock;
import com.yeshengo.bingxing.gui.ParallelHatchScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = BingXing.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, world, pos, tintIndex) -> ParallelHatchBlock.getColor(state, world, pos, tintIndex),
                ModBlocks.BASIC_PARALLEL_HATCH.get(),
                ModBlocks.ADVANCED_PARALLEL_HATCH.get(),
                ModBlocks.ELITE_PARALLEL_HATCH.get(),
                ModBlocks.ULTIMATE_PARALLEL_HATCH.get(),
                ModBlocks.CREATIVE_PARALLEL_HATCH.get()
        );
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.PARALLEL_HATCH.get(), ParallelHatchScreen::new);
    }
}