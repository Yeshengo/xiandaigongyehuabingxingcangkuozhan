package com.yeshengo.bingxing.network;

import com.yeshengo.bingxing.BingXing;
import com.yeshengo.bingxing.blockentity.ParallelHatchBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record UpdateParallelLevelPacket(BlockPos pos, int newLevel) implements CustomPacketPayload {

    public static final Type<UpdateParallelLevelPacket> TYPE = new Type<>(BingXing.id("update_parallel_level"));

    public static final StreamCodec<ByteBuf, UpdateParallelLevelPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateParallelLevelPacket::pos,
            ByteBufCodecs.VAR_INT, UpdateParallelLevelPacket::newLevel,
            UpdateParallelLevelPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(final UpdateParallelLevelPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                if (serverPlayer.level().getBlockEntity(packet.pos()) instanceof ParallelHatchBlockEntity hatch) {
                    hatch.setParallelLevel(packet.newLevel());
                }
            }
        });
    }
}