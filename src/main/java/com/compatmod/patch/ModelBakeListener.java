package com.compatmod.patch;

import com.compatmod.CompatMod;
import com.compatmod.cache.CacheInspector;
import com.compatmod.config.BlacklistConfig;
import com.compatmod.config.ModConfig;
import com.compatmod.logging.LegacyTransformLogger;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Entry point for the whole patch system (replaces the old
 * ModelBakeryMixin). ModelEvent.ModifyBakingResult is a real, documented
 * Forge event -- fired on the MOD event bus, client-side only, after models
 * are baked but before they're handed off for use. No Mixin, no refmap, no
 * SRG names.
 *
 * FIXED (2026-07-30, second compile attempt): event.getModels() is keyed by
 * ModelResourceLocation, which turned out to be a *record* as of 1.21 that
 * does NOT extend ResourceLocation (confirmed by the compiler rejecting a
 * direct pass to BlacklistConfig.isBlacklisted(ResourceLocation) etc). Rather
 * than guess a third time at whatever accessor gets the underlying
 * ResourceLocation back out, everything downstream (matching, blacklist,
 * logging) now works off location.toString() instead -- guaranteed to exist
 * on any object, record or not.
 */
@Mod.EventBusSubscriber(modid = CompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class ModelBakeListener {

    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        if (ModConfig.isSafeMode()) {
            CompatMod.LOGGER.warn("CompatMod: safe mode active, skipping model patching");
            return;
        }

        var registry = event.getModels();
        int patched = 0;

        for (ModelResourceLocation location : registry.keySet().toArray(new ModelResourceLocation[0])) {
            String locationString = location.toString();
            if (BlacklistConfig.isBlacklisted(locationString)) continue;

            boolean disableAO = false;
            boolean translucent = false;
            String matchedPatchName = null;

            for (ModelPatch patch : CompatRegistry.getPatches()) {
                if (patch.matcher().test(locationString)) {
                    disableAO |= patch.disableAmbientOcclusion();
                    translucent |= patch.forceTranslucent();
                    matchedPatchName = patch.name();
                }
            }

            if (matchedPatchName != null) {
                BakedModel original = registry.get(location);
                registry.put(location, new CompatBakedModel(original, disableAO, translucent));
                CompatMod.LOGGER.debug("CompatMod: Applied patch '{}' to {}", matchedPatchName, locationString);
                LegacyTransformLogger.log(locationString, matchedPatchName);
                CacheInspector.recordPatch();
                patched++;
            }
        }

        if (patched > 0) {
            CompatMod.LOGGER.info("CompatMod: patched {} baked models", patched);
        }
    }
}
