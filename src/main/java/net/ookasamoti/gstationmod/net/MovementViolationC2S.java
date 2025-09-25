package net.ookasamoti.gstationmod.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MovementViolationC2S(int limit) implements CustomPacketPayload {
    public static final Type<MovementViolationC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("gstationmod", "movement_violation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MovementViolationC2S> STREAM_CODEC =
            StreamCodec.of(
                    (buf, msg) -> buf.writeVarInt(msg.limit()),
                    buf -> new MovementViolationC2S(buf.readVarInt())
            );

    @Override public Type<MovementViolationC2S> type() { return TYPE; }
}
