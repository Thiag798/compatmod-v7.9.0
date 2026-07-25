package com.compatmod.cache;

import com.compatmod.config.ModConfig;

import java.util.concurrent.atomic.AtomicInteger;

public class CacheInspector {
    private static final AtomicInteger cachedCount = new AtomicInteger(0);
    private static final AtomicInteger patchedCount = new AtomicInteger(0);

    public static void recordCache() { cachedCount.incrementAndGet(); }
    public static void recordPatch() { patchedCount.incrementAndGet(); }

    public static int getCachedCount() { return cachedCount.get(); }
    public static int getPatchedCount() { return patchedCount.get(); }

    public static void reset() {
        cachedCount.set(0);
        patchedCount.set(0);
    }

    public record InspectionResult(int cached, int patched, boolean safeModeActive) {}

    // FIXED (2026-07-24): InspectionResult existed but nothing ever built one --
    // every caller read the two counters separately instead. This is the one
    // place that actually assembles the record, used by CompatCommand's
    // "cache" subcommand.
    public static InspectionResult snapshot() {
        return new InspectionResult(cachedCount.get(), patchedCount.get(), ModConfig.isSafeMode());
    }
}
