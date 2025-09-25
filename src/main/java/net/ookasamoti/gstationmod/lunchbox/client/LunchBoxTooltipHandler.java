package net.ookasamoti.gstationmod.lunchbox.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;

import java.util.List;

/**
 * 弁当箱のツールチップを「選択中の食料」に置き換える／横に並べて表示する。
 * - AppleSkin 等が GatherComponents で追加した情報も維持される。
 * - '置き換え'：Shift未押下（デフォルト）
 * - '横並び'：Shift押下（好みに応じてロジックを入れ替えてOK）
 */
@EventBusSubscriber(modid = "gstationmod", value = Dist.CLIENT)
public final class LunchBoxTooltipHandler {

    @SubscribeEvent
    public static void onTooltipPre(RenderTooltipEvent.Pre e) {
        ItemStack box = e.getItemStack();
        if (!(box.getItem() instanceof LunchBoxItem)) return;

        ItemStack sel = LunchBoxUtil.getSelectedFood(box);
        if (sel.isEmpty()) return;

        GuiGraphics gg = e.getGraphics();
        var font = e.getFont();

        if (!Screen.hasShiftDown()) {
            e.setCanceled(true);
            gg.renderTooltip(font, sel, e.getX(), e.getY());
            return;
        }

        int defaultWidth = tooltipWidth(e.getComponents(), font);

        int pad = 8;
        int xLeft  = e.getX() - (180 + pad);
        int xRight = e.getX() + defaultWidth + pad;
        int x2 = (xRight + 200 <= e.getScreenWidth()) ? xRight : Math.max(0, xLeft);

        gg.renderTooltip(font, sel, x2, e.getY());
    }

    private static int tooltipWidth(List<ClientTooltipComponent> comps, net.minecraft.client.gui.Font font) {
        int w = 0;
        for (ClientTooltipComponent c : comps) {
            w = Math.max(w, c.getWidth(font));
        }
        return w;
    }

    private LunchBoxTooltipHandler() {}
}
