package net.ookasamoti.gstationmod.net;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import net.ookasamoti.gstationmod.GStationMod;

public final class MovementNet {
    private static final Map<UUID, Long> LAST_KILL_TICK = new HashMap<>();

    public static void register(RegisterPayloadHandlersEvent e) {
        var registrar = e.registrar(GStationMod.MODID);
        registrar.playToServer(MovementViolationC2S.TYPE, MovementViolationC2S.STREAM_CODEC, MovementNet::handleViolation);
    }

    private static void handleViolation(MovementViolationC2S msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            var p = ctx.player();
            if (!(p instanceof ServerPlayer sp)) return;

            if (sp.isCreative() || sp.isSpectator()) return;

            var level = sp.serverLevel();

            if (level.dimension() != Level.OVERWORLD) return;

            int limit = Mth.clamp(msg.limit(), 1, 300_000);

            if (Math.abs(sp.getX()) <= limit && Math.abs(sp.getZ()) <= limit) return;

            long now = level.getGameTime();
            Long last = LAST_KILL_TICK.get(sp.getUUID());
            if (last != null && now - last < 100) return;
            LAST_KILL_TICK.put(sp.getUUID(), now);

            level.playSound(null, sp.getX(), sp.getY(), sp.getZ(),
                    SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.0f);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    sp.getX(), sp.getY() + 0.5, sp.getZ(), 1, 0, 0, 0, 0);

            sp.hurt(net.ookasamoti.gstationmod.common.BoundaryDamage.source(level), Float.MAX_VALUE);
        });
    }

    private MovementNet() {}
}
