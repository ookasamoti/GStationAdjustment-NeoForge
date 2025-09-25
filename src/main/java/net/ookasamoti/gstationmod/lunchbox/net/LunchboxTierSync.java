package net.ookasamoti.gstationmod.lunchbox.net;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class LunchboxTierSync {

    public static final String PROTOCOL = "1";

    public static void register(RegisterPayloadHandlersEvent e) {
        e.registrar("gstationmod").versioned("1")
                .playToClient(LunchboxTierSyncS2C.TYPE, LunchboxTierSyncS2C.STREAM,
                        (msg, ctx) -> ctx.enqueueWork(() ->
                                net.ookasamoti.gstationmod.lunchbox.LunchboxUnlocks.setClientCache(msg.tier())
                        )
                );
    }

    public static void sendToAll(int tier) {
        for (ServerPlayer sp : spList()) {
            PacketDistributor.sendToPlayer(sp, new LunchboxTierSyncS2C(tier));
        }
    }

    public static void sendTo(ServerPlayer sp, int tier) {
        PacketDistributor.sendToPlayer(sp, new LunchboxTierSyncS2C(tier));
    }

    private static java.util.List<ServerPlayer> spList() {
        var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        return server.getPlayerList().getPlayers();
    }

    private LunchboxTierSync() {}
}
