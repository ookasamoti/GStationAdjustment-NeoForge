package net.ookasamoti.gstationmod.lunchbox;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.ookasamoti.gstationmod.common.PlayerNutritionData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class LunchBoxUtil {
    public static final int SIZE = 54;
    private static final String NBT_ROOT = "gstation_lunch";
    private static final String NBT_SEL  = "sel";

    public static ItemContainerContents getContents(ItemStack box) {
        ItemContainerContents c = box.get(DataComponents.CONTAINER);
        return (c != null) ? c : emptyContents();
    }

    public static void setContents(ItemStack box, ItemContainerContents c) {
        box.set(DataComponents.CONTAINER, c);
    }

    public static int getSelectedIndex(ItemStack box) {
        CompoundTag tag = box.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag root = tag.contains(NBT_ROOT, 10) ? tag.getCompound(NBT_ROOT) : new CompoundTag();
        return root.getInt(NBT_SEL);
    }
    public static void setSelectedIndex(ItemStack box, int idx) {
        int clamped = Math.max(0, Math.min(SIZE - 1, idx));
        CompoundTag cd = box.getOrDefault(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        CompoundTag root = cd.contains(NBT_ROOT, 10) ? cd.getCompound(NBT_ROOT) : new CompoundTag();
        root.putInt(NBT_SEL, clamped);
        cd.put(NBT_ROOT, root);
        box.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(cd));
    }

    public static ItemStack getSelectedFood(ItemStack box) {
        List<ItemStack> list = contentsAsList(getContents(box));
        int idx = getSelectedIndex(box);
        if (idx < 0 || idx >= list.size()) return ItemStack.EMPTY;
        ItemStack st = list.get(idx);
        return st.get(DataComponents.FOOD) != null ? st : ItemStack.EMPTY;
    }

    public static void cycleSelected(ItemStack box, int dir) {
        List<ItemStack> list = contentsAsList(getContents(box));
        int n = list.size();
        if (n <= 0) return;

        int i = getSelectedIndex(box);
        for (int step = 0; step < n; step++) {
            i = (i + (dir > 0 ? 1 : -1) + n) % n;
            if (!list.get(i).isEmpty()) {
                setSelectedIndex(box, i);
                return;
            }
        }
    }

    public static void selectLowestSatiety(ItemStack box, Player player) {
        if (player.level().isClientSide()) return;

        ItemContainerContents cont = getContents(box);
        List<ItemStack> list = contentsAsList(cont);

        int bestIdx = -1;
        int bestSat = Integer.MAX_VALUE;

        int excludeIdx = getSelectedIndex(box);

        for (int i = 0; i < list.size(); i++) {
            if (i == excludeIdx) continue;
            ItemStack s = list.get(i);
            if (s.isEmpty()) continue;
            if (s.get(DataComponents.FOOD) == null) continue;

            String key = BuiltInRegistries.ITEM.getKey(s.getItem()).toString();
            int sat = PlayerNutritionData.getSatiety(player, key);
            if (sat < bestSat) { bestSat = sat; bestIdx = i; }
        }

        player.displayClientMessage(Component.translatable("gstation.nutrition.cannot_eat").withStyle(ChatFormatting.RED), true);

        String msg = String.format("LunchBox: auto-selected slot %d (sat %d)", bestIdx, bestSat);
        LogUtils.getLogger().info("{} | {}", msg, player.getGameProfile().getName());

//        if (bestIdx >= 0) {
//            setSelectedIndex(box, bestIdx);
//        }

        if (bestIdx >= 0) {
            setSelectedIndex(box, bestIdx);

            if (player.getMainHandItem() == box) {
                player.setItemInHand(InteractionHand.MAIN_HAND, box);
            } else if (player.getOffhandItem() == box) {
                player.setItemInHand(InteractionHand.OFF_HAND, box);
            }
            player.getInventory().setChanged();
            if (player.containerMenu != null) player.containerMenu.broadcastChanges();
            player.inventoryMenu.broadcastChanges();
        }
    }

    public static boolean tryInsertIntoBox(ItemStack box, ItemStack in) {
        if (in.isEmpty()) return true;

        List<ItemStack> list = contentsAsList(getContents(box));
        ensureSize(list, SIZE);

        for (int i = 0; i < list.size() && !in.isEmpty(); i++) {
            ItemStack s = list.get(i);
            if (!s.isEmpty() && ItemStack.isSameItemSameComponents(s, in)) {
                int can = Math.min(s.getMaxStackSize() - s.getCount(), in.getCount());
                if (can > 0) {
                    s.grow(can);
                    in.shrink(can);
                }
            }
        }

        for (int i = 0; i < list.size() && !in.isEmpty(); i++) {
            ItemStack s = list.get(i);
            if (s.isEmpty()) {
                int put = Math.min(in.getCount(), in.getMaxStackSize());
                ItemStack copy = in.copy();
                copy.setCount(put);
                list.set(i, copy);
                in.shrink(put);
            }
        }

        setContents(box, ItemContainerContents.fromItems(list));
        return in.isEmpty();
    }

    private static ItemContainerContents emptyContents() {
        List<ItemStack> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) list.add(ItemStack.EMPTY);
        return ItemContainerContents.fromItems(list);
    }

    private static void ensureSize(List<ItemStack> list, int size) {
        if (list.size() < size) {
            int need = size - list.size();
            for (int i = 0; i < need; i++) list.add(ItemStack.EMPTY);
        } else if (list.size() > size) {
            while (list.size() > size) list.remove(list.size() - 1);
        }
    }

    public static List<ItemStack> contentsAsList(ItemContainerContents cont) {
        List<ItemStack> out = new ArrayList<>();
        if (cont == null) {
            ensureSize(out, SIZE);
            return out;
        }

        try {
            Method mSize = findNoArg(cont.getClass(), "size", "getSlotCount");
            Method mGet  = findIntArg(cont.getClass(), "getStack", "getItem");
            if (mSize != null && mGet != null) {
                int n = (int) mSize.invoke(cont);
                for (int i = 0; i < n; i++) {
                    ItemStack s = (ItemStack) mGet.invoke(cont, i);
                    out.add(s.copy());
                }
                ensureSize(out, SIZE);
                return out;
            }
        } catch (Throwable ignored) {}

        try {
            Field f = cont.getClass().getDeclaredField("items");
            f.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<ItemStack> items = (List<ItemStack>) f.get(cont);
            for (ItemStack s : items) out.add(s.copy());
            ensureSize(out, SIZE);
            return out;
        } catch (Throwable ignored) {}

        ensureSize(out, SIZE);
        return out;
    }

    private static Method findNoArg(Class<?> c, String... names) {
        for (String n : names) {
            try { return c.getMethod(n); } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }
    private static Method findIntArg(Class<?> c, String... names) {
        for (String n : names) {
            try { return c.getMethod(n, int.class); } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    private LunchBoxUtil() {}
}
