package net.ookasamoti.gstationmod.lunchbox.net;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxItem;
import net.ookasamoti.gstationmod.lunchbox.LunchBoxUtil;

public record CycleSlotC2S(int delta) implements CustomPacketPayload {

    public static final Type<CycleSlotC2S> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("gstationmod", "cycle_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CycleSlotC2S> STREAM_CODEC =
            StreamCodec.ofMember(CycleSlotC2S::write, CycleSlotC2S::new);

    private CycleSlotC2S(RegistryFriendlyByteBuf buf) { this(buf.readVarInt()); }
    private void write(RegistryFriendlyByteBuf buf)    { buf.writeVarInt(this.delta); }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(CycleSlotC2S msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer sp)) return;

        int d = msg.delta();
        if (d == 0) return;
        int step = d > 0 ? 1 : -1;

        var main = sp.getMainHandItem();
        var off  = sp.getOffhandItem();

        boolean changed = false;

        if (main.getItem() instanceof LunchBoxItem) {
            LunchBoxUtil.cycleSelected(main, step);
            changed = true;
        } else if (off.getItem() instanceof LunchBoxItem) {
            LunchBoxUtil.cycleSelected(off, step);
            changed = true;
        }

        if (changed) {
            sp.getInventory().setChanged();
            sp.containerMenu.broadcastChanges();
        }
    }
}
