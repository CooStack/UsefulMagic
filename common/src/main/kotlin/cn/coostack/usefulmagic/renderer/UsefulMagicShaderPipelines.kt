package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.renderer.backend.RenderBackendCapability

object UsefulMagicShaderPipelines {
    val DEPTH_AWARE_MASK_BLOOM_CAPABILITIES: Set<RenderBackendCapability> = setOf(
        RenderBackendCapability.FINAL_FRAME_POST,
        RenderBackendCapability.SAFE_WORLD_COMPOSITE,
        RenderBackendCapability.SCENE_DEPTH_READ,
    )

    fun init() {
        // CooParticlesAPI 2.3.8 installs builtin post effects through CooParticlesAPIClient.initShaderPrograms().
        UsefulMagicPostEffects.init()
    }
}
