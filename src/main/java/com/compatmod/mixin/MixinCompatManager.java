package com.compatmod.mixin;

import com.compatmod.core.Logging;

import java.util.*;

public final class MixinCompatManager {

    private MixinCompatManager() { throw new UnsupportedOperationException("Utility class"); }

    private static final Map<String, String> COREMODS = new LinkedHashMap<>();
    private static final Map<String, Set<String>> CONFLICTING_MIXINS = new LinkedHashMap<>();

    static {
        COREMODS.put("optifine", "OptiFine");
        COREMODS.put("sodium", "Sodium");
        COREMODS.put("embeddium", "Embeddium");
        COREMODS.put("rubidium", "Rubidium");
        COREMODS.put("oculus", "Oculus");
        COREMODS.put("iris", "Iris");
        COREMODS.put("canvas", "Canvas Renderer");

        CONFLICTING_MIXINS.put("optifine", Set.of("MixinParticleEngine"));
        CONFLICTING_MIXINS.put("sodium", Set.of());
    }

    private static int detectedCoremods = 0;

    public static void detectCoremods() {
        detectedCoremods = 0;
        for (Map.Entry<String, String> e : COREMODS.entrySet()) {
            try { tryLoadCoremodClass(e.getKey()); detectedCoremods++;
                Logging.compatibility("Detected coremod: {}", e.getValue());
            } catch (ClassNotFoundException ignored) {}
        }
        Logging.mixin("Coremod scan: {}/{} detected", detectedCoremods, COREMODS.size());
    }

    private static void tryLoadCoremodClass(String pkg) throws ClassNotFoundException {
        switch (pkg) {
            case "optifine":  Class.forName("net.optifine.Config"); break;
            case "sodium":    Class.forName("me.jellysquid.mods.sodium.client.SodiumClientMod"); break;
            case "embeddium": Class.forName("org.embeddedt.embeddium.Embeddium"); break;
            default: throw new ClassNotFoundException(pkg);
        }
    }

    public static boolean shouldLoad(String mixinClassName) {
        for (Map.Entry<String, Set<String>> e : CONFLICTING_MIXINS.entrySet()) {
            boolean loaded = false;
            try { tryLoadCoremodClass(e.getKey()); loaded = true;
            } catch (ClassNotFoundException ignored) {}
            if (loaded && e.getValue().contains(mixinClassName)) {
                Logging.mixin("Skipping mixin {} — conflicts with {}", mixinClassName, COREMODS.get(e.getKey()));
                return false;
            }
        }
        return true;
    }

    public static int getDetectedCoremods() { return detectedCoremods; }
    public static Map<String, String> getCoremods() { return Collections.unmodifiableMap(COREMODS); }
}
