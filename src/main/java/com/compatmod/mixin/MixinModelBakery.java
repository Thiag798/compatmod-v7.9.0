package com.compatmod.mixin;

import com.compatmod.compat.VirtualModelLoader;
import com.compatmod.compat.ModelTransformCache;
import com.compatmod.core.Logging;
import com.compatmod.core.ConfigLoader;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Mixin(ModelBakery.class)
public abstract class MixinModelBakery {

    @Shadow @Final private ResourceManager resourceManager;

    @Inject(method = "getModel", at = @At("HEAD"), cancellable = false)
    private void compatmod$interceptModelLoading(
            ResourceLocation location, CallbackInfoReturnable<UnbakedModel> cir) {

        if (isBuiltinModel(location)) return;

        String modId = location.getNamespace();
        if (ConfigLoader.isBlacklisted(modId)) {
            Logging.transformation("Skipping blacklisted mod: {}", modId);
            return;
        }

        if (ModelTransformCache.isTransformed(location)) return;

        tryTransformModel(location);
    }

    @Unique
    private void tryTransformModel(ResourceLocation location) {
        try {
            Optional<Resource> opt = resourceManager.getResource(
                new ResourceLocation(location.getNamespace(),
                    "models/" + location.getPath() + ".json"));
            if (opt.isEmpty()) return;

            JsonObject orig;


            Resource res = null;


            Reader r = null;


            try {


                res = opt.get();


                r = new InputStreamReader(res.open(), StandardCharsets.UTF_8);


                orig = JsonParser.parseReader(r).getAsJsonObject();


            } finally {


                if (r != null) try { r.close(); } catch (Exception ignored) {}


            }

            JsonObject transformed = VirtualModelLoader.transformModel(location, orig);

            if (transformed == orig) {
                ModelTransformCache.markChecked(location, false);
                return;
            }

            ModelTransformCache.markChecked(location, true);
            if (ConfigLoader.isDebugMode()) {
                Logging.transformation("Transformed legacy model: {} (pattern: {})",
                        location, VirtualModelLoader.getLastDetectedPattern());
            }
        } catch (Exception e) {
            Logging.securityIncident(
                "Failed to transform model {}: {} — skipping, game continues",
                location, e.getMessage());
            if (ConfigLoader.isDebugMode()) {
                Logging.transformation("Transform failure details for {}:", location, e);
            }
            ModelTransformCache.markChecked(location, false);
        }
    }

    @Unique
    private boolean isBuiltinModel(ResourceLocation loc) {
        String ns = loc.getNamespace();
        return ns.equals("minecraft")
            && (loc.getPath().startsWith("builtin/") || loc.getPath().equals("missing"));
    }
}
