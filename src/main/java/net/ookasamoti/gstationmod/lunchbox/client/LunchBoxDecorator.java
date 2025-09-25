package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;
import net.ookasamoti.gstationmod.registry.ModItems;

@EventBusSubscriber(value = Dist.CLIENT, modid = "gstationmod")
public final class LunchBoxDecorator {

    private static final ResourceLocation BADGE =
            ResourceLocation.fromNamespaceAndPath("gstationmod","textures/gui/lunch_badge.png");

    @SubscribeEvent
    public static void onDecorators(RegisterItemDecorationsEvent e) {
        Item[] boxes = {
                ModItems.LUNCH_BOX_T1.get(),
                ModItems.LUNCH_BOX_T2.get(),
                ModItems.LUNCH_BOX_T3.get(),
                ModItems.LUNCH_BOX_T4.get()
        };

        for (Item lunchBox : boxes) {
            e.register(lunchBox, (GuiGraphics gg, Font font, ItemStack stack, int x, int y) -> {
                if (Minecraft.getInstance().screen != null) return false;

                ItemStack sel = LunchBoxUtil.getSelectedFood(stack);
                if (sel.isEmpty()) return false;

                gg.renderItemDecorations(font, sel, x, y);

                gg.pose().pushPose();
                gg.pose().translate(0, 0, 400);
                int w = 6, h = 6, over = 2;
                gg.blit(BADGE, x - over, y - over, 0, 0, w, h, 16, 16);
                gg.pose().popPose();

                return true;
            });
        }
    }

    private LunchBoxDecorator() {}
}
