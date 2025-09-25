package net.ookasamoti.gstationmod.common;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ookasamoti.gstationmod.config.ClientConfig;
import net.ookasamoti.gstationmod.net.MovementViolationC2S;

public final class ClientViolationReporter {
    private static boolean outsideReported = false;
    private static long lastSentMs = 0;

    public static void onLoggedIn(ClientPlayerNetworkEvent.LoggingIn e) {
        outsideReported = false;
        lastSentMs = 0;
    }

    public static void onClientTick(ClientTickEvent.Post e) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        // クライアント設定OFFなら無効
        if (!ClientConfig.REPORT_MOVEMENT_VIOLATION.get()) return;

        // オーバーワールドのみ
        if (mc.level.dimension() != Level.OVERWORLD) {
            outsideReported = false;
            return;
        }

        int limit = ClientConfig.CLIENT_LIMIT_DISTANCE.get();
        double x = mc.player.getX(), z = mc.player.getZ();
        boolean outside = Math.abs(x) > limit || Math.abs(z) > limit;

        // 境界内に戻ったら再送を許可
        if (!outside) { outsideReported = false; return; }

        // 既に報告済みなら送らない
        if (outsideReported) return;

        // 連打防止
        long now = System.currentTimeMillis();
        if (now - lastSentMs < 2000) return;

        mc.getConnection().send(new MovementViolationC2S(limit));
        lastSentMs = now;
        outsideReported = true;
    }

    private ClientViolationReporter() {}
}
