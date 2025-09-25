package net.ookasamoti.gstationmod.common;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

import net.ookasamoti.gstationmod.GStationMod;

public final class BoundaryDamage {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(GStationMod.MODID, "boundary");
    public static final ResourceKey<DamageType> KEY =
            ResourceKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE, ID);

    public static DamageSource source(ServerLevel level) {
        return level.damageSources().source(KEY);
    }

    private BoundaryDamage() {}
}
