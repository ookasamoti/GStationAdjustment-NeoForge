package net.ookasamoti.gstationmod.mixin;

import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.ookasamoti.gstationmod.lunchbox.SmithingLunchboxGate;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.OptionalInt;

@Mixin(SmithingMenu.class)
public abstract class SmithingMenuMixin {

    @Shadow @Final private Level level;
    @Shadow @Final private List<RecipeHolder<SmithingRecipe>> recipes;

    @Inject(method = "createInputSlotDefinitions", at = @At("RETURN"), cancellable = true)
    private void gstation$gateBaseSlot(CallbackInfoReturnable<ItemCombinerMenuSlotDefinition> cir) {

        ItemCombinerMenuSlotDefinition def = ItemCombinerMenuSlotDefinition.create()
                .withSlot(0, 8, 48, stack ->
                        this.recipes.stream().anyMatch(r -> r.value().isTemplateIngredient(stack)))
                .withSlot(1, 26, 48, stack ->
                        SmithingLunchboxGate.allowBaseSlot(stack, this.level) &&
                                this.recipes.stream().anyMatch(r -> r.value().isBaseIngredient(stack)))
                .withSlot(2, 44, 48, stack ->
                        this.recipes.stream().anyMatch(r -> r.value().isAdditionIngredient(stack)))
                .withResultSlot(3, 98, 48)
                .build();

        cir.setReturnValue(def);
    }

    @Inject(method = "findSlotToQuickMoveTo", at = @At("HEAD"), cancellable = true)
    private void gstation$blockQuickMove(ItemStack stack, CallbackInfoReturnable<OptionalInt> cir) {
        if (!SmithingLunchboxGate.allowBaseSlot(stack, this.level)) {
            OptionalInt alt = this.recipes.stream()
                    .flatMapToInt(r -> {
                        SmithingRecipe rec = r.value();
                        if (rec.isTemplateIngredient(stack)) return java.util.stream.IntStream.of(0);
                        if (rec.isBaseIngredient(stack))     return java.util.stream.IntStream.of(1);
                        if (rec.isAdditionIngredient(stack)) return java.util.stream.IntStream.of(2);
                        return java.util.stream.IntStream.empty();
                    })
                    .filter(i -> i != 1)
                    .filter(i -> !((SmithingMenu)(Object)this).getSlot(i).hasItem())
                    .findFirst();

            cir.setReturnValue(alt);
        }
    }
}