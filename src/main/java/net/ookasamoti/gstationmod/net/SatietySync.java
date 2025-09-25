package net.ookasamoti.gstationmod.net;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ookasamoti.gstationmod.GStationMod;

import java.util.HashMap;
import java.util.Map;

public final class SatietySync {

    public record SatietyMapS2C(Map<String, Integer> map) implements CustomPacketPayload {
        public static final Type<SatietyMapS2C> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(GStationMod.MODID, "satiety_map"));

        public static final StreamCodec<FriendlyByteBuf, SatietyMapS2C> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.VAR_INT),
                        SatietyMapS2C::map,
                        SatietyMapS2C::new
                );

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent e) {
        var reg = e.registrar(GStationMod.MODID).versioned("1");
        reg.playToClient(SatietyMapS2C.TYPE, SatietyMapS2C.STREAM_CODEC, SatietySync::handleClient);
    }

    private static void handleClient(SatietyMapS2C msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;

            CompoundTag root = mc.player.getPersistentData().getCompound("gstation_nutrition").copy();
            CompoundTag sat = new CompoundTag();
            for (var e : msg.map().entrySet()) {
                sat.putInt(e.getKey(), e.getValue());
            }
            root.put("satiety", sat);
            mc.player.getPersistentData().put("gstation_nutrition", root);
        });
    }

    public static void syncSatiety(ServerPlayer sp) {
        CompoundTag root = sp.getPersistentData().getCompound("gstation_nutrition");
        CompoundTag sat = root.getCompound("satiety");
        Map<String, Integer> map = new HashMap<>();
        for (String k : sat.getAllKeys()) {
            map.put(k, sat.getInt(k));
        }

        PacketDistributor.sendToPlayer(sp, new SatietyMapS2C(map));
    }

    private SatietySync() {}
}
