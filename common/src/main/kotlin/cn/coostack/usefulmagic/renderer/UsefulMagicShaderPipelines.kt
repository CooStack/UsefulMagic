package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.pipeline.CooRenderPipeline
import cn.coostack.cooparticlesapi.renderer.pipeline.CooUniformValue
import cn.coostack.cooparticlesapi.renderer.shader.api.glsl.CooTextureFormat
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation

/**
 * 创建 UsefulMagic 的实体渲染 Pipeline。
 *
 * Renderer 传入实体注册 ID，由这里统一派生 Pipeline ID。
 */
object UsefulMagicShaderPipelines {
    /**
     * 创建世界几何、亮部提取、多尺度 BSL 模糊和屏幕合成组成的实体 Pipeline。
     *
     * 示例：
     * ```kotlin
     * override val pipeline = UsefulMagicShaderPipelines.maskBloom<MyRenderEntity>(
     *     renderEntityId = MyRenderEntity.ID,
     *     bloomMipLevels = 6,
     *     intensity = 2F,
     *     baseMaskIntensity = 0.2F,
     * )
     * ```
     *
     * 仅在客户端创建并交给 renderer 使用；资源和后处理阶段由 CooParticlesAPI 管理。
     *
     * @param T 当前 renderer 接收的实体类型。
     * @param renderEntityId 实体的稳定注册 ID，用于派生唯一的 Pipeline ID。
     * @param bloomMipLevels 参与 BSL 多尺度重建的级数，取值范围为 1 到 7。
     * @param intensity 最终辉光强度；同一 Pipeline 的全部实体共用该值。
     * @param baseMaskIntensity 未模糊 mask 在合成阶段保留的强度。
     * @param threshold 进入模糊链的 mask 亮度门限。
     * @param thresholdSoftness mask 亮度门限的软过渡范围。
     * @return 可交给 RenderEntity renderer 的不可变 Pipeline。
     * @throws IllegalArgumentException bloomMipLevels 不在 1 到 7 时抛出。
     */
    fun <T : RenderEntity> maskBloom(
        renderEntityId: ResourceLocation,
        bloomMipLevels: Int,
        intensity: Float,
        baseMaskIntensity: Float = 0F,
        threshold: Float = 0F,
        thresholdSoftness: Float = 0.015F,
    ): CooRenderPipeline<T> {
        require(bloomMipLevels in 1..7) {
            "Bloom mip levels must be between 1 and 7"
        }
        return CooPipelines.entity<T>(pipelineId(renderEntityId)) {
            postInScene()
            val geometry = world("geometry") {
                maskOutput()
                outputFormat(CooTextureFormat.RGBA16F)
            }
            val brightExtract = pass("bright_extract") {
                fragment(cooShader("post/bloom_bright_extract.fsh"))
                input("scene", format = CooTextureFormat.RGBA16F)
                outputFormat(CooTextureFormat.RGBA16F)
                mipLevels(12)
                uniform("threshold", 0F)
                uniform("softKnee", 0.5F)
                uniform("PremultipliedInput", CooUniformValue.BoolValue(true))
                uniform("Intensity", 3F)
            }
            val bloomBslAtlas = pass("bloom_bsl_atlas") {
                fragment(cooShader("post/bloom_bsl_atlas.fsh"))
                input("BloomInput", format = CooTextureFormat.RGBA16F, mipLevels = 12)
                outputFormat(CooTextureFormat.RGBA16F)
                uniform("BloomLevels", CooUniformValue.IntValue(7))
            }
            val composite = pass("composite") {
                fragment(modShader("post/mask_bloom_composite.fsh"))
                input("SceneColor")
                input("BloomAtlas", format = CooTextureFormat.RGBA16F)
                input("Mask", format = CooTextureFormat.RGBA16F)
                uniform("MipLevels", CooUniformValue.IntValue(7))
                uniform("Intensity", 3F)
                uniform("BaseMaskIntensity", baseMaskIntensity)
                outputFormat(CooTextureFormat.RGBA8)
            }

            line(geometry.color(), worldTarget())
            line(geometry.mask(), brightExtract.input("scene"))
            line(brightExtract.color(), bloomBslAtlas.input("BloomInput"))
            line(sceneColor(), composite.input("SceneColor"))
            line(bloomBslAtlas.color(), composite.input("BloomAtlas"))
            line(geometry.mask(), composite.input("Mask"))
            line(composite.color(), screenTarget())
            parameter("bloomThreshold", brightExtract, "threshold")
            parameter("bloomSoftKnee", brightExtract, "softKnee")
            parameter("intensity", brightExtract, "Intensity")
            parameter("intensity", composite, "Intensity")
            parameter("bloomMipLevels", bloomBslAtlas, "BloomLevels")
            parameter("bloomMipLevels", composite, "MipLevels")
        }.bloomMipLevels(bloomMipLevels)
            .bloomThreshold(threshold)
            .bloomSoftKnee(thresholdSoftness)
            .intensity(intensity)
    }

    /** 初始化仍由旧屏幕后处理入口管理的独立效果。 */
    fun init() {
        UsefulMagicPostEffects.init()
    }

    /**
     * 根据实体注册 ID 生成同命名空间的 Pipeline ID。
     *
     * @return 与实体 ID 一一对应的 Pipeline ID
     */
    private fun pipelineId(renderEntityId: ResourceLocation): ResourceLocation {
        val pipelinePath = renderEntityId.path.removeSuffix("_render_entity")
        return ofID(renderEntityId.namespace, "render_pipeline/$pipelinePath")
    }

    /**
     * 获取 CooParticlesAPI 内置 shader 的资源 ID。
     *
     * @return 指向 CooParticlesAPI shader 资源的 ID
     */
    private fun cooShader(path: String): ResourceLocation {
        return ofID(path)
    }

    /**
     * 获取 UsefulMagic shader 的资源 ID。
     *
     * @return 指向 UsefulMagic shader 资源的 ID
     */
    private fun modShader(path: String): ResourceLocation {
        return ofID(UsefulMagic.MOD_ID, path)
    }
}
