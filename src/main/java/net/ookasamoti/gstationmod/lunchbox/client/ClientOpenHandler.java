package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.InputEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.net.OpenLunchBoxC2S;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class ClientOpenHandler {
    public static void onMouse(InputEvent.MouseButton.Pre e) {
        if (e.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT || e.getAction() != GLFW.GLFW_PRESS) return;
        if (!Screen.hasControlDown()) return;

        var mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        var main = mc.player.getMainHandItem();
        var off  = mc.player.getOffhandItem();
        if (!(main.getItem() instanceof LunchBoxItem || off.getItem() instanceof LunchBoxItem)) return;

        PacketDistributor.sendToServer(new OpenLunchBoxC2S());
        e.setCanceled(true);
    }

    private ClientOpenHandler() {}
}
