package com.compatmod.patch;

import com.compatmod.CompatMod;
import com.compatmod.cache.CacheInspector;
import com.compatmod.config.ModConfig;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;

public class CompatRegistry {

    /**
     * FIXED (2026-07-24): this was a plain ArrayList that reload()/registerBuiltin()
     * mutated in place (clear() then add()...), while getPatches() handed out an
     * *unmodifiable view* of that same list (not a copy) for the model-baking
     * thread to iterate. Running "/compatmod reload" while a model was being
     * baked could race a clear()/add() against an in-progress iteration of the
     * same backing list -- classic ConcurrentModificationException territory.
     * Now each (re)build assembles a brand-new immutable list and swaps the
     * volatile reference atomically; anyone already iterating the old list
     * keeps seeing a consistent (if slightly stale) snapshot.
     */
    private static volatile List<ModelPatch> patches = List.of();

    public static void registerBuiltin() {
        List<ModelPatch> built = List.of(
            // Patch 1: Glass cullface fix
            // Matches cube_all (vanilla glass, stained glass), glass_pane,
            // and any model with "glass" in the parent path (modded glass)
            new ModelPatch("glass_cullface",
                view -> {
                    String p = view.parent();
                    return p.contains("cube_all")
                        || p.contains("glass")
                        || p.contains("glass_pane")
                        || p.contains("stained_glass");
                },
                json -> {
                    try {
                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                        root.addProperty("render_type", "minecraft:translucent");
                        root.addProperty("ambientocclusion", false);
                        return root.toString();
                    } catch (Exception e) {
                        CompatMod.LOGGER.warn("glass_cullface patch failed: {}", e.getMessage());
                        return json;
                    }
                }
            ),

            // Patch 2: Ambient occlusion off for foliage
            // FIXED (2026-07-24): this used to test view.rawJson() -- the whole
            // serialized model, key names and all -- for "leaves"/"foliage"/
            // "plant"/"grass". That matches far more than intended: e.g. any
            // block whose *parent path* happens to contain "grass" (vanilla's
            // own dirt-with-grass-overlay side models, grass path blocks,
            // grass carpets, etc.) would get ambient occlusion silently
            // disabled even though it has nothing to do with foliage.
            // Checking the actual texture values (and the parent) is much
            // closer to "is this model foliage-like".
            new ModelPatch("ambient_occlusion_disable",
                view -> {
                    String p = view.parent();
                    String tex = view.textureValues();
                    return p.contains("leaves") || tex.contains("leaves")
                        || p.contains("foliage") || tex.contains("foliage")
                        || tex.contains("plant")
                        || p.contains("tall_grass") || tex.contains("tall_grass");
                },
                json -> {
                    try {
                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                        root.addProperty("ambientocclusion", false);
                        return root.toString();
                    } catch (Exception e) {
                        return json;
                    }
                }
            ),

            // Patch 3: UV normalization for cross/flower models
            new ModelPatch("uv_normalization",
                view -> {
                    String p = view.parent();
                    return p.contains("cross")
                        || p.contains("tall_grass")
                        || p.contains("flower");
                },
                json -> {
                    try {
                        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
                        if (root.has("elements")) {
                            var elements = root.getAsJsonArray("elements");
                            for (var elem : elements) {
                                var obj = elem.getAsJsonObject();
                                if (obj.has("faces")) {
                                    var faces = obj.getAsJsonObject("faces");
                                    for (var face : faces.entrySet()) {
                                        var faceObj = face.getValue().getAsJsonObject();
                                        if (faceObj.has("uv")) {
                                            var uv = faceObj.getAsJsonArray("uv");
                                            for (int i = 0; i < uv.size(); i++) {
                                                float v = uv.get(i).getAsFloat();
                                                uv.set(i, new com.google.gson.JsonPrimitive(
                                                    Math.max(0f, Math.min(16f, v))));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        return root.toString();
                    } catch (Exception e) {
                        return json;
                    }
                }
            )
        );

        patches = built;
        CompatMod.LOGGER.info("CompatRegistry: {} built-in patches registered", built.size());
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
