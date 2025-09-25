package net.ookasamoti.gstationmod.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchboxUnlocks;

@EventBusSubscriber(modid = "gstationmod", value = Dist.DEDICATED_SERVER)
public class LunchboxSlotEvents {

    @SubscribeEvent
    public static void onOpen(PlayerContainerEvent.Open e) {
        if (!(e.getContainer() instanceof SmithingMenu menu)) return;
        if (!(e.getEntity() instanceof ServerPlayer sp)) return;

        var level = sp.serverLevel();
        var unlocks = LunchboxUnlocks.get(level);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot orig = menu.slots.get(i);
            menu.slots.set(i, new Slot(orig.container, orig.index, orig.x, orig.y) {
                @Override
                public boolean mayPlace(ItemStack stack) {

                    if (!orig.mayPlace(stack)) return false;

                    if (this.index == 1 && stack.getItem() instanceof LunchBoxItem box) {
                        int tier = switch (box.size()) {
                            case T1_5 -> 1;
                            case T2_9 -> 2;
                            case T3_27 -> 3;
                            case T4_54 -> 4;
                        };
                        return unlocks.unlockedTier() >= tier;
                    }
                    return true;
                }
            });
        }
    }
}