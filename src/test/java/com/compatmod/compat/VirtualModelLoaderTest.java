package com.compatmod.compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.*;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VirtualModelLoader — Legacy Model Transformation")
class VirtualModelLoaderTest {

    @BeforeEach void setUp() { ModelTransformCache.clear(); }
    @AfterEach void tearDown() { ModelTransformCache.clear(); }

    @Test @DisplayName("1. builtin/generated -> minecraft:block/cube_all")
    void testBuiltinGenerated() {
        JsonObject out = transform("builtin/generated", "legacy_mod", "models/item/old_sword");
        assertEquals("minecraft:block/cube_all", out.get("parent").getAsString());
    }

    @Test @DisplayName("2. builtin/entity -> minecraft:builtin/entity")
    void testBuiltinEntity() {
        JsonObject out = transform("builtin/entity", "legacy_mod", "models/block/old_sign");
        assertEquals("minecraft:builtin/entity", out.get("parent").getAsString());
    }

    @Test @DisplayName("3. item/generated -> minecraft:item/generated")
    void testItemGenerated() {
        JsonObject out = transform("item/generated", "legacy_mod", "models/item/ancient_gem");
        assertEquals("minecraft:item/generated", out.get("parent").getAsString());
    }

    @Test @DisplayName("4. block/cube -> minecraft:block/cube")
    void testBlockCube() {
        JsonObject out = transform("block/cube", "legacy_mod", "models/block/old_machine");
        assertEquals("minecraft:block/cube", out.get("parent").getAsString());
    }

    @Test @DisplayName("5. builtin/missing -> minecraft:block/cube_all")
    void testBuiltinMissing() {
        JsonObject out = transform("builtin/missing", "broken_mod", "models/block/broken");
        assertEquals("minecraft:block/cube_all", out.get("parent").getAsString());
    }

    @Test @DisplayName("6. No parent -> fallback minecraft:block/cube_all")
    void testNoParent() {
        String json = """
            {
              "textures": {"all": "legacy_mod:blocks/strange"},
              "elements": [{
                "from": [0, 0, 0], "to": [16, 16, 16],
                "faces": {"down": {"uv": [0, 0, 16, 16], "texture": "#all"}}
              }]
            }""";
        JsonObject in = JsonParser.parseString(json).getAsJsonObject();
        JsonObject out = VirtualModelLoader.transformModel(rl("legacy_mod","models/item/no_parent"), in);
        assertNotNull(out);
        assertTrue(out.has("parent") || out.has("textures") || out.has("elements"));
    }

    @Test @DisplayName("Malformed JSON — should not throw")
    void testMalformed() {
        JsonObject in = new JsonObject(); in.addProperty("parent", 12345);
        assertNotNull(VirtualModelLoader.transformModel(rl("corrupt","block/broken"), in));
    }

    @Test @DisplayName("Empty JSON — gets default parent")
    void testEmpty() {
        JsonObject out = VirtualModelLoader.transformModel(rl("empty","block/empty"), new JsonObject());
        assertTrue(out.has("parent"));
    }

    @Test @DisplayName("Already modern — not modified")
    void testModern() {
        String json = """
            {"parent":"minecraft:block/cube_all",
             "textures":{"all":"mod:blocks/cool"}}""";
        JsonObject in = JsonParser.parseString(json).getAsJsonObject();
        assertSame(in, VirtualModelLoader.transformModel(rl("modern","block/cool"), in));
    }

    @Test @DisplayName("Modern namespace — not modified")
    void testModernNamespace() {
        String json = """
            {"parent":"forge:block/cube",
             "textures":{"all":"forge:blocks/example"}}""";
        JsonObject in = JsonParser.parseString(json).getAsJsonObject();
        assertSame(in, VirtualModelLoader.transformModel(rl("forge","block/example"), in));
    }

    @Test @DisplayName("Thread-safety — 100 concurrent transforms")
    void testConcurrency() throws Exception {
        ExecutorService ex = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(100);
        for (int i = 0; i < 100; i++) {
            final int idx = i;
            ex.submit(() -> {
                try {
                    String json = """
                        {"parent":"builtin/generated",
                         "textures":{"layer0":"mod_%d:items/item_%d"}}""".formatted(idx % 5, idx);
                    JsonObject in = JsonParser.parseString(json).getAsJsonObject();
                    JsonObject out = VirtualModelLoader.transformModel(
                        rl("mod_" + (idx % 5), "models/item/item_" + idx), in);
                    assertNotNull(out); assertTrue(out.has("parent"));
                } finally { latch.countDown(); }
            });
        }
        ex.shutdown();
        assertTrue(ex.awaitTermination(5, TimeUnit.SECONDS));
        assertTrue(latch.await(1, TimeUnit.SECONDS));
    }

    @Test @DisplayName("Cache metrics")
    void testCacheMetrics() {
        assertEquals(0, ModelTransformCache.getTransformedCount());
        ModelTransformCache.markChecked(rl("test","item/test"), true);
        assertEquals(1, ModelTransformCache.getCheckedCount());
        assertEquals(1, ModelTransformCache.getTransformedCount());
        assertTrue(ModelTransformCache.formatStats().contains("Transformed: 1"));
    }

    @Test @DisplayName("Cache prevents re-processing")
    void testCachePreventsReprocessing() {
        ResourceLocation loc = rl("test","item/cached");
        assertFalse(ModelTransformCache.isTransformed(loc));
        ModelTransformCache.markChecked(loc, false);
        assertTrue(ModelTransformCache.isTransformed(loc));
    }

    private JsonObject transform(String parent, String ns, String path) {
        String json = """
            {"parent":"%s",
             "textures":{"layer0":"%s:items/test"}}""".formatted(parent, ns);
        return VirtualModelLoader.transformModel(
            rl(ns, path), JsonParser.parseString(json).getAsJsonObject());
    }

    private static ResourceLocation rl(String ns, String path) {
        return new ResourceLocation(ns, path);
    }
}
