package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BingXing.MODID);

    public static final Supplier<CreativeModeTab> BINGXING_TAB = TABS.register("bingxing",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.bingxing"))
                    .icon(() -> ModItems.BASIC_PARALLEL_HATCH.get().getDefaultInstance())
                    .displayItems((params, output) -> {
                        output.accept(ModItems.BASIC_PARALLEL_HATCH.get());
                        output.accept(ModItems.ADVANCED_PARALLEL_HATCH.get());
                        output.accept(ModItems.ELITE_PARALLEL_HATCH.get());
                        output.accept(ModItems.ULTIMATE_PARALLEL_HATCH.get());
                        output.accept(ModItems.CREATIVE_PARALLEL_HATCH.get());
                    })
                    .build());
}