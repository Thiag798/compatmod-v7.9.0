package com.compatmod.core;

import com.compatmod.compat.ModelTransformCache;
import com.compatmod.mixin.MixinCompatManager;

import java.lang.management.*;
import java.util.*;

public final class HealthChecker {

    private HealthChecker() { throw new UnsupportedOperationException("Utility class"); }

    public enum Status { HEALTHY, DEGRADED, DOWN }

    public record ServiceHealth(String name, Status status, String detail) {}

    public static Map<String, ServiceHealth> checkAll() {
        Map<String, ServiceHealth> results = new LinkedHashMap<>();

        try {
            results.put("config", new ServiceHealth("ConfigLoader", Status.HEALTHY,
                    ConfigLoader.getLoadedEntryCount() + " entries loaded"));
        } catch (Exception e) {
            results.put("config", new ServiceHealth("ConfigLoader", Status.DOWN, e.getMessage()));
        }

        try {
            results.put("mixins", new ServiceHealth("MixinCompatManager", Status.HEALTHY,
                    MixinCompatManager.getDetectedCoremods() + " coremods detected"));
        } catch (Exception e) {
            results.put("mixins", new ServiceHealth("MixinCompatManager", Status.DEGRADED, e.getMessage()));
        }

        try {
            results.put("cache", new ServiceHealth("ModelTransformCache", Status.HEALTHY,
                    ModelTransformCache.getHotEntries() + " hot entries, "
                    + ModelTransformCache.getTransformedCount() + " transformed"));
        } catch (Exception e) {
            results.put("cache", new ServiceHealth("ModelTransformCache", Status.DOWN, e.getMessage()));
        }

        try {
            MemoryMXBean mem = ManagementFactory.getMemoryMXBean();
            long used = mem.getHeapMemoryUsage().getUsed() / (1024 * 1024);
            long max = mem.getHeapMemoryUsage().getMax() / (1024 * 1024);
            double pct = (double) used / max * 100;
            Status s = pct > 85 ? Status.DEGRADED : Status.HEALTHY;
            results.put("memory", new ServiceHealth("JVM Heap", s,
                    String.format("%d/%d MB (%.1f%%)", used, max, pct)));
        } catch (Exception e) {
            results.put("memory", new ServiceHealth("JVM Heap", Status.DOWN, e.getMessage()));
        }

        return results;
    }

    public static String formatStatus() {
        Map<String, ServiceHealth> results = checkAll();
        long healthy = results.values().stream().filter(s -> s.status() == Status.HEALTHY).count();
        return healthy + "/" + results.size() + " healthy";
    }
}
