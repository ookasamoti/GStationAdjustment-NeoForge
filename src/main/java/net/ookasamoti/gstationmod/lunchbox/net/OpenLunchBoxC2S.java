package net.ookasamoti.gstationmod.lunchbox.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ookasamoti.gstationmod.GStationMod;
import net.ookasamoti.gstationmod.lunchbox.menu.LunchBoxMenu;
import net.minecraft.world.SimpleMenuProvider;

public record OpenLunchBoxC2S() implements CustomPacketPayload {
    public static final Type<OpenLunchBoxC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(GStationMod.MODID, "open_lunch_box"));

    public static final StreamCodec<FriendlyByteBuf, OpenLunchBoxC2S> STREAM_CODEC =
            StreamCodec.unit(new OpenLunchBoxC2S());

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenLunchBoxC2S msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp) {
                sp.openMenu(
                        new SimpleMenuProvider(
                                (id, inv, player) -> new LunchBoxMenu(id, inv),
                                Component.translatable("item.gstationmod.lunch_box")
                        )
                );
            }
        });
    }
}
