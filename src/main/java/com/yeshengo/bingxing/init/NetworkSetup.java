package com.yeshengo.bingxing.init;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.network.UpdateParallelLevelPacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = BingXing.MODID)
public class NetworkSetup {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(BingXing.MODID);
        registrar.playToServer(UpdateParallelLevelPacket.TYPE, UpdateParallelLevelPacket.STREAM_CODEC, UpdateParallelLevelPacket::handleServer);
    }
}