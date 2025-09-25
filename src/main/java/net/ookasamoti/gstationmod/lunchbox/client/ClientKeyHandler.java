package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.net.OpenLunchBoxC2S;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ClientKeyHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post e) {
        var km = ClientKeybinds.OPEN_LUNCH_BOX;
        if (km == null) return;
        if (!km.consumeClick()) return;

        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        ItemStack main = mc.player.getMainHandItem();
        ItemStack off  = mc.player.getOffhandItem();
        boolean holding = main.getItem() instanceof LunchBoxItem || off.getItem() instanceof LunchBoxItem;
        if (!holding) return;

        PacketDistributor.sendToServer(new OpenLunchBoxC2S());
    }

    private ClientKeyHandler() {}
}
