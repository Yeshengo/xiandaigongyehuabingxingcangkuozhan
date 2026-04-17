package com.yeshengo.bingxing;

import com.yeshengo.bingxing.init.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.minecraft.resources.ResourceLocation;
import net.swedz.tesseract.neoforge.compat.mi.TesseractMI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BingXing.MODID)
public class BingXing {
    public static final String MODID = "bingxing";
    public static final Logger LOGGER = LogManager.getLogger();

    public BingXing(IEventBus modEventBus) {
        TesseractMI.init(BingXing.MODID);

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);

        LOGGER.info("Bingxing Parallel Hatches loaded!");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}