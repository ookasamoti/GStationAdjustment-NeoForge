// src/main/java/net/ookasamoti/gstationmod/lunchbox/recipe/LunchboxUnlocks.java
package net.ookasamoti.gstationmod.lunchbox;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class LunchboxUnlocks extends SavedData {

    private static final String KEY = "gstation_lunchbox_unlocks";


    private int unlockedTier = 1;

    private static volatile int CACHED = 1;

    public static LunchboxUnlocks get(ServerLevel level) {
        SavedData.Factory<LunchboxUnlocks> factory =
                new SavedData.Factory<>(LunchboxUnlocks::new, LunchboxUnlocks::load, null);
        return level.getDataStorage().computeIfAbsent(factory, KEY);
    }

    public static boolean isUnlocked(Level level, int tier) {
        if (level instanceof ServerLevel sl) {
            return get(sl).unlockedTier >= tier;
        }
        return CACHED >= tier;
    }

    public static boolean isUnlockedClientCached(int tier) {
        return CACHED >= tier;
    }

    public int unlockedTier() {
        return unlockedTier;
    }

    public void setTo(int tier) {
        int t = clampTier(tier);
        if (t != this.unlockedTier) {
            this.unlockedTier = t;
            CACHED = t;       // サーバ側のキャッシュも更新（無害）
            setDirty();
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void setClientCache(int tier) {
        CACHED = clampTier(tier);
    }

    public static int getCachedTier() {
        return CACHED;
    }

    public LunchboxUnlocks() {}

    public static LunchboxUnlocks load(CompoundTag tag, HolderLookup.Provider provider) {
        LunchboxUnlocks d = new LunchboxUnlocks();
        d.unlockedTier = clampTier(tag.getInt("tier"));
        CACHED = d.unlockedTier;
        return d;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("tier", unlockedTier);
        return tag;
    }

    /* ---------------------------------------------------------------------------------------------- */

    private static int clampTier(int t) {
        return t < 1 ? 1 : (t > 4 ? 4 : t);
    }
}
