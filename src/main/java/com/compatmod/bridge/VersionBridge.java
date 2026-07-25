package com.compatmod.bridge;

import com.compatmod.CompatMod;
import net.minecraftforge.fml.loading.FMLLoader;

public class VersionBridge {

    public static String getMinecraftVersion() {
        try {
            return net.minecraft.SharedConstants.getCurrentVersion().getName();
        } catch (Exception e) {
            CompatMod.LOGGER.debug("VersionBridge fallback: {}", e.getMessage());
            return "1.21.1";
        }
    }

    public static String getForgeVersion() {
        try {
            return FMLLoader.versionInfo().toString();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static boolean isClientSide() {
        try {
            return FMLLoader.getDist().isClient();
        } catch (Exception e) {
            return true;
        }
    }
}
