package net.ookasamoti.gstationmod.lunchbox.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record LunchboxTierSyncS2C(int tier) implements CustomPacketPayload {
    public static final Type<LunchboxTierSyncS2C> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("gstationmod", "lunchbox_tier_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LunchboxTierSyncS2C> STREAM =
            StreamCodec.ofMember(LunchboxTierSyncS2C::write, LunchboxTierSyncS2C::read);

    private static LunchboxTierSyncS2C read(RegistryFriendlyByteBuf buf) {
        return new LunchboxTierSyncS2C(buf.readVarInt());
    }
    private void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.tier());
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
