package com.compatmod.patch;

import com.compatmod.CompatMod;
import com.compatmod.cache.CacheInspector;
import com.compatmod.config.ModConfig;

import java.util.List;

public class CompatRegistry {

    private static volatile List<ModelPatch> patches = List.of();

    public static void registerBuiltin() {
        List<ModelPatch> built = List.of(
            // Patch 1: Glass -- force translucent render type, disable AO.
            new ModelPatch("glass_cullface",
                loc -> containsAny(loc, "glass"),
                true,
                true
            ),

            // Patch 2: Foliage -- disable AO only.
            new ModelPatch("ambient_occlusion_disable",
                loc -> containsAny(loc, "leaves", "foliage", "tall_grass"),
                true,
                false
            ),

            // Patch 3 (uv_normalization) intentionally dropped -- see notes
            // from the previous rewrite; no clean equivalent once working
            // with already-baked quads.
            new ModelPatch("ambient_occlusion_disable_flowers",
                loc -> containsAny(loc, "flower", "cross"),
                true,
                false
            )
        );

        patches = built;
        CompatMod.LOGGER.info("CompatRegistry: {} built-in patches registered", built.size());
    }

    private static boolean containsAny(String locationString, String... needles) {
        String p = locationString.toLowerCase();
        for (String n : needles) {
            if (p.contains(n)) return true;
        }
        return false;
    }

    public static List<ModelPatch> getPatches() {
        var disabled = ModConfig.getDisabledPatches();
        if (disabled.isEmpty()) return patches;
        return patches.stream().filter(p -> !disabled.contains(p.name())).toList();
    }

    public static void reload() {
        registerBuiltin();
        CacheInspector.reset();
        CompatMod.LOGGER.info("CompatRegistry: patches reloaded ({})", patches.size());
    }
}
