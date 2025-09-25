package net.ookasamoti.gstationmod.mixin;

import github.mrh0.buildersaddition2.entity.seat.SeatEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = SeatEntity.class, remap = true)
public abstract class BA2SeatEntityFixMixin {

    @Shadow public Vec3 dismountLocation;
    @Unique private int gstationmod$graceTicks = 40;
    @Unique private Vec3 gstationmod$lastDismountBackup = null;

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z")
    )
    private boolean gstationmod$redirectPassengersIsEmpty(List<?> list) {
        final Level level = ((Entity)(Object)this).level();
        if (!level.isClientSide && gstationmod$graceTicks > 0) {
            return false;
        }
        return list.isEmpty();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void gstationmod$afterTick(CallbackInfo ci) {
        final Level level = ((Entity)(Object)this).level();
        if (!level.isClientSide && gstationmod$graceTicks > 0) {
            gstationmod$graceTicks--;
        }
        if (dismountLocation != null && !gstationmod$isZeroVec(dismountLocation)) {
            gstationmod$lastDismountBackup = dismountLocation;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void gstationmod$saveNBT(CompoundTag tag, CallbackInfo ci) {
        Vec3 v = (dismountLocation != null && !gstationmod$isZeroVec(dismountLocation))
                ? dismountLocation
                : gstationmod$lastDismountBackup;
        if (v != null) {
            tag.putDouble("gstationmod_dismount_x", v.x);
            tag.putDouble("gstationmod_dismount_y", v.y);
            tag.putDouble("gstationmod_dismount_z", v.z);
        }
        tag.putInt("gstationmod_grace", gstationmod$graceTicks);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void gstationmod$loadNBT(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("gstationmod_dismount_x")) {
            double x = tag.getDouble("gstationmod_dismount_x");
            double y = tag.getDouble("gstationmod_dismount_y");
            double z = tag.getDouble("gstationmod_dismount_z");
            this.dismountLocation = new Vec3(x, y, z);
            this.gstationmod$lastDismountBackup = this.dismountLocation;
        }
        this.gstationmod$graceTicks = Math.max(this.gstationmod$graceTicks, tag.getInt("gstationmod_grace"));
        if (this.gstationmod$graceTicks <= 0) this.gstationmod$graceTicks = 20;
    }

    @Inject(method = "getDismountLocationForPassenger", at = @At("HEAD"), cancellable = true)
    private void gstationmod$fallbackDismount(LivingEntity passenger, CallbackInfoReturnable<Vec3> cir) {
        Vec3 base = (dismountLocation != null && !gstationmod$isZeroVec(dismountLocation))
                ? dismountLocation
                : gstationmod$lastDismountBackup;

        if (base != null) {
            cir.setReturnValue(base);
        } else {

            Vec3 pos = ((Entity)(Object)this).position().add(0, 0.1, 0);
            cir.setReturnValue(pos);
        }
    }

    @Unique
    private static boolean gstationmod$isZeroVec(Vec3 v) {
        return v.x == 0.0 && v.y == 0.0 && v.z == 0.0;
    }
}
