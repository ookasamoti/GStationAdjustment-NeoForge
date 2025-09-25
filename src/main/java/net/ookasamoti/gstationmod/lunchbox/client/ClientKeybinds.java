package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public final class ClientKeybinds {
    public static KeyMapping OPEN_LUNCH_BOX;

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent e) {

        OPEN_LUNCH_BOX = new KeyMapping(
                "key.gstationmod.open_lunch_box",
                GLFW.GLFW_KEY_B,
                "key.categories.gstationmod"
        );
        e.register(OPEN_LUNCH_BOX);
    }

    private ClientKeybinds() {}
}
