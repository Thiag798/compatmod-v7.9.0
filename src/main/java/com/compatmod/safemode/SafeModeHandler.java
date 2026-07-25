package com.compatmod.safemode;

import com.compatmod.CompatMod;
import com.compatmod.config.ModConfig;
import com.compatmod.patch.CompatRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = CompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SafeModeHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        if (ModConfig.isSafeMode()) {
            CompatMod.LOGGER.warn("SAFE MODE ACTIVE - all model patches are DISABLED");
            CompatMod.LOGGER.warn("Use /compatmod safemode to toggle");
        }
    }

    public static boolean isOperational() {
        return !ModConfig.isSafeMode() && !CompatRegistry.getPatches().isEmpty();
    }
}
