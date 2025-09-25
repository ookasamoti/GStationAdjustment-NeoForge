package net.ookasamoti.gstationmod.lunchbox.net;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.ookasamoti.gstationmod.GStationMod;

public final class NetLunchBox {
    public static final String PROTOCOL = "1";

    public static void register(final RegisterPayloadHandlersEvent e) {
        var reg = e.registrar(GStationMod.MODID).versioned(PROTOCOL);

        reg.playToServer(OpenLunchBoxC2S.TYPE, OpenLunchBoxC2S.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> OpenLunchBoxC2S.handle(msg, ctx)));

        reg.playToServer(CycleSlotC2S.TYPE, CycleSlotC2S.STREAM_CODEC,
                (msg, ctx) -> ctx.enqueueWork(() -> CycleSlotC2S.handle(msg, ctx)));
    }

    private NetLunchBox() {}
}
