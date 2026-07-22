package com.compatmod.compat;

import com.compatmod.core.ConfigLoader;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelTransformCache {

    private ModelTransformCache() { throw new UnsupportedOperationException("Utility class"); }

    public static final class CacheEntry {
        private final ResourceLocation location;
        private final boolean wasChecked, wasTransformed;
        private final long timestamp;

        CacheEntry(ResourceLocation l, boolean c, boolean t, long ts) {
            this.location = l; this.wasChecked = c; this.wasTransformed = t; this.timestamp = ts;
        }
        public ResourceLocation location() { return location; }
        public boolean wasChecked() { return wasChecked; }
        public boolean wasTransformed() { return wasTransformed; }
        public long timestamp() { return timestamp; }
    }

    private static final Map<ResourceLocation, CacheEntry> HOT_CACHE = new ConcurrentHashMap<>();
    private static volatile long transformedCount = 0;
    private static volatile long checkedCount = 0;

    public static boolean isTransformed(ResourceLocation loc) {
        CacheEntry e = HOT_CACHE.get(loc);
        return e != null && e.wasChecked();
    }

    public static void markChecked(ResourceLocation loc, boolean wasTransformed) {
        int max = ConfigLoader.getPatchCacheSize();
        if (HOT_CACHE.size() >= max) evictOldest();
        HOT_CACHE.put(loc, new CacheEntry(loc, true, wasTransformed, System.currentTimeMillis()));
        checkedCount++;
        if (wasTransformed) transformedCount++;
    }

    public static int getHotEntries() { return HOT_CACHE.size(); }
    public static long getTransformedCount() { return transformedCount; }
    public static long getCheckedCount() { return checkedCount; }
    public static int getMaxSize() { return ConfigLoader.getPatchCacheSize(); }

    public static void clear() { HOT_CACHE.clear(); transformedCount = 0; checkedCount = 0; }

    public static String formatStats() {
        return String.format("L1 Hot: %d/%d entries | Transformed: %d | Checked: %d",
                HOT_CACHE.size(), ConfigLoader.getPatchCacheSize(), transformedCount, checkedCount);
    }

    private static void evictOldest() {
        long oldest = Long.MAX_VALUE;
        ResourceLocation oldestKey = null;
        for (Map.Entry<ResourceLocation, CacheEntry> e : HOT_CACHE.entrySet()) {
            if (e.getValue().timestamp() < oldest) {
                oldest = e.getValue().timestamp();
                oldestKey = e.getKey();
            }
        }
        if (oldestKey != null) HOT_CACHE.remove(oldestKey);
    }
}
