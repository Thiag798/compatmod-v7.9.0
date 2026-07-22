package com.compatmod;

import com.compatmod.command.DebugCommand;
import com.compatmod.compat.ModelTransformCache;
import com.compatmod.core.ConfigLoader;
import com.compatmod.core.HealthChecker;
import com.compatmod.core.Logging;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@Mod(ForgeCompatMod.MOD_ID)
public class ForgeCompatMod {

    public static final String MOD_ID = "compatmod";
    public static final String VERSION = "7.8.0";

    private static final Logger LOGGER = LogUtils.getLogger();

    public ForgeCompatMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::clientSetup);

        try {
            ConfigLoader.initialize();
            Logging.initialization("ConfigLoader initialized: {} entries loaded",
                    ConfigLoader.getLoadedEntryCount());
        } catch (Exception e) {
            Logging.securityIncident("ConfigLoader failed to initialize: {}", e.getMessage());
            Logging.initialization("Running with defaults");
        }

        ModelTransformCache.clear();
        Logging.initialization("ModelTransformCache cleared — capacity: {}",
                ModelTransformCache.getMaxSize());

        MinecraftForge.EVENT_BUS.register(this);

        logStartupBanner();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        Logging.initialization("Common setup complete — side: {}", net.minecraftforge.fml.loading.FMLEnvironment.dist.toString());
    }

    private void clientSetup(FMLClientSetupEvent event) {
        Logging.initialization("Client setup complete — MixinModelBakery active");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());
        Logging.initialization("DebugCommand registered — /compatmod available");
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        HealthChecker.checkAll();
        Logging.initialization("Server started — HealthChecker: {}",
                HealthChecker.formatStatus());
        if (ConfigLoader.isSafeMode()) {
            Logging.securityIncident(
                    "SAFE MODE ACTIVE — models are detected but NOT transformed");
        }
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        Logging.initialization("Server stopping — cache stats: {}",
                ModelTransformCache.formatStats());
        ModelTransformCache.clear();
    }

    private void logStartupBanner() {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptimeMs = rb.getUptime();
        Logging.initialization("========================================");
        Logging.initialization("  CompatMod v{} starting...", VERSION);
        Logging.initialization("  PID: {} | JDK: {}",
                ProcessHandle.current().pid(),
                System.getProperty("java.version"));
        Logging.initialization("  Config: safe_mode={}, debug={}, blacklist={}",
                ConfigLoader.isSafeMode(),
                ConfigLoader.isDebugMode(),
                ConfigLoader.getBlacklistedMods().size());
        Logging.initialization("  Cache: capacity={}", ModelTransformCache.getMaxSize());
        Logging.initialization("  Uptime: {}ms", uptimeMs);
        Logging.initialization("========================================");
    }
}
