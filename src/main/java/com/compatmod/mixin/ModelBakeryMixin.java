package com.compatmod.mixin;

import com.compatmod.config.BlacklistConfig;
import com.compatmod.config.ModConfig;
import com.compatmod.patch.CompatTransformer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {

    @Inject(method = "getModel", at = @At("RETURN"), cancellable = true)
    private void compatmod$onGetModel(ResourceLocation location,
                                       CallbackInfoReturnable<UnbakedModel> cir) {
        if (ModConfig.isSafeMode()) return;
        if (BlacklistConfig.isBlacklisted(location)) return;

        UnbakedModel original = cir.getReturnValue();
        UnbakedModel patched = CompatTransformer.transform(location, original);

        if (patched != original) {
            cir.setReturnValue(patched);
        }
    }
}
