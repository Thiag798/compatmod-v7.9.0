package com.compatmod.patch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the ResourceLocation-string-based matchers directly -- no
 * BlockModel, no BakedModel, no Minecraft runtime needed, since
 * ModelPatch.matcher() is a plain Predicate<String>.
 *
 * FIXED (2026-07-30): switched from Predicate<ResourceLocation> to
 * Predicate<String> after ModelResourceLocation turned out not to extend
 * ResourceLocation in 1.21 (see ModelPatch.java / ModelBakeListener.java).
 * These tests now just pass plain "namespace:path" strings, which is what
 * ModelBakeListener actually feeds the matcher via location.toString().
 */
class CompatRegistryTest {

    @BeforeEach
    void setUp() {
        CompatRegistry.registerBuiltin();
    }

    @Test
    void testGlassModel_matchesGlassCullfacePatch() {
        var match = findMatch("minecraft:block/glass");
        assertNotNull(match, "expected a glass model to match a patch");
        assertEquals("glass_cullface", match.name());
        assertTrue(match.disableAmbientOcclusion());
        assertTrue(match.forceTranslucent());
    }

    @Test
    void testStainedGlassPane_matchesGlassCullfacePatch() {
        var match = findMatch("minecraft:block/red_stained_glass_pane");
        assertNotNull(match);
        assertEquals("glass_cullface", match.name());
    }

    @Test
    void testModdedGlass_stillMatchesByPath() {
        var match = findMatch("somemod:block/reinforced_glass");
        assertNotNull(match);
        assertEquals("glass_cullface", match.name());
    }

    @Test
    void testLeavesModel_disablesAOOnly() {
        var match = findMatch("minecraft:block/oak_leaves");
        assertNotNull(match);
        assertEquals("ambient_occlusion_disable", match.name());
        assertTrue(match.disableAmbientOcclusion());
        assertFalse(match.forceTranslucent());
    }

    @Test
    void testTallGrass_disablesAO() {
        var match = findMatch("minecraft:block/tall_grass");
        assertNotNull(match);
        assertEquals("ambient_occlusion_disable", match.name());
    }

    @Test
    void testFlowerModel_matchesFlowerPatch() {
        assertNull(findMatch("minecraft:block/poppy"),
            "a plain 'poppy' path has no 'flower' or 'cross' substring, so it should not match");

        var crossMatch = findMatch("minecraft:block/flower_pot_cross");
        assertNotNull(crossMatch);
        assertEquals("ambient_occlusion_disable_flowers", crossMatch.name());
    }

    @Test
    void testUnrelatedModel_matchesNothing() {
        assertNull(findMatch("minecraft:block/stone"));
        assertNull(findMatch("minecraft:block/oak_planks"));
    }

    /** Mirrors the "first matching patch wins" loop in ModelBakeListener. */
    private static ModelPatch findMatch(String locationString) {
        for (ModelPatch patch : CompatRegistry.getPatches()) {
            if (patch.matcher().test(locationString)) {
                return patch;
            }
        }
        return null;
    }
}
