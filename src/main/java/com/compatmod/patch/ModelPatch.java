package com.compatmod.patch;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.function.Function;
import java.util.function.Predicate;

public record ModelPatch(
    String name,
    Predicate<JsonModelView> matcher,
    Function<String, String> transform
) {
    public static class JsonModelView {
        private final String rawJson;
        private JsonObject parsed;
        private boolean parseAttempted;
        private String parent;
        private String textureValues;

        public JsonModelView(String rawJson) {
            this.rawJson = rawJson;
        }

        /**
         * FIXED (2026-07-24): this used to scan the raw string by hand
         * (find `"parent"`, then the next `:`, then the next two quotes).
         * That breaks the moment the text "parent" shows up anywhere
         * earlier in the file (e.g. inside a comment-like key, or before
         * pretty-printing reorders fields) or the JSON uses escaped/unicode
         * characters around it. All callers now always hand this class real
         * model JSON (read straight from the resource pack, see
         * CompatTransformer), so there is no reason not to just parse it
         * properly with Gson.
         */
        public String parent() {
            JsonObject root = parseIfNeeded();
            if (parent == null) {
                parent = (root != null && root.has("parent") && root.get("parent").isJsonPrimitive())
                    ? root.get("parent").getAsString()
                    : "";
            }
            return parent;
        }

        /**
         * Concatenation of every value under "textures" (e.g. "block/oak_leaves"),
         * lower-cased. Use this -- not rawJson() -- when a patch needs to key off
         * what texture a model actually uses; scanning the whole raw JSON text
         * (as the original ambient_occlusion_disable patch did) also matches
         * unrelated fields that merely contain the same substring.
         */
        public String textureValues() {
            if (textureValues == null) {
                JsonObject root = parseIfNeeded();
                StringBuilder sb = new StringBuilder();
                if (root != null && root.has("textures") && root.get("textures").isJsonObject()) {
                    for (var entry : root.getAsJsonObject("textures").entrySet()) {
                        JsonElement v = entry.getValue();
                        if (v.isJsonPrimitive()) {
                            sb.append(v.getAsString().toLowerCase()).append(' ');
                        }
                    }
                }
                textureValues = sb.toString();
            }
            return textureValues;
        }

        /** Kept for callers that genuinely need the raw text (e.g. logging). */
        public String rawJson() { return rawJson; }

        private JsonObject parseIfNeeded() {
            if (parseAttempted) return parsed;
            parseAttempted = true;
            try {
                JsonElement el = JsonParser.parseString(rawJson);
                if (el.isJsonObject()) parsed = el.getAsJsonObject();
            } catch (Exception ignored) {
                parsed = null;
            }
            return parsed;
        }
    }
}
