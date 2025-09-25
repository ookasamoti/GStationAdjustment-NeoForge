// src/main/java/net/ookasamoti/gstationmod/common/FoodDefaultApplier.java
package net.ookasamoti.gstationmod.common;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.ookasamoti.gstationmod.config.FoodOverrideConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class FoodDefaultApplier {

    @SubscribeEvent
    public static void onModifyDefaults(ModifyDefaultComponentsEvent e) {

        BuiltInRegistries.ITEM.holders().forEach(ref -> {
            Item item = ref.value();

            var ov = FoodOverrideConfig.resolve(item);
            if (ov == null) return;

            if (Boolean.TRUE.equals(ov.inedible())) {
                e.modify(item, comps -> comps.remove(DataComponents.FOOD));
                return;
            }

            FoodProperties base = item.components().get(DataComponents.FOOD);

            int nutrition = (ov.nutrition() != null)
                    ? Math.max(0, ov.nutrition())
                    : (base != null ? base.nutrition() : 0);

            float saturation;
            if (ov.saturation() != null) {
                saturation = Math.max(0f, ov.saturation());
            } else {
                saturation = (base != null ? base.saturation() : 0f);
            }

            boolean always = (ov.always_edible() != null)
                    ? ov.always_edible()
                    : (base != null && base.canAlwaysEat());

            float eatSec = (ov.eat_seconds() != null)
                    ? Math.max(0.01f, ov.eat_seconds())
                    : (base != null ? base.eatSeconds() : 1.6f);

            List<FoodProperties.PossibleEffect> effects = new ArrayList<>();
            if (ov.effects() != null && !ov.effects().isEmpty()) {
                for (var eff : ov.effects()) {
                    ResourceLocation id;
                    try { id = ResourceLocation.parse(eff.id()); } catch (Exception ex) { continue; }
                    Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(id).orElse(null);
                    if (holder == null) continue;

                    float prob = Math.max(0f, Math.min(1f, eff.probability()));
                    var supplier = (java.util.function.Supplier<MobEffectInstance>) () ->
                            new MobEffectInstance(holder, Math.max(1, eff.duration()), Math.max(0, eff.amplifier()));
                    effects.add(new FoodProperties.PossibleEffect(supplier, prob));
                }
            } else if (base != null) {
                effects = base.effects();
            }

            Optional<ItemStack> converts = (base != null) ? base.usingConvertsTo() : Optional.empty();

            FoodProperties patched = new FoodProperties(
                    nutrition,
                    saturation,
                    always,
                    eatSec,
                    converts,
                    effects
            );

            e.modify(item, comps -> comps.set(DataComponents.FOOD, patched));
        });
    }

    private FoodDefaultApplier() {}
}
