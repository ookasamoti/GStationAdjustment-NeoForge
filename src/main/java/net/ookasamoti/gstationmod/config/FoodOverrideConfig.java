package net.ookasamoti.gstationmod.config;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class FoodOverrideConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    public record EffectSpec(String id, int duration, int amplifier, float probability) {}

    public record OverrideEntry(
            Integer  nutrition,
            Float    saturation,
            Boolean  always_edible,
            Float    eat_seconds,
            Boolean  inedible,
            Integer  gs_satisfaction,
            List<EffectSpec> effects,
            Integer  tan_thirst,
            Integer  tan_hydration,
            Boolean  tan_cooling_on_consume,
            Boolean  tan_heating_on_consume,
            Boolean  gs_relief_only
    ) {}

    public record Root(
            Map<String, OverrideEntry> items,
            Map<String, OverrideEntry> tags
    ) {}

    private static Map<String, OverrideEntry> ITEMS = Map.of();
    private static Map<String, OverrideEntry> TAGS  = Map.of();

    private FoodOverrideConfig() {}

    public static void load(Path gameDir) {
        Objects.requireNonNull(gameDir, "gameDir");

        Path single = gameDir.resolve("config/gstationmod/food_overrides.json");
        Path folder = gameDir.resolve("config/gstationmod/food_overrides");

        try {
            if (Files.isDirectory(folder)) {
                var merged = loadAllJsonUnder(folder);
                ITEMS = merged.items();
                TAGS  = merged.tags();
                LOGGER.info("[G-Station] Loaded {} item overrides and {} tag overrides from folder {}",
                        ITEMS.size(), TAGS.size(), folder.toAbsolutePath());
            } else {
                Files.createDirectories(single.getParent());
                if (!Files.exists(single)) {
                    Files.writeString(single, sampleJson());
                    LOGGER.info("[G-Station] Created sample {}", single.toAbsolutePath());
                }
                Root r = readOne(single);
                ITEMS = r.items() != null ? r.items() : Map.of();
                TAGS  = r.tags()  != null ? r.tags()  : Map.of();
                LOGGER.info("[G-Station] Loaded {} item overrides and {} tag overrides from {}",
                        ITEMS.size(), TAGS.size(), single.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load food overrides", e);
        }
    }

    public static int satisfactionFor(Item item) {
        OverrideEntry e = resolve(item);
        if (e != null && e.gs_satisfaction() != null) return Math.max(1, e.gs_satisfaction());
        return 1;
    }

    public static boolean reliefOnlyFor(Item item) {
        OverrideEntry e = resolve(item);
        return e != null && Boolean.TRUE.equals(e.gs_relief_only());
    }

    public static boolean isInedible(Item item) {
        OverrideEntry e = resolve(item);
        return e != null && Boolean.TRUE.equals(e.inedible());
    }

    public static OverrideEntry resolve(Item item) {
        var id = BuiltInRegistries.ITEM.getKey(item);
        if (id == null) return null;
        return ITEMS.get(id.toString());
    }

    public static Root exportRaw() {
        return new Root(ITEMS, TAGS);
    }

    public static Set<String> allKeys() {
        Set<String> s = new TreeSet<>();
        s.addAll(ITEMS.keySet());
        s.addAll(TAGS.keySet());
        return s;
    }

    private static Root loadAllJsonUnder(Path dir) throws IOException {
        Files.createDirectories(dir);
        List<Path> files;
        try (Stream<Path> st = Files.walk(dir)) {
            files = st.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".json"))
                    .sorted()
                    .collect(Collectors.toList());
        }
        if (files.isEmpty()) {
            Path sample = dir.resolve("000_example.json");
            Files.writeString(sample, sampleJson());
            Root r = readOne(sample);
            return new Root(
                    r.items() != null ? r.items() : Map.of(),
                    r.tags()  != null ? r.tags()  : Map.of()
            );
        }

        Map<String, OverrideEntry> mergedItems = new LinkedHashMap<>();
        Map<String, OverrideEntry> mergedTags  = new LinkedHashMap<>();
        for (Path file : files) {
            Root r = readOne(file);
            if (r.items() != null) r.items().forEach(mergedItems::put); // 後勝ち
            if (r.tags()  != null) r.tags().forEach(mergedTags::put);   // 後勝ち
            LOGGER.info("[G-Station] merged {}", file.toAbsolutePath());
        }
        return new Root(mergedItems, mergedTags);
    }

    private static Root readOne(Path file) throws IOException {
        try (Reader r = Files.newBufferedReader(file)) {
            Gson gson = new GsonBuilder().setLenient().create();
            Root parsed = gson.fromJson(r, Root.class);
            if (parsed == null) parsed = new Root(Map.of(), Map.of());
            return new Root(
                    parsed.items() != null ? parsed.items() : Map.of(),
                    parsed.tags()  != null ? parsed.tags()  : Map.of()
            );
        }
    }

    private static String sampleJson() {
        return """
        {
          "items": {
            "minecraft:golden_carrot": {
              "nutrition": 9,
              "saturation": 0.1,
              "always_edible": false,
              "eat_seconds": 1.6,
              "effects": [],
              "gs_satisfaction": 3
            },
            "minecraft:bread": {
              "inedible": true
            }
          },
          "tags": {
            "#gstationmod:berries": {
              "nutrition": 7,
              "saturation": 1.5,
              "always_edible": false,
              "eat_seconds": 1.6,
              "effects": [],
              "gs_satisfaction": 1
            }
          }
        }
        """;
    }
}
