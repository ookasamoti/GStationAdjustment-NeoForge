package net.ookasamoti.gstationmod.common;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class NameCarryOverHandler {

    private record PendingCopy(InteractionHand hand, Component name, int ttl) {}

    private static final Map<UUID, EnumMap<InteractionHand, PendingCopy>> PENDING = new HashMap<>();

    @SubscribeEvent
    public static void onUseFinish(LivingEntityUseItemEvent.Finish e) {
        LivingEntity user = e.getEntity();
        if (user.level().isClientSide) return;

        ItemStack before = e.getItem();
        ItemStack after  = e.getResultStack();

        Component name = before.get(DataComponents.CUSTOM_NAME);
        if (name == null || after.isEmpty()) return;

        if (after.get(DataComponents.CUSTOM_NAME) == null) {
            after.set(DataComponents.CUSTOM_NAME, name);
            e.setResultStack(after);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        snapshotHandIfNamed(e.getEntity(), e.getHand());
    }
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        snapshotHandIfNamed(e.getEntity(), e.getHand());
    }

    private static void snapshotHandIfNamed(Player player, InteractionHand hand) {
        if (player == null || player.level().isClientSide) return;
        ItemStack inHand = player.getItemInHand(hand);
        if (inHand.isEmpty()) return;
        Component name = inHand.get(DataComponents.CUSTOM_NAME);
        if (name == null) return;

        var byHand = PENDING.computeIfAbsent(player.getUUID(), id -> new EnumMap<>(InteractionHand.class));
        byHand.put(hand, new PendingCopy(hand, name, 2));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post e) {
        if (PENDING.isEmpty()) return;

        PENDING.entrySet().removeIf(entry -> {
            UUID id = entry.getKey();
            Player player = e.getServer().getPlayerList().getPlayer(id);
            if (player == null) return true;

            EnumMap<InteractionHand, PendingCopy> map = entry.getValue();
            map.entrySet().removeIf(en -> {
                PendingCopy pc = en.getValue();
                ItemStack cur = player.getItemInHand(pc.hand);

                boolean done = false;
                if (!cur.isEmpty()) {

                    if (cur.get(DataComponents.CUSTOM_NAME) == null) {
                        cur.set(DataComponents.CUSTOM_NAME, pc.name);
                    }
                    done = true;
                }

                int rest = pc.ttl - 1;
                if (!done && rest > 0) {
                    en.setValue(new PendingCopy(pc.hand, pc.name, rest));
                    return false;
                }
                return true;
            });
            return map.isEmpty();
        });
    }

    private NameCarryOverHandler() {}
}
