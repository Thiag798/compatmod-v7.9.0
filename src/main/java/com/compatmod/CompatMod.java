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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(CompatMod.MOD_ID)
public class CompatMod {
    public static final String MOD_ID = "compatmod";
    public static final Logger LOGGER = LoggerFactory.getLogger("CompatMod");

    // FIXED (2026-07-26): the previous version declared this constructor as
    // CompatMod(IEventBus modEventBus), on the assumption -- mine, and wrong
    // -- that Forge 1.21.1 injects the event bus directly as a constructor
    // parameter. A real crash log proved otherwise:
    //
    //   java.lang.NoSuchMethodException: com.compatmod.CompatMod.<init>()
    //       at java.base/java.lang.Class.getDeclaredConstructor(...)
    //       at ...FMLModContainer.constructMod(FMLModContainer.java:143)
    //
    // FML's constructMod() called getDeclaredConstructor() with ZERO
    // arguments and failed because no such constructor existed -- meaning
    // this exact build (Forge 52.1.0 / MC 1.21.1) looks up the mod's main
    // class via a no-arg constructor, not an IEventBus-parameterized one.
    // This is the classic, long-standing pattern (still valid, if flagged
    // deprecated-for-future-removal, as of 1.21.1) and is what the crash log
    // proves this Forge build actually expects. Because this failed, the
    // mod's CONSTRUCT lifecycle event never completed -- ModConfig.init(),
    // BlacklistConfig.init(), CompatRegistry.registerBuiltin(), and
    // LegacyTransformLogger.init() never ran at all. This was the *first*
    // failure in the crash, ahead of (and independent from) the Mixin/refmap
    // SRG-mismatch failure that followed it.
    public CompatMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
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
