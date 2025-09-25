package net.ookasamoti.gstationmod.config;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.List;

public final class ServerConfig {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BASE_SATIETY = B
            .comment("Base satiety increment per eating.")
            .defineInRange("nutrition.base_satiety", 10, 0, 100);

    public static final ModConfigSpec.IntValue EXTRA_SATIETY_PER_MS = B
            .comment("Extra satiety per milestone (added as EXTRA * milestoneIndex).")
            .defineInRange("nutrition.extra_satiety_per_ms", 5, 0, 100);

    public static final ModConfigSpec.ConfigValue<List<? extends Integer>> MILESTONES = B
            .comment("Cumulative nutrition thresholds for 5 milestones (ascending).")
            .defineList("nutrition.milestones",
                    List.of(20, 40, 60, 80, 100),
                    o -> o instanceof Number n && n.intValue() >= 0);

    public static final ModConfigSpec.IntValue MAX_CUM_NUTRITION = B
            .comment("Hard cap for cumulative nutrition.")
            .defineInRange("nutrition.max_cumulative", 100, 0, 100000);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> PERSISTENT_EFFECTS = B
            .comment("Persistent (toggleable) effects per milestone. Format: \"<ms>=<effect_id>,<duration(ignored)>,<amplifier>\"")
            .defineListAllowEmpty("nutrition.effects", List.of(
                    "1=,0,0",
                    "2=,0,0",
                    "3=,0,0",
                    "4=,0,0",
                    "5=,0,0"
            ), o -> o instanceof String s && s.contains("=") && s.contains(","));

    public static final ModConfigSpec.ConfigValue<List<? extends String>> HP_BONUSES = B
            .comment("Max health bonuses by milestone. Format: \"<ms>=<hearts_half_units>\" (e.g., \"1=0.5\")")
            .defineListAllowEmpty("nutrition.hp_bonuses", List.of(
                    "1=0.5", "2=0.5", "3=0.5", "4=0.5", "5=1.0"
            ), o -> o instanceof String s && s.contains("="));

    public static final ModConfigSpec SPEC = B.build();
    private ServerConfig() {}

    public static int milestoneIndex(int cum) {
        var list = MILESTONES.get();
        int idx = 0;
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            int th = ((Number) list.get(i)).intValue(); // ★ 追加（LongでもOKに）
            if (cum >= th) idx++; else break;
        }
        return idx;
    }

    public record EffectDef(int ms, ResourceLocation id, int amplifier) {}
    public static List<EffectDef> persistentEffects() {
        List<EffectDef> out = new ArrayList<>();
        for (String raw : PERSISTENT_EFFECTS.get()) {
            try {
                String[] lr = raw.split("=");
                int ms = Integer.parseInt(lr[0].trim());
                String[] parts = lr[1].split(",");
                ResourceLocation id = ResourceLocation.parse(parts[0].trim());
                int amp = Integer.parseInt(parts[2].trim());
                out.add(new EffectDef(ms, id, amp));
            } catch (Exception ignored) {}
        }
        return out;
    }

    public static float hpBonusHeartsForMs(int msIdx) {
        float sum = 0f;
        for (String raw : HP_BONUSES.get()) {
            try {
                String[] lr = raw.split("=");
                int ms = Integer.parseInt(lr[0].trim());
                float hearts = Float.parseFloat(lr[1].trim());
                if (msIdx >= ms) sum += hearts;
            } catch (Exception ignored) {}
        }
        return sum;
    }
}
