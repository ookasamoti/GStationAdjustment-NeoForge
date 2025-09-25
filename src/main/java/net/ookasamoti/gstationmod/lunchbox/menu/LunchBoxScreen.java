package net.ookasamoti.gstationmod.lunchbox.menu;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem.Size;

public class LunchBoxScreen extends AbstractContainerScreen<LunchBoxMenu> {
    private ResourceLocation BG;

    public LunchBoxScreen(LunchBoxMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);

        this.imageWidth = 176;

        Size s = menu.getBoxSize();
        switch (s) {
            case T1_5 -> {
                this.imageHeight = 133;
                BG = ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/hopper.png");
            }
            case T2_9 -> {
                this.imageHeight = 166;
                BG = ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/dispenser.png");
            }
            case T3_27 -> {
                this.imageHeight = 166;
                BG = ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/shulker_box.png");
            }
            case T4_54 -> {
                this.imageHeight = 222;
                BG = ResourceLocation.fromNamespaceAndPath("minecraft","textures/gui/container/generic_54.png");
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics gg, float partial, int mx, int my) {
        gg.blit(BG, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics gg, int mx, int my) {
        int titleY, invTitleY;
        switch (this.menu.getBoxSize()) {
            case T1_5 -> { titleY = 6; invTitleY = 40; }
            case T2_9 -> { titleY = 6; invTitleY = 73; }
            case T3_27 -> { titleY = 6; invTitleY = 72; }
            case T4_54 -> { titleY = 6; invTitleY = 128; }
            default -> { titleY = 6; invTitleY = this.imageHeight - 96 + 2; }
        }
        gg.drawString(this.font, this.title, 8, titleY, 0x404040, false);
        gg.drawString(this.font, this.playerInventoryTitle, 8, invTitleY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics gg, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(gg, mouseX, mouseY, partialTicks);
        super.render(gg, mouseX, mouseY, partialTicks);
        this.renderTooltip(gg, mouseX, mouseY);
    }
}
