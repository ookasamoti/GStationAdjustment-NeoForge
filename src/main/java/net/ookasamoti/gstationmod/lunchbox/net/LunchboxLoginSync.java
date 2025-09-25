package net.ookasamoti.gstationmod.lunchbox.net;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchboxUnlocks;

public final class LunchboxLoginSync {
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;
        int tier = LunchboxUnlocks.get(sp.serverLevel()).unlockedTier();
        LunchboxTierSync.sendTo(sp, tier);
    }
    private LunchboxLoginSync() {}
}