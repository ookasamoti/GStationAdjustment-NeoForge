// src/main/java/net/ookasamoti/gstationmod/common/PlayerNutritionData.java
package net.ookasamoti.gstationmod.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlayerNutritionData {

    private static final String KEY_ROOT         = "gstation_nutrition";
    private static final String KEY_ROOT_LEGACY  = "gstation_diet";

    private static final String KEY_MAP          = "satiety";
    private static final String KEY_LAST         = "last5";
    private static final String KEY_CUM          = "cumulative";
    private static final String KEY_NUTR_LEGACY  = "nutrition";
    private static final String KEY_HPBN         = "hp_bonus";
    private static final String KEY_EFX_TOGGLE   = "effects_enabled";

    private static final ResourceLocation HEALTH_BONUS_ID =
            ResourceLocation.fromNamespaceAndPath("gstationmod", "nutrition_bonus");

    private PlayerNutritionData() {}

    private static CompoundTag root(Player p) {
        var pd = p.getPersistentData();

        if (!pd.contains(KEY_ROOT, Tag.TAG_COMPOUND) && pd.contains(KEY_ROOT_LEGACY, Tag.TAG_COMPOUND)) {
            CompoundTag legacy = pd.getCompound(KEY_ROOT_LEGACY).copy();
            pd.put(KEY_ROOT, legacy);
        }
        if (!pd.contains(KEY_ROOT, Tag.TAG_COMPOUND)) {
            pd.put(KEY_ROOT, new CompoundTag());
        }

        CompoundTag r = pd.getCompound(KEY_ROOT);

        if (r.contains(KEY_NUTR_LEGACY, Tag.TAG_INT) && !r.contains(KEY_CUM, Tag.TAG_INT)) {
            int old = r.getInt(KEY_NUTR_LEGACY);
            r.putInt(KEY_CUM, Math.max(0, old));
            r.remove(KEY_NUTR_LEGACY);
        }

        return r;
    }

    private static CompoundTag map(Player p) {
        var r = root(p);
        if (!r.contains(KEY_MAP, Tag.TAG_COMPOUND)) {
            r.put(KEY_MAP, new CompoundTag());
        }
        return r.getCompound(KEY_MAP);
    }

    public static int getSatiety(Player p, String itemId) {
        if (itemId == null || itemId.isEmpty()) return 0;
        var m = map(p);
        return m.contains(itemId) ? m.getInt(itemId) : 0;
    }

    public static void setSatiety(Player p, String itemId, int v) {
        if (itemId == null || itemId.isEmpty()) return;
        v = Math.max(0, Math.min(100, v));
        var m = map(p);
        if (v == 0) m.remove(itemId); else m.putInt(itemId, v);
    }

    public static List<String> last5(Player p) {
        var r = root(p);
        var out = new ArrayList<String>(5);
        var tag = r.getList(KEY_LAST, Tag.TAG_STRING);
        for (int i = 0; i < tag.size(); i++) out.add(tag.getString(i));
        return out;
    }

    public static List<String> lastN(Player p, int n) {
        List<String> all = last5(p);
        int size = all.size();
        if (n <= 0 || size == 0) return Collections.emptyList();
        if (n >= size) return new ArrayList<>(all);
        return new ArrayList<>(all.subList(size - n, size));
    }

    public static void pushLast(Player p, String id) {
        if (id == null || id.isEmpty()) return;
        var r = root(p);
        var tag = r.getList(KEY_LAST, Tag.TAG_STRING);
        tag.add(StringTag.valueOf(id));
        while (tag.size() > 5) tag.remove(0);
        r.put(KEY_LAST, tag);
    }

    public static void clearAllSatiety(Player p) {
        var r = root(p);

        r.remove(KEY_MAP);
        r.put(KEY_MAP, new CompoundTag());
    }

    public static int getCumulative(Player p) {
        return root(p).getInt(KEY_CUM);
    }

    public static void setCumulative(Player p, int v) {
        root(p).putInt(KEY_CUM, Math.max(0, v));
    }

    public static int getHpBonusApplied(Player p) {
        return root(p).getInt(KEY_HPBN);
    }

    public static void setHpBonusApplied(Player p, int v) {
        root(p).putInt(KEY_HPBN, Math.max(0, v));
    }

    public static boolean isPersistentEffectsEnabled(Player p) {
        var r = root(p);

        if (!r.contains(KEY_EFX_TOGGLE, Tag.TAG_BYTE)) return true;
        return r.getBoolean(KEY_EFX_TOGGLE);
    }

    public static void setPersistentEffectsEnabled(Player p, boolean enabled) {
        root(p).putBoolean(KEY_EFX_TOGGLE, enabled);
    }

    public static void applyHealthBonus(Player p, int msIdx, float hearts) {
        AttributeInstance inst = p.getAttribute(Attributes.MAX_HEALTH);
        if (inst == null) return;

        if (inst.getModifier(HEALTH_BONUS_ID) != null) {
            inst.removeModifier(HEALTH_BONUS_ID);
        }

        double amount = Math.max(0.0, hearts * 2.0);

        if (msIdx > 0 && amount > 0.0) {
            inst.addPermanentModifier(new AttributeModifier(
                    HEALTH_BONUS_ID,
                    amount,
                    AttributeModifier.Operation.ADD_VALUE
            ));
        }

        if (p.getHealth() > p.getMaxHealth()) {
            p.setHealth(p.getMaxHealth());
        }
    }
}
