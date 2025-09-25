package net.ookasamoti.gstationmod.lunchbox;

import com.mojang.logging.LogUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

import static net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil.*;

public class LunchBoxItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public enum Size {
        T1_5 (5),
        T2_9 (9),
        T3_27(27),
        T4_54(54);
        public final int slots;
        Size(int s){ this.slots = s; }
    }

    private final Size size;

    public LunchBoxItem(Size size, Properties props) {
        super(props);
        this.size = size;
    }

    public int slots() { return size.slots; }

    public Size size() { return size; }

    @Override
    @SuppressWarnings({ "deprecation", "removal" })
    public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientItemExtensions> consumer) {
        consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientItemExtensions() {
            private final net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer renderer =
                    new net.ookasamoti.gstationmod.lunchbox.client.LunchBoxDynamicRenderer(
                            net.minecraft.client.Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            net.minecraft.client.Minecraft.getInstance().getEntityModels()
                    );
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return renderer;
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack box = player.getItemInHand(hand);

        cycleSelected(box, +1);
        cycleSelected(box, -1);
        if (level.isClientSide && net.minecraft.client.gui.screens.Screen.hasControlDown()) {
            net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new net.ookasamoti.gstationmod.lunchbox.net.OpenLunchBoxC2S()
            );
            return InteractionResultHolder.sidedSuccess(box, true);
        }

        ItemStack sel = getSelectedFood(box);
        if (!sel.isEmpty()) {
            FoodProperties fp = sel.get(DataComponents.FOOD);
            if (fp != null && player.canEat(fp.canAlwaysEat())) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(box);
            }
        }
        return InteractionResultHolder.pass(box);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        ItemStack sel = getSelectedFood(stack);
        return sel.get(DataComponents.FOOD) != null ? UseAnim.EAT : UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        ItemStack sel = getSelectedFood(stack);
        FoodProperties fp = sel.get(DataComponents.FOOD);
        if (fp != null) {
            float secs = fp.eatSeconds();
            if (!Float.isFinite(secs) || secs <= 0f) secs = 1.6f;

            if (entity instanceof Player p) {
                String key = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(sel.getItem()).toString();
                int sat = net.ookasamoti.gstationmod.common.PlayerNutritionData.getSatiety(p, key);
                if (sat > 30) secs *= 1.2f;
            }
            return Mth.ceil(secs * 20f);
        }
        return 0;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack box, Level level, LivingEntity entity) {
        if (!(entity instanceof Player player)) return box;

        int selIdx = getSelectedIndex(box);
        ItemContainerContents cont = getContents(box);

        List<ItemStack> list = net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil.contentsAsList(cont);
        if (selIdx < 0 || selIdx >= list.size()) return box;

        ItemStack slot = list.get(selIdx);
        if (slot.isEmpty()) return box;

        var fp = slot.get(DataComponents.FOOD);
        if (fp == null) return box;

        ItemStack one = slot.copy();
        one.setCount(1);
        ItemStack leftover = slot.getItem().finishUsingItem(one, level, player);

        slot.shrink(1);
        if (slot.isEmpty()) slot = ItemStack.EMPTY;
        list.set(selIdx, slot);
        ItemContainerContents newCont = ItemContainerContents.fromItems(list);
        setContents(box, newCont);

        if (!leftover.isEmpty() && leftover.getItem() != Items.AIR) {
            if (!player.addItem(leftover)) {
                if (!tryInsertIntoBox(box, leftover)) {
                    player.drop(leftover, false);
                }
            }
        }

        if (!level.isClientSide()) {
            selectLowestSatiety(box, player);
            LOGGER.info("test");
        }
//        if (level.isClientSide()) {
//            selectLowestSatiety(box, player);
//        }

        return box;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.empty();
    }
}
