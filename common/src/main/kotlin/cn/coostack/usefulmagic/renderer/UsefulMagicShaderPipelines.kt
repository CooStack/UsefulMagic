package cn.coostack.usefulmagic.renderer

import cn.coostack.cooparticlesapi.extend.ofID
import cn.coostack.cooparticlesapi.renderer.RenderEntity
import cn.coostack.cooparticlesapi.renderer.pipeline.CooPipelines
import cn.coostack.cooparticlesapi.renderer.pipeline.CooRenderPipeline
import cn.coostack.usefulmagic.UsefulMagic
import net.minecraft.resources.ResourceLocation

/**
 * 创建 UsefulMagic 的实体渲染 Pipeline。
 *
 * Renderer 传入实体注册 ID，由这里统一派生 Pipeline ID。
 */
object UsefulMagicShaderPipelines {
    /**
     * 创建世界几何、两向模糊和屏幕合成组成的实体 Pipeline。
     *
     * 示例：
     * ```kotlin
     * override val pipeline = UsefulMagicShaderPipelines.maskBloom<MyRenderEntity>(
     *     renderEntityId = MyRenderEntity.ID,
     *     blurSigma = 8F,
     *     blurRange = 6F,
     *     intensity = 2F,
     *     baseMaskIntensity = 0.2F,
     * )
     * ```
     *
     * @param renderEntityId 实体的稳定注册 ID，用于派生唯一的 Pipeline ID。
     * @param blurSigma 高斯模糊强度。
     * @param blurRange 高斯模糊采样范围。
     * @param intensity 最终辉光强度；同一 Pipeline 的全部实体共用该值。
     * @param baseMaskIntensity 未模糊 mask 在合成阶段保留的强度。
     * @param threshold 进入模糊链的 mask 亮度门限。
     * @param thresholdSoftness mask 亮度门限的软过渡范围。
     * @return 可交给 RenderEntity renderer 的不可变 Pipeline。
     */
    fun <T : RenderEntity> maskBloom(
        renderEntityId: ResourceLocation,
        blurSigma: Float,
        blurRange: Float,
        intensity: Float,
        baseMaskIntensity: Float = 0F,
        threshold: Float = 0F,
        thresholdSoftness: Float = 0.015F,
    ): CooRenderPipeline<T> {
        return CooPipelines.entity(pipelineId(renderEntityId)) {
            val geometry = world("geometry") {
                maskOutput()
            }
            val brightExtract = pass("bright_extract") {
                fragment(cooShader("post/bloom_bright_extract.fsh"))
                input("scene")
                uniform("threshold", threshold)
                uniform("softKnee", thresholdSoftness)
            }
            val blurHorizontal = pass("blur_horizontal") {
                fragment(cooShader("post/bloom_blur_horizontal.fsh"))
                input("Input")
                uniform("Sigma", blurSigma)
                uniform("Range", blurRange)
            }
            val blurVertical = pass("blur_vertical") {
                fragment(cooShader("post/bloom_blur_vertical.fsh"))
                input("Input")
                uniform("Sigma", blurSigma)
                uniform("Range", blurRange)
            }
            val composite = pass("composite") {
                fragment(modShader("post/mask_bloom_composite.fsh"))
                input("SceneColor")
                input("Bloom")
                input("Mask")
                uniform("Intensity", intensity)
                uniform("BaseMaskIntensity", baseMaskIntensity)
            }

            line(geometry.color(), worldTarget())
            line(geometry.mask(), brightExtract.input("scene"))
            line(brightExtract.color(), blurHorizontal.input("Input"))
            line(blurHorizontal.color(), blurVertical.input("Input"))
            line(sceneColor(), composite.input("SceneColor"))
            line(blurVertical.color(), composite.input("Bloom"))
            line(geometry.mask(), composite.input("Mask"))
            line(composite.color(), screenTarget())
        }
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
