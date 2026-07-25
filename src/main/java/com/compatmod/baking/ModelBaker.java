package com.compatmod.baking;

import com.compatmod.CompatMod;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * NOT CURRENTLY CALLED FROM ANYWHERE (confirmed 2026-07-24 -- zero references
 * outside this file in the whole project). Patches today are defined as Java
 * {@code Function<String,String>} objects in CompatRegistry, which apply
 * their own targeted field edits; nothing in that design needs a generic
 * JSON-merge helper. This class only makes sense if/when patches move to a
 * declarative "JSON merge-patch" format (e.g. patch files shipped as
 * resources instead of hardcoded in CompatRegistry) -- at that point this is
 * exactly the merge primitive you'd want. Until then, this is dead code:
 * either wire it into a real patch-loading path, or delete it. Left as-is
 * rather than removed, since only you can say which of those two you want.
 */
public class ModelBaker {

    public static String merge(String baseJson, String overrideJson) {
        try {
            JsonObject base = JsonParser.parseString(baseJson).getAsJsonObject();
            JsonObject override = JsonParser.parseString(overrideJson).getAsJsonObject();
            for (var entry : override.entrySet()) {
                base.add(entry.getKey(), entry.getValue());
            }
            return base.toString();
        } catch (Exception e) {
            CompatMod.LOGGER.error("ModelBaker merge failed: {}", e.getMessage());
            return baseJson;
        }
    }

    public static boolean isValidModelJson(String json) {
        try {
            JsonParser.parseString(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
