package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.net.CycleSlotC2S;

public final class ClientWheelHandler {
    public static void onScroll(InputEvent.MouseScrollingEvent e) {
        if (!Screen.hasControlDown()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean holding =
                mc.player.getMainHandItem().getItem() instanceof LunchBoxItem
                        || mc.player.getOffhandItem().getItem() instanceof LunchBoxItem;
        if (!holding) return;

        double dy = e.getScrollDeltaY();
        if (dy == 0.0) return;

        int delta = dy > 0 ? -1 : 1;
        PacketDistributor.sendToServer(new CycleSlotC2S(delta));
        e.setCanceled(true);
    }

    private ClientWheelHandler() {}
}
