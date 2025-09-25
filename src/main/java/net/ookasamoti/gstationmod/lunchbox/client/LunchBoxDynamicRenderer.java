package net.ookasamoti.gstationmod.lunchbox.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;
import net.ookasamoti.gstationmod.lunchbox.menu.LunchBoxScreen;

public final class LunchBoxDynamicRenderer extends BlockEntityWithoutLevelRenderer {

    // ティア別フォールバック（standalone変種）
    private static final ModelResourceLocation MRL_FALLBACK_T1 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t1_fallback"), "standalone");
    private static final ModelResourceLocation MRL_FALLBACK_T2 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t2_fallback"), "standalone");
    private static final ModelResourceLocation MRL_FALLBACK_T3 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t3_fallback"), "standalone");
    private static final ModelResourceLocation MRL_FALLBACK_T4 =
            new ModelResourceLocation(ResourceLocation.fromNamespaceAndPath("gstationmod", "item/lunch_box_t4_fallback"), "standalone");

    public LunchBoxDynamicRenderer(
            net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher berd,
            net.minecraft.client.model.geom.EntityModelSet models
    ) {
        super(berd, models);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose,
                             MultiBufferSource buf, int light, int overlay) {
        var mc = Minecraft.getInstance();
        var ir = mc.getItemRenderer();
        ClientLevel level = mc.level;

        if (!(stack.getItem() instanceof LunchBoxItem box)) {
            ir.renderStatic(stack, ctx, light, overlay, pose, buf, level, 0);
            return;
        }

        boolean inBoxGui = mc.screen instanceof LunchBoxScreen;

        pose.pushPose();

        switch (ctx) {
            case GUI -> pose.translate(0.5F, 0.5F, 0.0F);
            default   -> pose.translate(0.5F, 0.5F, 0.5F);
        }

        if (!inBoxGui) {
            ItemStack sel = LunchBoxUtil.getSelectedFood(stack);
            if (!sel.isEmpty()) {
                ir.renderStatic(sel, ctx, light, overlay, pose, buf, level, 0);
                pose.popPose();
                return;
            }
        }

        ModelResourceLocation mrl = switch (box.size()) {
            case T1_5  -> MRL_FALLBACK_T1;
            case T2_9  -> MRL_FALLBACK_T2;
            case T3_27 -> MRL_FALLBACK_T3;
            case T4_54 -> MRL_FALLBACK_T4;
        };

        BakedModel fallback = mc.getModelManager().getModel(mrl);
        ir.render(stack, ctx, false, pose, buf, light, overlay, fallback);

        pose.popPose();
    }
}
