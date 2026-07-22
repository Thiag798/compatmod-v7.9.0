package com.compatmod.mixin;

import com.compatmod.core.Logging;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.List;
import java.util.Set;

public class MixinCompatPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        Logging.mixin("CompatMod Mixin Plugin loaded — package: {}", mixinPackage);
        MixinCompatManager.detectCoremods();
        Logging.mixin("Detected coremods: {}", MixinCompatManager.getDetectedCoremods());
    }

    @Override public String getRefMapperConfig() { return "compatmod.refmap.json"; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("MixinModelBakery")) return true;
        return MixinCompatManager.shouldLoad(mixinClassName);
    }

    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() { return List.of(); }
    @Override public void preApply(String t, ClassNode c, String m, IMixinInfo i) {}
    @Override public void postApply(String t, ClassNode c, String m, IMixinInfo i) {}
}
