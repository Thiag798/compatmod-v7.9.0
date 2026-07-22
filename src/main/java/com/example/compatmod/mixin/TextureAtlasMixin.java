
package com.example.compatmod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.texture.TextureAtlas")
public class TextureAtlasMixin {
    @Inject(method = "stitch", at = @At("HEAD"))
    private void compatmod_onStitch(CallbackInfo ci) {}
}
