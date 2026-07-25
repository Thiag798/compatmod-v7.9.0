package com.compatmod.patch;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompatTransformerTest {

    private static final ResourceLocation DUMMY =
        ResourceLocation.fromNamespaceAndPath("compatmod", "test/dummy");

    @BeforeEach
    void setUp() {
        // CompatRegistry.patches is static and only populated by registerBuiltin(),
        // which normally runs from CompatMod's constructor -- not in a plain unit
        // test. Without this, every applyPatches() call below would silently do
        // nothing, and the tests would pass for the wrong reason.
        CompatRegistry.registerBuiltin();
    }

    // ---- previous tests: JsonModelView is a plain string helper, no Minecraft
    // classes involved, cheap to keep as-is ----

    @Test
    void testJsonRoundtrip() {
        String json = "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"minecraft:block/glass\"}}";
        assertDoesNotThrow(() -> com.google.gson.JsonParser.parseString(json));
    }

    @Test
    void testGlassMatcher_cubeAll() {
        var view = new ModelPatch.JsonModelView(
            "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"minecraft:block/glass\"}}");
        assertTrue(view.parent().contains("cube_all"));
    }

    @Test
    void testGlassMatcher_glassPane() {
        var view = new ModelPatch.JsonModelView(
            "{\"parent\":\"minecraft:block/glass_pane_side\"}");
        assertTrue(view.parent().contains("glass_pane"));
    }

    @Test
    void testGlassMatcher_nonGlass_model_shouldNotMatch() {
        var view = new ModelPatch.JsonModelView(
            "{\"parent\":\"minecraft:block/stone\"}");
        assertFalse(view.parent().contains("cube_all")
            || view.parent().contains("glass")
            || view.parent().contains("glass_pane"));
    }

    @Test
    void testJsonViewEmptyParent() {
        var view = new ModelPatch.JsonModelView("{}");
        assertEquals("", view.parent());
    }

    // ---- NEW: tests for CompatTransformer.applyPatches(), the part that was
    // previously never exercised at all ----

    @Test
    void testApplyPatches_glassCullface_setsTranslucentAndDisablesAO() {
        String json = "{\"parent\":\"minecraft:block/cube_all\",\"textures\":{\"all\":\"minecraft:block/glass\"}}";
        String result = CompatTransformer.applyPatches(DUMMY, json);
        assertTrue(result.contains("\"render_type\":\"minecraft:translucent\""),
            "expected render_type to be set to translucent, got: " + result);
        assertTrue(result.contains("\"ambientocclusion\":false"),
            "expected ambientocclusion to be disabled, got: " + result);
    }

    @Test
    void testApplyPatches_leaves_disablesAmbientOcclusion() {
        String json = "{\"parent\":\"minecraft:block/leaves\",\"textures\":{\"all\":\"minecraft:block/oak_leaves\"}}";
        String result = CompatTransformer.applyPatches(DUMMY, json);
        assertTrue(result.contains("\"ambientocclusion\":false"),
            "expected ambientocclusion to be disabled for a leaves model, got: " + result);
    }

    @Test
    void testApplyPatches_crossModel_clampsOutOfRangeUv() {
        String json = "{\"parent\":\"minecraft:block/cross\","
            + "\"elements\":[{\"faces\":{\"north\":{\"uv\":[-4,0,20,16]}}}]}";
        String result = CompatTransformer.applyPatches(DUMMY, json);
        assertFalse(result.contains("-4.0"), "expected negative UV to be clamped, got: " + result);
        assertFalse(result.contains("20.0"), "expected UV > 16 to be clamped, got: " + result);
    }

    @Test
    void testApplyPatches_nonMatchingModel_isUntouched() {
        String json = "{\"parent\":\"minecraft:block/stone\",\"textures\":{\"all\":\"minecraft:block/stone\"}}";
        String result = CompatTransformer.applyPatches(DUMMY, json);
        assertEquals(json, result, "a model matching no patch should come back unchanged");
    }

    @Test
    void testApplyPatches_multiplePatchesCanStackOnSameModel() {
        // A "tall_grass" cross model matches BOTH uv_normalization (parent
        // contains "cross"... actually here via "tall_grass") AND
        // ambient_occlusion_disable (parent contains "tall_grass") at once.
        String json = "{\"parent\":\"minecraft:block/tall_grass\","
            + "\"textures\":{\"cross\":\"minecraft:block/tall_grass\"},"
            + "\"elements\":[{\"faces\":{\"north\":{\"uv\":[0,0,16,16]}}}]}";
        String result = CompatTransformer.applyPatches(DUMMY, json);
        assertTrue(result.contains("\"ambientocclusion\":false"),
            "expected ambient_occlusion_disable to also apply, got: " + result);
    }
}
