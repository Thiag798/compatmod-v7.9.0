package com.compatmod.patch;

import java.util.function.Predicate;

/**
 * Patches match against a model's string identifier (its ResourceLocation or
 * ModelResourceLocation's toString() -- see ModelBakeListener) rather than
 * the location object itself.
 *
 * FIXED (2026-07-30): originally this matched Predicate<ResourceLocation>,
 * but ModelEvent.ModifyBakingResult's registry is keyed by
 * ModelResourceLocation, which as of 1.21 is a record and does NOT extend
 * ResourceLocation (two separate compile errors trying to guess its API
 * confirmed this). Rather than guess a third time at exactly which accessor
 * gets you back to a ResourceLocation, matching on the plain string
 * identifier sidesteps the question entirely -- every object has toString(),
 * record or not.
 */
public record ModelPatch(
    String name,
    Predicate<String> matcher,
    boolean disableAmbientOcclusion,
    boolean forceTranslucent
) {
}
