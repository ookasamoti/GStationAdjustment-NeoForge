package net.ookasamoti.gstationmod.event;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import net.ookasamoti.gstationmod.common.PlayerNutritionData;
import net.ookasamoti.gstationmod.config.FoodOverrideConfig;
import net.ookasamoti.gstationmod.config.ServerConfig;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;
import net.ookasamoti.gstationmod.net.SatietySync;

import java.util.ArrayList;

public final class NutritionEvents {

    private static boolean participatesInSatiety(ItemStack stack) {
        if (stack.get(DataComponents.FOOD) == null) return false;
        if (FoodOverrideConfig.reliefOnlyFor(stack.getItem())) return false;
        return true;
    }

    public static void onRightClick(PlayerInteractEvent.RightClickItem e) {
        if (e.getLevel().isClientSide()) return;
        Player p = e.getEntity();
        ItemStack stack = resolveEatenFood(e.getItemStack());

        if (!participatesInSatiety(stack)) return;

        var food = stack.get(DataComponents.FOOD);
        if (food == null) return;

        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int sat = PlayerNutritionData.getSatiety(p, key);
        if (sat >= 100) {
            p.displayClientMessage(Component.translatable("gstation.nutrition.cannot_eat").withStyle(ChatFormatting.RED), true);
            e.setCanceled(true);
        }

        var tmpRoot = p.getPersistentData();
        var tmp = tmpRoot.getCompound("gstation_tmp");
        tmp.putInt("pre_food", p.getFoodData().getFoodLevel());
        tmp.putFloat("pre_sat", p.getFoodData().getSaturationLevel());
        tmpRoot.put("gstation_tmp", tmp);
    }

    public static void onFinish(LivingEntityUseItemEvent.Finish e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide()) return;

        ItemStack stack = resolveEatenFood(e.getItem());

        if (!participatesInSatiety(stack)) {
            int relief = Math.max(1, FoodOverrideConfig.satisfactionFor(stack.getItem()));
            if (relief > 0) {
                reduceAllSatiety(p, relief);
                if (p instanceof ServerPlayer sp) SatietySync.syncSatiety(sp);
            }
            return;
        }

        FoodProperties base = stack.get(DataComponents.FOOD);
        if (base == null) return;

        int satiationRelief = Math.max(1, FoodOverrideConfig.satisfactionFor(stack.getItem()));

        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int satNow = PlayerNutritionData.getSatiety(p, key);

        int cumBefore = PlayerNutritionData.getCumulative(p);
        int msBefore  = ServerConfig.milestoneIndex(cumBefore);

        float hungerSatMult = 1.0f;
        int   nutritionDelta = +1;

        if (satNow > 70) {
            nutritionDelta += -(3 * msBefore);
            hungerSatMult = 0.1f;
        } else if (satNow > 50) {
            nutritionDelta += -(2 * msBefore);
            hungerSatMult = 0.5f;
        } else if (satNow > 30) {
            nutritionDelta += -(1 * msBefore);
        }

        int   refH   = base.nutrition();
        float refSat = base.saturation();

        int   targetAddH   = Math.max(0, Mth.ceil(refH * hungerSatMult));
        float targetAddSat = (float)Math.ceil((refSat * hungerSatMult) * 10.0) / 10.0f;

        var tmpRoot = p.getPersistentData();
        var tmp = tmpRoot.getCompound("gstation_tmp");
        int   preFood = tmp.contains("pre_food") ? tmp.getInt("pre_food") : Math.max(0, p.getFoodData().getFoodLevel() - refH);
        float preSat  = tmp.contains("pre_sat")  ? tmp.getFloat("pre_sat") : Math.max(0f, p.getFoodData().getSaturationLevel() - refSat);
        tmpRoot.remove("gstation_tmp");

        FoodData data = p.getFoodData();
        int   newFood = Mth.clamp(preFood + targetAddH, 0, 20);
        float newSat  = Mth.clamp(preSat + targetAddSat, 0f, newFood);
        data.setFoodLevel(newFood);
        data.setSaturation(newSat);

        int cumAfter = Mth.clamp(cumBefore + nutritionDelta, 0, ServerConfig.MAX_CUM_NUTRITION.get());
        PlayerNutritionData.setCumulative(p, cumAfter);

        int inc = ServerConfig.BASE_SATIETY.get() + (ServerConfig.EXTRA_SATIETY_PER_MS.get() * msBefore);
        var history = PlayerNutritionData.lastN(p, 1);
        if (!history.isEmpty() && key.equals(history.get(history.size() - 1))) inc += 10;
        PlayerNutritionData.setSatiety(p, key, Math.min(100, satNow + inc));

        if (satNow <= 50 && satiationRelief > 0) {

            reduceOthersSatiety(p, key, satiationRelief);
        }

        PlayerNutritionData.pushLast(p, key);

        int msAfter = ServerConfig.milestoneIndex(cumAfter);
        if (msAfter != PlayerNutritionData.getHpBonusApplied(p)) {
            PlayerNutritionData.applyHealthBonus(p, msAfter, ServerConfig.hpBonusHeartsForMs(msAfter));
            PlayerNutritionData.setHpBonusApplied(p, msAfter);
        }
        applyPersistentEffects(p, msAfter);

        if (p instanceof ServerPlayer sp) SatietySync.syncSatiety(sp);
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide()) return;

        purgeNonFoodSatiety(p);

        int cum = PlayerNutritionData.getCumulative(p);
        int ms  = ServerConfig.milestoneIndex(cum);
        PlayerNutritionData.applyHealthBonus(p, ms, ServerConfig.hpBonusHeartsForMs(ms));
        PlayerNutritionData.setHpBonusApplied(p, ms);
        applyPersistentEffects(p, ms);
        if (p instanceof ServerPlayer sp) SatietySync.syncSatiety(sp);
    }

    public static void onPlayerClone(PlayerEvent.Clone e) {

        if (!e.isWasDeath()) return;
        if (!(e.getEntity() instanceof Player p)) return;
        if (p.level().isClientSide()) return;

        int cum = PlayerNutritionData.getCumulative(p);
        int after = Math.max(0, cum - 40);
        PlayerNutritionData.setCumulative(p, after);

        int ms  = ServerConfig.milestoneIndex(after);
        PlayerNutritionData.applyHealthBonus(p, ms, ServerConfig.hpBonusHeartsForMs(ms));
        PlayerNutritionData.setHpBonusApplied(p, ms);
    }

    public static void onTooltip(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        FoodProperties fp = stack.get(DataComponents.FOOD);
        if (fp == null) return;

        String key = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        int sat = e.getEntity() instanceof Player p ? PlayerNutritionData.getSatiety(p, key) : 0;

        int relief = Math.max(1, FoodOverrideConfig.satisfactionFor(stack.getItem()));

        e.getToolTip().add(Component.literal("——").withStyle(ChatFormatting.DARK_GRAY));
        e.getToolTip().add(Component.literal("飽食値: " + sat + " / 100").withStyle(
                sat >= 100 ? ChatFormatting.RED : (sat >= 70 ? ChatFormatting.GOLD : ChatFormatting.YELLOW)
        ));
        e.getToolTip().add(Component.literal("飽食軽減: " + relief).withStyle(ChatFormatting.AQUA));
    }

    public static ItemStack resolveEatenFood(ItemStack stack) {
        if (stack.getItem() instanceof LunchBoxItem) {
            return LunchBoxUtil.getSelectedFood(stack);
        }
        return stack;
    }
    private static void reduceOthersSatiety(Player p, String exceptKey, int dec) {
        var root   = p.getPersistentData().getCompound("gstation_nutrition");
        var satMap = root.getCompound("satiety");
        for (String k : new ArrayList<>(satMap.getAllKeys())) {
            if (!k.equals(exceptKey)) {
                int v  = satMap.getInt(k);
                int nv = Math.max(0, v - dec);
                PlayerNutritionData.setSatiety(p, k, nv);
            }
        }
    }
    private static void reduceAllSatiety(Player p, int dec) {
        var root   = p.getPersistentData().getCompound("gstation_nutrition");
        var satMap = root.getCompound("satiety");
        for (String k : new ArrayList<>(satMap.getAllKeys())) {
            int v  = satMap.getInt(k);
            int nv = Math.max(0, v - dec);
            PlayerNutritionData.setSatiety(p, k, nv);
        }
    }
    private static void applyPersistentEffects(Player p, int msIdx) {
        // トグルのON/OFFは PlayerNutritionData 側に boolean を持たせている想定（既存キーで）
        if (!PlayerNutritionData.isPersistentEffectsEnabled(p)) return;

        var effects = ServerConfig.persistentEffects();
        for (var def : effects) {
            if (msIdx >= def.ms()) {
                var holder = BuiltInRegistries.MOB_EFFECT.getHolder(def.id()).orElse(null);
                if (holder != null) {
                    // パーティクル無し、アイコン非表示
                    p.addEffect(new MobEffectInstance(holder,
                            20 * 30, // 30秒を小刻みに再付与（切れ目なし）
                            Math.max(0, def.amplifier()),
                            true,  // ambient
                            false  // showParticles
                    ));
                }
            }
        }
    }

    // FOODを持たないアイテムの飽食エントリをクリーンアップ
    private static void purgeNonFoodSatiety(Player p) {
        var root = p.getPersistentData().getCompound("gstation_nutrition");
        var satMap = root.getCompound("satiety");
        var keys = new ArrayList<>(satMap.getAllKeys()); // コピーしてループ

        for (String id : keys) {
            // id→Itemを解決できない/FOODが無い → 飽食値を削除（=0）
            net.minecraft.resources.ResourceLocation rl;
            try {
                rl = net.minecraft.resources.ResourceLocation.parse(id);
            } catch (Exception ex) {
                PlayerNutritionData.setSatiety(p, id, 0);
                continue;
            }

            var item = BuiltInRegistries.ITEM.get(rl);
            if (item == null) {
                PlayerNutritionData.setSatiety(p, id, 0);
                continue;
            }

            var fp = item.components().get(DataComponents.FOOD);
            if (fp == null) {
                PlayerNutritionData.setSatiety(p, id, 0);
            }
        }
    }

    private NutritionEvents() {}
}
