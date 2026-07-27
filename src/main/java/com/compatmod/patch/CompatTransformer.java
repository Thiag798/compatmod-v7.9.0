package com.compatmod.patch;

import com.compatmod.CompatMod;
import com.compatmod.cache.CacheInspector;
import com.compatmod.logging.LegacyTransformLogger;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.lang.reflect.Field;
import com.google.gson.Gson;

/**
 * Applies JSON-level model patches.
 *
 * FIXED (2026-07-24): the previous implementation tried to recover the
 * original model JSON by reflecting into BlockModel looking for a declared
 * field of type JsonElement, then rebuilt the model with
 * BlockModel.fromString(json). A real BlockModel instance does not retain
 * its source JSON -- it stores already-resolved parent/elements/textures --
 * so that reflection almost certainly never found a field and silently fell
 * back to a raw Gson.toJsonTree(model) dump, which does not round-trip
 * through BlockModel's own Deserializer. `fromString` is also not a real
 * BlockModel API.
 *
 * This version instead re-reads the model's own source JSON straight from
 * the resource pack -- the exact same file ModelBakery itself already
 * parsed to produce `model` -- patches THAT text, and reconstructs the
 * model using BlockModel.GSON: the same Gson instance + custom Deserializer
 * Minecraft uses internally, so the result behaves identically to a model
 * Minecraft loaded on its own.
 */
public class CompatTransformer {

    private static Gson cachedGson = null;

    public static UnbakedModel transform(ResourceLocation location, UnbakedModel model) {
        if (isBuiltinModel(location)) return model;

        String json = readSourceJson(location);
        if (json == null) return model;

        String patchedJson = applyPatches(location, json);
        if (patchedJson.equals(json)) return model;

        try {
            Gson gson = getGson();
            if (gson == null) return model;

            BlockModel patched = gson.fromJson(patchedJson, BlockModel.class);
            CacheInspector.recordCache();
            return patched;
        } catch (Exception e) {
            CompatMod.LOGGER.error("Failed to reconstruct patched model for {}: {}",
                location, e.getMessage());
            return model;
        }
    }

    private static Gson getGson() {
        if (cachedGson != null) return cachedGson;
        try {
            // Tenta encontrar o campo GSON no BlockModel (pode estar ofuscado ou ter nomes diferentes)
            for (Field f : BlockModel.class.getDeclaredFields()) {
                if (f.getType() == Gson.class) {
                    f.setAccessible(true);
                    cachedGson = (Gson) f.get(null);
                    return cachedGson;
                }
            }
        } catch (Exception e) {
            CompatMod.LOGGER.error("Failed to find GSON field in BlockModel: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Pure JSON-in/JSON-out patch application. No Minecraft classes are
     * touched here, so this is directly unit-testable without a running
     * game -- see CompatTransformerTest.
     */
    public static String applyPatches(ResourceLocation location, String json) {
        ModelPatch.JsonModelView view = new ModelPatch.JsonModelView(json);

        for (ModelPatch patch : CompatRegistry.getPatches()) {
            if (patch.matcher().test(view)) {
                json = patch.transform().apply(json);
                view = new ModelPatch.JsonModelView(json);

                CompatMod.LOGGER.debug("CompatMod: Applied patch '{}' to {}", patch.name(), location);
                LegacyTransformLogger.log(location, patch.name());
                CacheInspector.recordPatch();
            }
        }
        return json;
    }

    /** Re-reads the model's own source JSON from the resource pack. */
    private static String readSourceJson(ResourceLocation location) {
        try {
            ResourceLocation modelPath = location.withPath(p -> "models/" + p + ".json");
            Optional<Resource> resource =
                Minecraft.getInstance().getResourceManager().getResource(modelPath);
            if (resource.isEmpty()) return null;

            try (InputStreamReader reader =
                     new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).toString();
            }
        } catch (Exception e) {
            CompatMod.LOGGER.debug("CompatMod: could not read source JSON for {}: {}",
                location, e.getMessage());
            return null;
        }
    }

    private static boolean isBuiltinModel(ResourceLocation loc) {
        return loc.getNamespace().equals("minecraft")
            && (loc.getPath().startsWith("builtin/") || loc.getPath().equals("missing"));
    }
}
