package com.compatmod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.util.List;

// NOTE: kept as ModConfig (not CompatModConfig) -- same simple name as
// net.minecraftforge.fml.config.ModConfig, but in a different package, so
// there's no actual clash; call sites just need to fully-qualify the Forge
// one where both would otherwise be in scope (see init() below).
public class ModConfig {
    private static final ForgeConfigSpec SERVER_SPEC;
    private static final ServerConfig SERVER;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER = pair.getLeft();
        SERVER_SPEC = pair.getRight();
    }

    public static void init() {
        // Use fully-qualified Forge ModConfig to avoid name clash with this class
        ModLoadingContext.get().registerConfig(
            net.minecraftforge.fml.config.ModConfig.Type.SERVER,
            SERVER_SPEC,
            "compatmod-server.toml"
        );
    }

    public static boolean isSafeMode()        { return SERVER.safeMode.get(); }
    public static void setSafeMode(boolean v) { SERVER.safeMode.set(v); }
    public static boolean isLogEnabled()      { return safeGet(SERVER.logTransforms, false); }
    public static java.nio.file.Path getConfigDir() { return FMLPaths.CONFIGDIR.get(); }

    // FIXED (2026-07-24): this config option existed in the TOML spec (an
    // admin could list patch names under "disabledPatches") but nothing ever
    // read it back -- adding a name there had zero effect. CompatRegistry now
    // consults this to filter getPatches().
    public static List<? extends String> getDisabledPatches() {
        return safeGet(SERVER.disabledPatches, List.of());
    }

    private static <T> T safeGet(ForgeConfigSpec.ConfigValue<T> configValue, T fallback) {
        try {
            return configValue.get();
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot get config value before config is loaded")) {
                return fallback;
            }
            throw e;
        }
    }

    public static class ServerConfig {
        public final ForgeConfigSpec.BooleanValue safeMode;
        public final ForgeConfigSpec.BooleanValue logTransforms;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> disabledPatches;

        ServerConfig(ForgeConfigSpec.Builder b) {
            b.push("compatmod");
            safeMode = b.comment("Disable all model patches (safe mode)")
                .define("safeMode", false);
            logTransforms = b.comment("Log all model transformations to file")
                .define("logTransforms", true);
            disabledPatches = b.comment("Patch names to disable")
                .defineList("disabledPatches", List.of(), e -> e instanceof String);
            b.pop();
        }
    }
}
