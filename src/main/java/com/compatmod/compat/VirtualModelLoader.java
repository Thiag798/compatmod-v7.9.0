package com.compatmod.compat;

import com.compatmod.core.Logging;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class VirtualModelLoader {

    private static final String MINECRAFT = "minecraft";
    private static volatile String lastDetectedPattern = "none";

    private static final ThreadLocal<Boolean> IS_TRANSFORMING = ThreadLocal.withInitial(() -> false);

    private static final Map<String, String> PARENT_REWRITES = new LinkedHashMap<>();

    static {
        PARENT_REWRITES.put("builtin/generated", "minecraft:block/cube_all");
        PARENT_REWRITES.put("builtin/entity",    "minecraft:builtin/entity");
        PARENT_REWRITES.put("item/generated",    "minecraft:item/generated");
        PARENT_REWRITES.put("block/cube",        "minecraft:block/cube");
        PARENT_REWRITES.put("builtin/missing",   "minecraft:block/cube_all");
    }

    private VirtualModelLoader() { throw new UnsupportedOperationException("Utility class"); }

    public static JsonObject transformModel(ResourceLocation location, JsonObject json) {
        if (IS_TRANSFORMING.get()) {
            Logging.securityIncident("Recursion guard hit for: {}", location);
            return json;
        }
        IS_TRANSFORMING.set(true);
        try {
            return doTransform(location, json);
        } finally {
            IS_TRANSFORMING.set(false);
        }
    }

    public static String getLastDetectedPattern() { return lastDetectedPattern; }

    private static JsonObject doTransform(ResourceLocation location, JsonObject json) {
        if (json.has("parent")) {
            JsonElement parentEl = json.get("parent");
            if (!parentEl.isJsonPrimitive() || !parentEl.getAsJsonPrimitive().isString()) {
                Logging.securityIncident("Non-string parent in model {}: {}", location, parentEl);
                return json;
            }
            String parent = parentEl.getAsString();

            if (parent.contains(":")) return json;

            String rewritten = PARENT_REWRITES.get(parent);
            if (rewritten != null) {
                lastDetectedPattern = parent;
                JsonObject copy = json.deepCopy();
                copy.addProperty("parent", rewritten);
                Logging.transformation("Rewrote parent '{}' -> '{}' for {}", parent, rewritten, location);
                return copy;
            }

            lastDetectedPattern = "unknown:" + parent;
            JsonObject copy = json.deepCopy();
            copy.addProperty("parent", MINECRAFT + ":" + parent);
            Logging.transformation("Added namespace to parent '{}' -> '{}' for {}",
                    parent, copy.get("parent").getAsString(), location);
            return copy;
        }

        if (!json.has("parent")) {
            lastDetectedPattern = "no_parent";
            JsonObject copy = json.deepCopy();
            copy.addProperty("parent", "minecraft:block/cube_all");
            Logging.transformation("Added default parent for {} (no parent found)", location);
            return copy;
        }

        return json;
    }
}
