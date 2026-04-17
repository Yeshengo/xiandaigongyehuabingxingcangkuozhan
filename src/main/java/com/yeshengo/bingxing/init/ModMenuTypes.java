package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.gui.ParallelHatchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, BingXing.MODID);

    public static final Supplier<MenuType<ParallelHatchMenu>> PARALLEL_HATCH =
            MENU_TYPES.register("parallel_hatch",
                    () -> IMenuTypeExtension.create((windowId, inv, data) ->
                            new ParallelHatchMenu(windowId, inv, data.readBlockPos())));
}