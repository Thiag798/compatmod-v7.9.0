package com.compatmod.core;

import com.google.gson.*;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.*;
import java.util.*;

public final class ConfigLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = resolveConfigDir();
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("advanced.json");

    private static JsonObject config = new JsonObject();
    private static int loadedEntries = 0;

    private ConfigLoader() { throw new UnsupportedOperationException("Utility class"); }

    private static Path resolveConfigDir() {
        try {
            return FMLPaths.CONFIGDIR.get().resolve("compatmod");
        } catch (NullPointerException | NoClassDefFoundError e) {
            return Paths.get(System.getProperty("java.io.tmpdir"), "compatmod-test-config");
        }
    }

    public static void initialize() {
        try {
            Files.createDirectories(CONFIG_DIR);
            if (!Files.exists(CONFIG_FILE)) writeDefaults();
            load();
        } catch (Exception e) {
            Logging.securityIncident("Config init failed: {} — using defaults", e.getMessage());
            writeDefaults();
            load();
        }
    }

    private static void writeDefaults() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("safe_mode", false);
        defaults.addProperty("debug_mode", false);
        defaults.addProperty("transformation_log_level", "INFO");
        defaults.addProperty("patch_cache_size", 2048);
        defaults.addProperty("max_model_depth", 16);
        defaults.add("blacklisted_mods", new JsonArray());
        try (Writer w = Files.newBufferedWriter(CONFIG_FILE,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            GSON.toJson(defaults, w);
        } catch (Exception e) {
            Logging.securityIncident("Failed to write default config: {}", e.getMessage());
        }
    }

    private static void load() {
        try (Reader r = Files.newBufferedReader(CONFIG_FILE)) {
            config = JsonParser.parseReader(r).getAsJsonObject();
            loadedEntries = config.entrySet().size();
        } catch (Exception e) {
            Logging.securityIncident("Failed to load config: {}", e.getMessage());
            config = new JsonObject();
        }
    }

    public static void reload() { load(); Logging.initialization("Config reloaded — {} entries", loadedEntries); }

    public static boolean isSafeMode() { return config.has("safe_mode") && config.get("safe_mode").getAsBoolean(); }
    public static boolean isDebugMode() { return config.has("debug_mode") && config.get("debug_mode").getAsBoolean(); }

    public static Set<String> getBlacklistedMods() {
        if (!config.has("blacklisted_mods")) return Collections.emptySet();
        Set<String> set = new HashSet<>();
        config.getAsJsonArray("blacklisted_mods").forEach(el -> set.add(el.getAsString()));
        return Collections.unmodifiableSet(set);
    }

    public static boolean isBlacklisted(String modId) { return getBlacklistedMods().contains(modId); }

    public static String getTransformationLogLevel() {
        return config.has("transformation_log_level") ? config.get("transformation_log_level").getAsString() : "INFO";
    }

    public static int getPatchCacheSize() {
        return config.has("patch_cache_size") ? config.get("patch_cache_size").getAsInt() : 2048;
    }

    public static int getMaxModelDepth() {
        return config.has("max_model_depth") ? config.get("max_model_depth").getAsInt() : 16;
    }

    public static int getLoadedEntryCount() { return loadedEntries; }
    public static Path getConfigDir() { return CONFIG_DIR; }
}