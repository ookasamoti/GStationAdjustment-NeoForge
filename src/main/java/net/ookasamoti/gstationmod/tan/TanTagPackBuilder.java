package net.ookasamoti.gstationmod.tan;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.ookasamoti.gstationmod.config.FoodOverrideConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class TanTagPackBuilder {

    private TanTagPackBuilder(){}

    public static Path outputRoot(Path gameDir) {
        return gameDir.resolve("config/gstationmod/generated_datapack");
    }

    public static void rebuild(Path gameDir) {
        try {
            Path root = outputRoot(gameDir);
            Path data = root.resolve("data");
            Path tnItemTags = data.resolve("toughasnails/tags/item");

            if (Files.exists(root)) {
                deleteRecursively(root);
            }
            Files.createDirectories(tnItemTags);

            String mcmeta = """
            {
              "pack": {
                "pack_format": 48,
                "description": "G-Station generated TAN item tags"
              }
            }""";
            Files.writeString(root.resolve("pack.mcmeta"), mcmeta);

            Map<String, Set<String>> tagValues = new LinkedHashMap<>();

            java.util.function.BiConsumer<String, String> add = (tagPath, id) -> {
                tagValues.computeIfAbsent(tagPath, k -> new LinkedHashSet<>()).add(id);
            };

            FoodOverrideConfig.Root rootData = FoodOverrideConfig.exportRaw();
            Map<String, FoodOverrideConfig.OverrideEntry> itemMap = rootData.items();
            Map<String, FoodOverrideConfig.OverrideEntry> tagMap  = rootData.tags();

            for (var ent : itemMap.entrySet()) {
                String id = ent.getKey();
                FoodOverrideConfig.OverrideEntry ov = ent.getValue();
                applyOne(add, id, ov);
            }

            for (var ent : tagMap.entrySet()) {
                String tagRef = ent.getKey();
                FoodOverrideConfig.OverrideEntry ov = ent.getValue();
                applyOne(add, tagRef, ov);
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            for (var e : tagValues.entrySet()) {
                String tagPath = e.getKey();
                Set<String> values = e.getValue();

                Map<String, Object> json = new LinkedHashMap<>();
                json.put("replace", false);
                json.put("values", values);

                String out = gson.toJson(json);
                Path file = tnItemTags.resolve(tagPath + ".json");
                Files.createDirectories(file.getParent());
                Files.writeString(file, out);
            }

        } catch (IOException ex) {
            throw new RuntimeException("Failed building TAN tag datapack", ex);
        }
    }

    private static void applyOne(java.util.function.BiConsumer<String,String> add, String idOrTagRef,
                                 FoodOverrideConfig.OverrideEntry ov) {
        if (ov == null) return;

        if (Boolean.TRUE.equals(ov.tan_cooling_on_consume())) {
            add.accept("cooling_consumed_items", idOrTagRef);
        }
        if (Boolean.TRUE.equals(ov.tan_heating_on_consume())) {
            add.accept("heating_consumed_items", idOrTagRef);
        }

        if (ov.tan_thirst() != null) {
            int n = Math.max(1, Math.min(20, ov.tan_thirst()));
            add.accept("thirst/" + n + "_thirst_drinks", idOrTagRef);
        }

        if (ov.tan_hydration() != null) {
            int x = ov.tan_hydration();

            x = Math.max(10, Math.min(100, (x / 10) * 10));
            add.accept("hydration/" + x + "_hydration_drinks", idOrTagRef);
        }
    }

    private static void deleteRecursively(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var s = Files.walk(p)) {
            s.sorted(Comparator.reverseOrder()).forEach(pp -> {
                try { Files.deleteIfExists(pp); } catch (IOException ignored) {}
            });
        }
    }
}
