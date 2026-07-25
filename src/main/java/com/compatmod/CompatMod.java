package com.compatmod;

import com.compatmod.bridge.VersionBridge;
import com.compatmod.command.CompatCommand;
import com.compatmod.config.BlacklistConfig;
import com.compatmod.config.ModConfig;
import com.compatmod.logging.LegacyTransformLogger;
import com.compatmod.patch.CompatRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CompatMod.MOD_ID)
public class CompatMod {
    public static final String MOD_ID = "compatmod";
    public static final Logger LOGGER = LoggerFactory.getLogger("CompatMod");

    // Forge 51.x (1.21.1): IEventBus is injected directly into the constructor
    public CompatMod(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new CompatCommand());

        ModConfig.init();
        BlacklistConfig.init();
        CompatRegistry.registerBuiltin();
        LegacyTransformLogger.init();

        LOGGER.info("CompatMod v8.2.0 initialized -- {} patches loaded", CompatRegistry.getPatches().size());
    }

    @SubscribeEvent
    public void commonSetup(FMLCommonSetupEvent event) {
        // FIXED (2026-07-24): VersionBridge existed but nothing ever called it --
        // there was no diagnostic trail at all of which MC/Forge build a given
        // patch was applied under, which matters a lot for a mod whose whole
        // job is version-specific model compatibility.
        LOGGER.info("CompatMod common setup complete (MC {}, Forge {}, side: {})",
            VersionBridge.getMinecraftVersion(),
            VersionBridge.getForgeVersion(),
            VersionBridge.isClientSide() ? "client" : "server");
    }
}
