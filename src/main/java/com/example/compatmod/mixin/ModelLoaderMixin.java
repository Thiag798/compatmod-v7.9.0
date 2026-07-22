
package com.example.compatmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraftforge.client.model.ModelLoader")
public class ModelLoaderMixin {
    @Inject(method = "loadModel", at = @At("HEAD"))
    private void compatmod_onLoadModel(CallbackInfo ci) {}
}
