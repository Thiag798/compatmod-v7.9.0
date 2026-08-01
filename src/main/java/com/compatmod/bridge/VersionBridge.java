package com.compatmod.bridge;

import com.compatmod.CompatMod;
import net.minecraftforge.fml.loading.FMLLoader;

public class VersionBridge {

    // FIXED (2026-07-29): every method here caught `Exception`, but a mapping
    // mismatch at runtime (the exact class of bug we've been chasing this
    // whole thread) surfaces as NoSuchMethodError / NoSuchFieldError --
    // subclasses of Error, NOT Exception. `catch (Exception e)` never catches
    // those, so instead of falling back gracefully these calls crashed the
    // whole client. Catching Throwable actually delivers the fallback this
    // code was clearly meant to provide.
    public static String getMinecraftVersion() {
        try {
            return net.minecraft.SharedConstants.getCurrentVersion().getName();
        } catch (Throwable e) {
            CompatMod.LOGGER.debug("VersionBridge fallback: {}", e.getMessage());
            return "1.21.1";
        }
    }

    public static String getForgeVersion() {
        try {
            return FMLLoader.versionInfo().toString();
        } catch (Throwable e) {
            return "unknown";
        }
    }

    public static boolean isClientSide() {
        try {
            return FMLLoader.getDist().isClient();
        } catch (Throwable e) {
            return true;
        }
    }
}
