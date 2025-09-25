package net.ookasamoti.gstationmod.lunchbox;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SmithingLunchboxGate {

    /** ベーススロットにこの stack を入れて良いか？（未解禁の段階なら false） */
    public static boolean allowBaseSlot(ItemStack stack, Level level) {
        if (!(stack.getItem() instanceof LunchBoxItem lb)) return true;

        int need = switch (lb.size()) {
            case T1_5  -> 2;
            case T2_9  -> 3;
            case T3_27 -> 4;
            case T4_54 -> Integer.MAX_VALUE;
        };

        return LunchboxUnlocks.isUnlocked(level, need);
    }

    private SmithingLunchboxGate() {}
}