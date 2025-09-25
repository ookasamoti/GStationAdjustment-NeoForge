package net.ookasamoti.gstationmod.mixin;

import com.pam.pamhc2trees.init.BlockRegistration;
import com.pam.pamhc2trees.init.ItemRegistration;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = ItemRegistration.class, remap = true)
public abstract class PamTreesFixAvocadoBlockItemMixin {

    @ModifyArg(
            method = "lambda$static$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/BlockItem;<init>(Lnet/minecraft/world/level/block/Block;Lnet/minecraft/world/item/Item$Properties;)V"
            ),
            index = 0
    )
    private static Block gstationmod$fixAvocadoBlockArg(Block original) {
        Block fixed = (Block) BlockRegistration.pamavocado.get();
        return fixed != null ? fixed : original;
    }
}
