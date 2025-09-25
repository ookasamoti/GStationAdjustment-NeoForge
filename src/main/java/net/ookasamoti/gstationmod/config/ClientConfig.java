package net.ookasamoti.gstationmod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder()
            .push("movement");

    public static final ModConfigSpec.BooleanValue REPORT_MOVEMENT_VIOLATION = B
        .comment("行動制限")
    .define("report_violation", false);

    public static final ModConfigSpec.IntValue CLIENT_LIMIT_DISTANCE = B
        .comment("行動制限距離")
        .defineInRange("limit_distance", 3000, 1, 300000);

    public static final ModConfigSpec SPEC = B.pop().build();

    private ClientConfig() {}
}