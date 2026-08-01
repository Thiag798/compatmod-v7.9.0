package com.compatmod.config;

import com.compatmod.CompatMod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlacklistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    // FIXED (2026-07-24): plain HashSet, but isBlacklisted() is read from the
    // model-baking thread while add()/remove()/load() write from the server
    // command thread. A HashSet isn't thread-safe for concurrent
    // read+mutate -- this could throw or corrupt iteration state. A
    // concurrent set removes the risk without needing manual locking.
    private static final Set<String> blacklist = ConcurrentHashMap.newKeySet();
    private static Path blacklistFile;
    
    public static void init() {
        blacklistFile = ModConfig.getConfigDir().resolve("compatmod-blacklist.json");
        load();
    }
    
    private static void load() {
        blacklist.clear();
        if (Files.exists(blacklistFile)) {
            try (Reader r = Files.newBufferedReader(blacklistFile)) {
                Map<String, List<String>> data = GSON.fromJson(r,
                    new TypeToken<Map<String, List<String>>>(){}.getType());
                if (data != null && data.containsKey("blacklist")) {
                    blacklist.addAll(data.get("blacklist"));
                }
            } catch (IOException e) {
                CompatMod.LOGGER.error("Failed to load blacklist: {}", e.getMessage());
            }
        }
        CompatMod.LOGGER.info("Blacklist loaded: {} entries", blacklist.size());
    }
    
    private static void save() {
        try {
            Files.createDirectories(blacklistFile.getParent());
            try (Writer w = Files.newBufferedWriter(blacklistFile)) {
                GSON.toJson(Map.of("blacklist", new ArrayList<>(blacklist)), w);
            }
        } catch (IOException e) {
            CompatMod.LOGGER.error("Failed to save blacklist: {}", e.getMessage());
        }
    }
    
    public static boolean isBlacklisted(ResourceLocation location) {
        return isBlacklisted(location.toString());
    }

    // NEW (2026-07-30): added so callers holding only a ModelResourceLocation
    // (which, as of 1.21, is a record that does NOT extend ResourceLocation --
    // confirmed by two separate compile errors before this) can check the
    // blacklist via .toString() without needing to convert to a real
    // ResourceLocation first.
    public static boolean isBlacklisted(String locationString) {
        int colon = locationString.indexOf(':');
        String namespace = colon >= 0 ? locationString.substring(0, colon) : locationString;
        return blacklist.contains(locationString)
            || blacklist.contains(namespace + ":*");
    }
    
    public static boolean add(String entry) {
        boolean added = blacklist.add(entry);
        if (added) save();
        return added;
    }
    
    public static boolean remove(String entry) {
        boolean removed = blacklist.remove(entry);
        if (removed) save();
        return removed;
    }
    
    public static Set<String> getAll() { return Collections.unmodifiableSet(blacklist); }
}
