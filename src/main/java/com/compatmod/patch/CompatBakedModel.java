package com.compatmod.patch;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * NEW (2026-07-30): replaces the old Mixin-based approach entirely.
 * BakedModelWrapper is a public, documented Forge utility (not an internal
 * vanilla class) made exactly for this: extend it, override only the
 * methods you care about, delegate everything else to the original model.
 *
 * CAVEAT: I could not compile this against the real Forge jars (same
 * sandbox limitation as always), so I could not confirm the exact
 * ChunkRenderTypeSet factory method name below. If `ChunkRenderTypeSet.of(...)`
 * doesn't match your exact Forge/Parchment mapping set, check
 * net.minecraftforge.client.ChunkRenderTypeSet's static factories in your
 * IDE -- this is the one part of this rewrite I'd flag as needing a
 * second look during compilation.
 */
public class CompatBakedModel extends BakedModelWrapper<BakedModel> {

    private final boolean disableAmbientOcclusion;
    private final boolean forceTranslucent;
    private static final AtomicBoolean RENDER_TYPE_WARNING_LOGGED = new AtomicBoolean(false);

    public CompatBakedModel(BakedModel originalModel, boolean disableAmbientOcclusion, boolean forceTranslucent) {
        super(originalModel);
        this.disableAmbientOcclusion = disableAmbientOcclusion;
        this.forceTranslucent = forceTranslucent;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return disableAmbientOcclusion ? false : super.useAmbientOcclusion();
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        if (forceTranslucent) {
            try {
                return ChunkRenderTypeSet.of(net.minecraft.client.renderer.RenderType.translucent());
            } catch (LinkageError | RuntimeException e) {
                if (RENDER_TYPE_WARNING_LOGGED.compareAndSet(false, true)) {
                    com.compatmod.CompatMod.LOGGER.warn(
                        "CompatMod: translucent render type override failed; falling back to original model render types",
                        e
                    );
                }
            }
        }
        return super.getRenderTypes(state, rand, data);
    }
}
