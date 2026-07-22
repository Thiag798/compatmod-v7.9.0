
package com.example.compatmod;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.resources.model.ModelBakery")
public class MixinModelBakery {
    @Inject(method = "processLoading", at = @At("HEAD"), cancellable = true)
    private void compatmod_injectProcessLoading(CallbackInfo ci) {
        // compat injection
    }
}
